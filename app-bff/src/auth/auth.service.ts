import { BadGatewayException, BadRequestException, Injectable, Logger, UnauthorizedException } from '@nestjs/common';
import { AxiosError } from 'axios';
import { createHash } from 'crypto';
import { buildSignedAccessToken } from '../common/auth-token';
import { resolveSubjectIdFromAccessToken } from '../common/auth-token';
import { BackendHttpService } from '../common/backend-http.service';

type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: {
    code?: string;
    message?: string;
  };
};

type BackendLoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: number | string;
  aipayUid: string;
  nickname: string;
};

type BackendUserProfile = Record<string, unknown> & {
  userId?: number | string;
  aipayUid?: string;
  loginId?: string;
  accountStatus?: string;
  kycLevel?: string;
  accountSource?: string;
  nickname?: string;
  avatarUrl?: string;
  countryCode?: string;
  mobile?: string;
  maskedRealName?: string;
  idCardNo?: string;
  gender?: string;
  region?: string;
  birthday?: string;
};

type BackendRegistration = {
  userId?: number | string;
  aipayUid?: string;
  loginId?: string;
  kycSubmitted?: boolean;
};

type BackendMoney = {
  amount?: string | number;
  currencyCode?: string;
  currencyUnit?: {
    code?: string;
  };
};

type BackendBankCard = {
  cardNo?: string;
  bankCode?: string;
  bankName?: string;
  cardType?: string;
  cardHolderName?: string;
  reservedMobile?: string;
  phoneTailNo?: string;
  defaultCard?: boolean;
  singleLimit?: BackendMoney;
  dailyLimit?: BackendMoney;
};

type BackendContactRequest = {
  requestNo?: string;
};

type BackendConversation = {
  conversationNo?: string;
};

type BackendMessage = {
  messageId?: string;
  senderUserId?: number | string;
  receiverUserId?: number | string;
  contentText?: string;
};

type BackendLoginPresetAccount = {
  loginId?: string;
  nickname?: string;
};

type BackendDemoProvisioningConfig = {
  demoTemplateLoginId?: string;
  demoContactLoginId?: string;
  demoLoginPassword?: string;
};

type BackendAgreementTemplate = {
  templateCode?: string;
  templateVersion?: string;
};

type BackendCreditOpenAgreementPack = {
  agreements?: BackendAgreementTemplate[];
};

type DemoCardType = 'DEBIT' | 'CREDIT';

type DemoBankCatalogEntry = {
  bankCode: string;
  bankName: string;
  debitPrefix: string;
  creditPrefix: string;
};

@Injectable()
export class AuthService {
  private static readonly DEVICE_VERIFY_LOGIN_FAIL_CODE = 'DEVICE_VERIFY_LOGIN_NOT_ALLOWED';
  private static readonly DEVICE_VERIFY_LOGIN_FAIL_MESSAGE = '本机号码验证仅支持本机已注册或本机演示账号，请使用密码登录';
  private static readonly DEMO_COUPON_COUNT = 10;
  private static readonly DEMO_WELCOME_MESSAGE = '您好！有什么问题可以帮您？';
  private static readonly DEMO_MAX_REGISTER_RETRY = 30;
  private static readonly DEMO_MAX_BIND_CARD_RETRY = 8;
  private static readonly DEMO_DEVICE_LOGIN_PROBE_COUNT = 1;
  private static readonly DEMO_LOGIN_PROBE_TIMEOUT_MS = 3000;
  private static readonly DEMO_TARGET_DEBIT_CARD_COUNT = 5;
  private static readonly DEMO_TARGET_CREDIT_CARD_COUNT = 5;
  private static readonly DEMO_DEFAULT_DEVICE_KEY = 'ios-demo-device';
  private static readonly DEMO_CONFIG_CACHE_TTL_MS = 60_000;
  private static readonly DEMO_CONFIG_APP_CODE = (process.env.OPENAIPAY_APP_CODE ?? 'OPENAIPAY_IOS').trim() || 'OPENAIPAY_IOS';
  private static readonly DEMO_CARD_HOLDER_FALLBACK = '演示用户';
  private static readonly DEMO_CARD_PREFIX = '622202';
  private static readonly DEMO_ACCOUNT_SOURCE = 'DEMO';
  private static readonly DEMO_WECHAT_NICKNAMES: ReadonlyArray<string> = [
    'Aimee',
    'Aurora',
    'momo',
    '小满',
    '阿澈',
    '晚风',
    '野渡舟',
    '不二',
    '木木',
    '云朵偷喝汽水',
    '今天也要开心',
    '慢热选手',
    '柚子乌龙',
    '橘子海',
    '夏末微凉',
    '凌晨四点',
    '把月亮藏起来',
    'Lucky',
    'Blue',
    '未读消息',
  ];
  private static readonly DEMO_BANK_CATALOG: ReadonlyArray<DemoBankCatalogEntry> = [
    { bankCode: 'ICBC', bankName: '中国工商银行', debitPrefix: '622200', creditPrefix: '622230' },
    { bankCode: 'ABC', bankName: '中国农业银行', debitPrefix: '622848', creditPrefix: '622836' },
    { bankCode: 'BOC', bankName: '中国银行', debitPrefix: '621661', creditPrefix: '625907' },
    { bankCode: 'CCB', bankName: '中国建设银行', debitPrefix: '621284', creditPrefix: '436742' },
    { bankCode: 'CMB', bankName: '招商银行', debitPrefix: '621483', creditPrefix: '622575' },
    { bankCode: 'BCM', bankName: '交通银行', debitPrefix: '622252', creditPrefix: '601428' },
    { bankCode: 'PSBC', bankName: '中国邮政储蓄银行', debitPrefix: '621098', creditPrefix: '622188' },
  ];

  private readonly logger = new Logger(AuthService.name);
  private readonly demoLoginIdByDeviceId = new Map<string, string>();
  private readonly demoLoginProvisionLocks = new Map<string, Promise<Record<string, unknown>>>();
  private readonly demoInitializationLocks = new Map<string, Promise<void>>();
  private readonly demoInitializedLoginIds = new Set<string>();
  private cachedDemoProvisioningConfig: {
    expiresAt: number;
    value: { demoTemplateLoginId: string; demoContactLoginId: string; demoLoginPassword: string };
  } | null = null;

  constructor(private readonly backendHttpService: BackendHttpService) {}

  async mobileVerifyLogin(
    loginId: string,
    deviceId: string,
    timeoutMs?: number,
    legacyDeviceIdsHeader?: string,
  ): Promise<Record<string, unknown>> {
    const normalizedLoginId = this.normalizeMainlandPhoneLoginId(loginId);
    const normalizedDeviceId = this.normalizeDeviceId(deviceId);
    if (!normalizedLoginId || !normalizedDeviceId) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'loginId 和 deviceId 不能为空',
      });
    }

    let authData: BackendLoginResponse | undefined;
    try {
      const loginResp = await this.backendHttpService.post<BackendEnvelope<BackendLoginResponse>, Record<string, string>>(
        '/api/auth/mobile-verify-login',
        {
          loginId: normalizedLoginId,
          deviceId: normalizedDeviceId,
        },
        {
          headers: this.buildForwardedDeviceHeaders(normalizedDeviceId, legacyDeviceIdsHeader),
          timeoutMs,
        },
      );
      authData = loginResp.data?.data;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, AuthService.DEVICE_VERIFY_LOGIN_FAIL_MESSAGE);
    }

    const profileUserKey = authData ? this.resolveProfileUserKey(authData) : null;
    if (!authData || !profileUserKey) {
      throw new UnauthorizedException({
        code: AuthService.DEVICE_VERIFY_LOGIN_FAIL_CODE,
        message: AuthService.DEVICE_VERIFY_LOGIN_FAIL_MESSAGE,
      });
    }

    let profile: BackendUserProfile | null = null;
    let profileFallback = false;
    try {
      const profileAuthHeaders = this.buildBackendAccessTokenHeaders(authData.accessToken)
        ?? this.buildInternalUserAuthHeaders(profileUserKey);
      const profileResp = await this.backendHttpService.get<{ data?: Record<string, unknown> }>(
        `/api/users/${encodeURIComponent(profileUserKey)}/profile`,
        {
          headers: profileAuthHeaders,
        },
      );
      profile = (profileResp.data?.data ?? null) as BackendUserProfile | null;
    } catch {
      profileFallback = true;
    }

    const user = this.buildLoginUserEnvelope(authData, normalizedLoginId, profileUserKey, profile);
    if (timeoutMs == null) {
      this.triggerAiCreditAutoOpenInBackground(profileUserKey);
    }
    const backendAccessToken = this.normalizeOptionalText(authData.accessToken);
    const signedAccessToken = backendAccessToken ?? buildSignedAccessToken(profileUserKey, {
      deviceId: normalizedDeviceId,
      expiresInSeconds: authData.expiresInSeconds,
    });
    const tokenType = this.normalizeOptionalText(authData.tokenType) ?? 'Bearer';
    return {
      accessToken: signedAccessToken,
      tokenType,
      expiresInSeconds: authData.expiresInSeconds,
      user,
      fallback: {
        profileFallback,
      },
    };
  }

  async demoAutoLogin(
    deviceId?: string,
    preferredLoginId?: string,
    legacyDeviceIdsHeader?: string,
  ): Promise<Record<string, unknown>> {
    const normalizedDeviceId = this.normalizeDeviceId(deviceId);
    const normalizedPreferredLoginId = this.normalizeMainlandPhoneLoginId(preferredLoginId);

    if (normalizedPreferredLoginId) {
      const preferredSession = await this.tryDemoLoginSafely(
        normalizedPreferredLoginId,
        normalizedDeviceId,
        undefined,
        legacyDeviceIdsHeader,
      );
      if (preferredSession && this.isDemoAccountSession(preferredSession)) {
        this.rememberDemoDeviceLoginId(normalizedDeviceId, normalizedPreferredLoginId);
        this.triggerDemoInitializationInBackground(preferredSession, normalizedPreferredLoginId);
        return this.attachDemoMeta(preferredSession, normalizedPreferredLoginId, true);
      }
      if (preferredSession) {
        // 用户主动指定了演示登录手机号且认证通过时，直接返回会话。
        // 避免因账号来源不是 DEMO 而回退到初始化分支，导致真机演示自动登录失败。
        this.rememberDemoDeviceLoginId(normalizedDeviceId, normalizedPreferredLoginId);
        return this.attachDemoMeta(preferredSession, normalizedPreferredLoginId, true);
      }
    }

    const rememberedLoginId = normalizedDeviceId ? this.demoLoginIdByDeviceId.get(normalizedDeviceId) : undefined;
    if (rememberedLoginId && rememberedLoginId !== normalizedPreferredLoginId) {
      const rememberedSession = await this.tryDemoLoginSafely(
        rememberedLoginId,
        normalizedDeviceId,
        undefined,
        legacyDeviceIdsHeader,
      );
      if (rememberedSession && this.isDemoAccountSession(rememberedSession)) {
        this.rememberDemoDeviceLoginId(normalizedDeviceId, rememberedLoginId);
        this.triggerDemoInitializationInBackground(rememberedSession, rememberedLoginId);
        return this.attachDemoMeta(rememberedSession, rememberedLoginId, true);
      }
      if (rememberedSession) {
        this.logger.warn(
          `[演示账号自动登录]忽略非演示账号, loginId:${rememberedLoginId}, accountSource:${this.resolveSessionAccountSource(rememberedSession) ?? 'UNKNOWN'}`,
        );
      }
    }

    if (normalizedDeviceId) {
      const deterministicLogin = await this.tryDeterministicDeviceDemoLogin(normalizedDeviceId, legacyDeviceIdsHeader);
      if (deterministicLogin) {
        this.rememberDemoDeviceLoginId(normalizedDeviceId, deterministicLogin.loginId);
        this.triggerDemoInitializationInBackground(deterministicLogin.session, deterministicLogin.loginId);
        return this.attachDemoMeta(deterministicLogin.session, deterministicLogin.loginId, true);
      }
    }

    const lockKey = normalizedDeviceId ?? normalizedPreferredLoginId ?? AuthService.DEMO_DEFAULT_DEVICE_KEY;
    const existingLock = this.demoLoginProvisionLocks.get(lockKey);
    if (existingLock) {
      return existingLock;
    }

    const provisioningTask = this.provisionDemoAccountAndLogin(normalizedDeviceId, legacyDeviceIdsHeader);
    this.demoLoginProvisionLocks.set(lockKey, provisioningTask);
    try {
      return await provisioningTask;
    } finally {
      this.demoLoginProvisionLocks.delete(lockKey);
    }
  }

  async listPresetLoginAccounts(deviceId: string, legacyDeviceIdsHeader?: string): Promise<Array<Record<string, string>>> {
    const normalizedDeviceId = this.normalizeDeviceId(deviceId);
    if (!normalizedDeviceId) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'deviceId 不能为空',
      });
    }

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendLoginPresetAccount[]>>(
        `/api/auth/preset-login-accounts?deviceId=${encodeURIComponent(normalizedDeviceId)}`,
        {
          headers: this.buildForwardedDeviceHeaders(normalizedDeviceId, legacyDeviceIdsHeader),
        },
      );
      const source = response.data?.data;
      if (!Array.isArray(source)) {
        return [];
      }

      const deduplicated = new Map<string, string>();
      source.forEach((item) => {
        const loginId = this.normalizeMainlandPhoneLoginId(item?.loginId);
        if (!loginId || deduplicated.has(loginId)) {
          return;
        }
        const nickname = this.normalizeOptionalText(item?.nickname) ?? loginId;
        deduplicated.set(loginId, nickname);
      });
      return Array.from(deduplicated.entries()).map(([loginId, nickname]) => ({ loginId, nickname }));
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询登录下拉账号失败');
    }
  }

  private async provisionDemoAccountAndLogin(deviceId?: string, legacyDeviceIdsHeader?: string): Promise<Record<string, unknown>> {
    const demoConfig = await this.getDemoProvisioningConfig();
    const templateProfile = await this.fetchProfileByLoginId(
      demoConfig.demoTemplateLoginId,
      '读取演示模板账号失败',
    );

    const registration = await this.registerDemoUser(
      templateProfile,
      deviceId,
      demoConfig.demoLoginPassword,
      legacyDeviceIdsHeader,
    );
    const session = await this.mobileVerifyLogin(
      registration.loginId,
      deviceId ?? '',
      undefined,
      legacyDeviceIdsHeader,
    );
    this.rememberDemoDeviceLoginId(deviceId, registration.loginId);
    this.triggerDemoInitializationInBackground(session, registration.loginId);
    return this.attachDemoMeta(session, registration.loginId, false);
  }

  private triggerDemoInitializationInBackground(session: Record<string, unknown>, loginId: string): void {
    const normalizedLoginId = this.normalizeMainlandPhoneLoginId(loginId);
    if (!normalizedLoginId || this.demoInitializedLoginIds.has(normalizedLoginId)) {
      return;
    }

    const existingTask = this.demoInitializationLocks.get(normalizedLoginId);
    if (existingTask) {
      return;
    }

    const targetUserId = this.resolveSessionUserId(session);
    if (!targetUserId) {
      return;
    }

    const task = Promise.resolve()
      .then(() => this.runDemoInitializationWorkflow(targetUserId, normalizedLoginId))
      .then(() => {
        this.demoInitializedLoginIds.add(normalizedLoginId);
      })
      .catch((error: unknown) => {
        this.logger.warn(
          `[演示账号自动登录]后台初始化失败, loginId:${normalizedLoginId}, userId:${targetUserId}, error:${this.extractErrorMessage(error) || 'unknown'}`,
        );
      })
      .finally(() => {
        this.demoInitializationLocks.delete(normalizedLoginId);
      });

    this.demoInitializationLocks.set(normalizedLoginId, task);
    void task;
  }

  private resolveSessionUserId(session: Record<string, unknown>): string | null {
    const user = (session.user ?? null) as Record<string, unknown> | null;
    const normalized = this.normalizeNumericIdentifier(
      (user?.userId ?? null) as string | number | null | undefined,
    );
    return normalized ?? null;
  }

  private async runDemoInitializationWorkflow(targetUserId: string, targetLoginId: string): Promise<void> {
    const demoConfig = await this.getDemoProvisioningConfig();
    const templateProfile = await this.fetchProfileByLoginId(
      demoConfig.demoTemplateLoginId,
      '读取演示模板账号失败',
    );

    const contactProfile = await this.fetchProfileByLoginId(
      demoConfig.demoContactLoginId,
      '读取演示联系人账号失败',
    );
    const contactUserId = this.resolveRequiredUserId(contactProfile, '读取演示联系人账号失败');

    await this.ensureDemoBankCards(targetUserId, targetLoginId, templateProfile);
    await this.grantDemoMobileTopUpCoupons(targetUserId);
    await this.ensureDemoFriendConversation(targetUserId, contactUserId);
  }

  private async fetchProfileByLoginId(loginId: string, fallbackMessage: string): Promise<BackendUserProfile> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendUserProfile>>(
        `/api/users/profile-by-login?loginId=${encodeURIComponent(loginId)}`,
      );
      const profile = response.data?.data;
      if (!profile) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: fallbackMessage,
        });
      }
      return profile;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, fallbackMessage);
    }
  }

  private async registerDemoUser(
    templateProfile: BackendUserProfile,
    deviceSeed: string | undefined,
    demoLoginPassword: string,
    legacyDeviceIdsHeader?: string,
  ): Promise<{ userId: string; loginId: string }> {
    const avatarUrl = this.normalizeOptionalText(templateProfile.avatarUrl);
    const kycIdentity = this.resolveTemplateKycIdentity(templateProfile);
    const nickname = this.generateDemoWechatNickname();
    const normalizedDeviceSeed = this.normalizeDeviceId(deviceSeed);
    if (!kycIdentity) {
      throw new BadGatewayException({
        code: 'DEMO_AUTO_LOGIN_INIT_FAILED',
        message: '演示模板账号缺少实名信息，请补齐后重试',
      });
    }

    let lastError: unknown = null;
    for (let attempt = 0; attempt < AuthService.DEMO_MAX_REGISTER_RETRY; attempt += 1) {
      const loginId = this.generateDeterministicMainlandPhoneLoginId(deviceSeed, attempt);
      const requestPayload: Record<string, unknown> = {
        deviceId: normalizedDeviceSeed,
        legacyDeviceIds: this.parseLegacyDeviceIds(legacyDeviceIdsHeader, normalizedDeviceSeed),
        loginId,
        userTypeCode: '01',
        accountSource: AuthService.DEMO_ACCOUNT_SOURCE,
        nickname,
        countryCode: '86',
        mobile: loginId,
        loginPassword: demoLoginPassword,
        realName: kycIdentity.realName,
        idCardNo: kycIdentity.idCardNo,
      };
      if (avatarUrl) {
        requestPayload.avatarUrl = avatarUrl;
      }

      try {
        const response = await this.backendHttpService.post<BackendEnvelope<BackendRegistration>, Record<string, unknown>>(
          '/api/user-flows/registrations',
          requestPayload,
        );
        const registration = response.data?.data;
        const userId = this.normalizeNumericIdentifier(registration?.userId);
        if (!registration || !userId) {
          throw new BadGatewayException({
            code: 'UPSTREAM_EMPTY_RESPONSE',
            message: '上游服务未返回演示账号注册结果',
          });
        }
        return {
          userId,
          loginId: this.firstNonEmpty(registration.loginId, loginId),
        };
      } catch (error) {
        if (this.isDuplicatedLoginIdError(error)) {
          continue;
        }
        lastError = error;
        break;
      }
    }

    if (lastError) {
      this.rethrowMappedUpstreamError(lastError, '创建演示账号失败');
    }

    throw new BadGatewayException({
      code: 'DEMO_AUTO_LOGIN_INIT_FAILED',
      message: '创建演示账号失败，请稍后重试',
    });
  }

  private async ensureDemoBankCards(
    targetUserId: string,
    targetLoginId: string,
    templateProfile: BackendUserProfile,
  ): Promise<void> {
    const targetAuthHeaders = this.buildInternalUserAuthHeaders(targetUserId);
    let existingCards: BackendBankCard[] = [];
    try {
      const existingTargetCardsResponse = await this.backendHttpService.get<BackendEnvelope<BackendBankCard[]>>(
        `/api/bankcards/users/${encodeURIComponent(targetUserId)}/active`,
        {
          headers: targetAuthHeaders,
        },
      );
      existingCards = Array.isArray(existingTargetCardsResponse.data?.data)
        ? existingTargetCardsResponse.data?.data ?? []
        : [];
      if (existingCards.length > 0) {
        return;
      }
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '初始化演示银行卡失败');
    }

    const holderName = this.resolveBankCardHolderName(templateProfile);
    const existingCardNos = new Set<string>();
    const existingBankCodesByType: Record<DemoCardType, Set<string>> = {
      DEBIT: new Set<string>(),
      CREDIT: new Set<string>(),
    };
    const defaultCardState = {
      hasDefaultCard: false,
    };
    existingCards.forEach((card) => {
      const normalizedCardNo = this.normalizeCardNo(card.cardNo);
      if (normalizedCardNo) {
        existingCardNos.add(normalizedCardNo);
      }

      const cardType = this.normalizeCardType(card.cardType);
      if (!cardType) {
        return;
      }
      if (card.defaultCard === true) {
        defaultCardState.hasDefaultCard = true;
      }
      const bankCode = this.normalizeOptionalText(card.bankCode)?.toUpperCase();
      if (!bankCode) {
        return;
      }
      existingBankCodesByType[cardType].add(bankCode);
    });

    await this.ensureDemoBankCardsByType(
      targetUserId,
      targetLoginId,
      holderName,
      'DEBIT',
      AuthService.DEMO_TARGET_DEBIT_CARD_COUNT,
      targetAuthHeaders,
      existingCardNos,
      existingBankCodesByType.DEBIT,
      defaultCardState,
    );
    await this.ensureDemoBankCardsByType(
      targetUserId,
      targetLoginId,
      holderName,
      'CREDIT',
      AuthService.DEMO_TARGET_CREDIT_CARD_COUNT,
      targetAuthHeaders,
      existingCardNos,
      existingBankCodesByType.CREDIT,
      defaultCardState,
    );
  }

  private async ensureDemoBankCardsByType(
    targetUserId: string,
    targetLoginId: string,
    holderName: string,
    cardType: DemoCardType,
    targetCount: number,
    targetAuthHeaders: Record<string, string>,
    existingCardNos: Set<string>,
    existingBankCodes: Set<string>,
    defaultCardState: { hasDefaultCard: boolean },
  ): Promise<void> {
    if (existingBankCodes.size >= targetCount) {
      return;
    }

    for (const bank of AuthService.DEMO_BANK_CATALOG) {
      if (existingBankCodes.has(bank.bankCode)) {
        continue;
      }

      const bindResult = await this.tryBindDemoBankCard(
        targetUserId,
        targetLoginId,
        holderName,
        bank,
        cardType,
        targetAuthHeaders,
        existingCardNos,
        defaultCardState,
      );
      if (!bindResult) {
        continue;
      }
      existingBankCodes.add(bank.bankCode);
      if (existingBankCodes.size >= targetCount) {
        return;
      }
    }

    throw new BadGatewayException({
      code: 'DEMO_AUTO_LOGIN_INIT_FAILED',
      message: '初始化演示银行卡失败，请稍后重试',
    });
  }

  private async tryBindDemoBankCard(
    targetUserId: string,
    targetLoginId: string,
    holderName: string,
    bank: DemoBankCatalogEntry,
    cardType: DemoCardType,
    targetAuthHeaders: Record<string, string>,
    existingCardNos: Set<string>,
    defaultCardState: { hasDefaultCard: boolean },
  ): Promise<boolean> {
    for (let attempt = 0; attempt < AuthService.DEMO_MAX_BIND_CARD_RETRY; attempt += 1) {
      const cardNo = this.generateDemoBankCardNo(targetUserId, targetLoginId, bank, cardType, attempt);
      if (existingCardNos.has(cardNo)) {
        continue;
      }
      const defaultCard = !defaultCardState.hasDefaultCard && cardType === 'DEBIT';
      const requestPayload: Record<string, unknown> = {
        userId: targetUserId,
        cardNo,
        bankCode: bank.bankCode,
        bankName: bank.bankName,
        cardType,
        cardHolderName: holderName,
        reservedMobile: targetLoginId,
        phoneTailNo: targetLoginId.slice(-4),
        defaultCard,
      };
      try {
        await this.backendHttpService.post<BackendEnvelope<BackendBankCard>, Record<string, unknown>>(
          '/api/bankcards',
          requestPayload,
          {
            headers: targetAuthHeaders,
          },
        );
        existingCardNos.add(cardNo);
        if (defaultCard) {
          defaultCardState.hasDefaultCard = true;
        }
        return true;
      } catch (error) {
        if (this.isBankCardAlreadyExistsError(error)) {
          existingCardNos.add(cardNo);
          continue;
        }
        this.rethrowMappedUpstreamError(error, '初始化演示银行卡失败');
      }
    }
    return false;
  }

  private generateDemoBankCardNo(
    targetUserId: string,
    targetLoginId: string,
    bank: DemoBankCatalogEntry,
    cardType: DemoCardType,
    attempt: number,
  ): string {
    const prefix = (cardType === 'DEBIT' ? bank.debitPrefix : bank.creditPrefix).slice(0, 6).padEnd(6, '0');
    const middleSeed = `${targetUserId}:${targetLoginId}:${bank.bankCode}:${cardType}:${attempt}`;
    const middleDigits = this.deriveDeterministicDigits(middleSeed, 12);
    const body = `${prefix}${middleDigits}`;
    const checkDigit = this.calculateLuhnCheckDigit(body);
    return `${body}${checkDigit}`;
  }

  private calculateLuhnCheckDigit(body: string): string {
    let sum = 0;
    let shouldDouble = true;
    for (let index = body.length - 1; index >= 0; index -= 1) {
      const digit = Number(body.charAt(index));
      if (!Number.isFinite(digit)) {
        continue;
      }
      let contribution = digit;
      if (shouldDouble) {
        contribution *= 2;
        if (contribution > 9) {
          contribution -= 9;
        }
      }
      sum += contribution;
      shouldDouble = !shouldDouble;
    }
    return String((10 - (sum % 10)) % 10);
  }

  private normalizeCardType(raw: string | undefined): DemoCardType | null {
    const normalized = this.normalizeOptionalText(raw)?.toUpperCase();
    if (normalized === 'DEBIT' || normalized === 'CREDIT') {
      return normalized;
    }
    return null;
  }

  private async grantDemoMobileTopUpCoupons(userId: string): Promise<void> {
    try {
      const authHeaders = this.buildInternalUserAuthHeaders(userId);
      await this.backendHttpService.post<BackendEnvelope<Record<string, unknown>>, Record<string, unknown>>(
        '/api/coupons/mobile-topup-reward/demo-auto-login/grant',
        {
          userId,
          count: AuthService.DEMO_COUPON_COUNT,
        },
        {
          headers: authHeaders,
        },
      );
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '发放演示红包失败');
    }
  }

  private async ensureDemoFriendConversation(targetUserId: string, contactUserId: string): Promise<void> {
    let requestNo: string | null = null;
    let shouldSendWelcomeMessage = false;
    let conversationNo: string | null = null;

    try {
      const requesterAuthHeaders = this.buildInternalUserAuthHeaders(targetUserId);
      const applyResponse = await this.backendHttpService.post<BackendEnvelope<BackendContactRequest>, Record<string, unknown>>(
        '/api/contacts/requests',
        {
          requesterUserId: targetUserId,
          targetUserId: contactUserId,
          applyMessage: '演示账号自动初始化',
        },
        {
          headers: requesterAuthHeaders,
        },
      );
      requestNo = this.normalizeOptionalText(applyResponse.data?.data?.requestNo) ?? null;
      shouldSendWelcomeMessage = requestNo != null;
    } catch (error) {
      if (!this.isAlreadyFriendError(error)) {
        this.rethrowMappedUpstreamError(error, '初始化演示联系人失败');
      }
    }

    if (requestNo) {
      try {
        const operatorAuthHeaders = this.buildInternalUserAuthHeaders(contactUserId);
        await this.backendHttpService.post<BackendEnvelope<Record<string, unknown>>, Record<string, unknown>>(
          `/api/contacts/requests/${encodeURIComponent(requestNo)}/handle`,
          {
            operatorUserId: contactUserId,
            action: 'ACCEPT',
          },
          {
            headers: operatorAuthHeaders,
          },
        );
      } catch (error) {
        if (!this.isRequestAlreadyHandledError(error) && !this.isAlreadyFriendError(error)) {
          this.rethrowMappedUpstreamError(error, '初始化演示联系人失败');
        }
      }
    }

    try {
      const conversationAuthHeaders = this.buildInternalUserAuthHeaders(targetUserId);
      const openConversationResponse = await this.backendHttpService.post<BackendEnvelope<BackendConversation>, Record<string, unknown>>(
        '/api/conversations/private/open',
        {
          userId: targetUserId,
          peerUserId: contactUserId,
        },
        {
          headers: conversationAuthHeaders,
        },
      );
      conversationNo = this.normalizeOptionalText(openConversationResponse.data?.data?.conversationNo) ?? null;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '初始化演示会话失败');
    }

    if (!shouldSendWelcomeMessage) {
      return;
    }

    if (conversationNo) {
      const hasExistingWelcomeMessage = await this.hasExistingDemoWelcomeMessage(
        conversationNo,
        targetUserId,
        contactUserId,
      );
      if (hasExistingWelcomeMessage) {
        return;
      }
    }

    try {
      const senderAuthHeaders = this.buildInternalUserAuthHeaders(contactUserId);
      await this.backendHttpService.post<BackendEnvelope<Record<string, unknown>>, Record<string, unknown>>(
        '/api/messages/text',
        {
          senderUserId: contactUserId,
          receiverUserId: targetUserId,
          contentText: AuthService.DEMO_WELCOME_MESSAGE,
        },
        {
          headers: senderAuthHeaders,
        },
      );
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '初始化演示消息失败');
    }
  }

  private async hasExistingDemoWelcomeMessage(
    conversationNo: string,
    targetUserId: string,
    contactUserId: string,
  ): Promise<boolean> {
    const normalizedConversationNo = this.normalizeOptionalText(conversationNo);
    if (!normalizedConversationNo) {
      return false;
    }

    try {
      const authHeaders = this.buildInternalUserAuthHeaders(targetUserId);
      const response = await this.backendHttpService.get<BackendEnvelope<BackendMessage[]>>(
        `/api/messages/conversations/${encodeURIComponent(normalizedConversationNo)}?userId=${encodeURIComponent(targetUserId)}&limit=120`,
        {
          headers: authHeaders,
        },
      );
      const messages = Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
      return messages.some((message) => {
        const senderUserId = this.normalizeNumericIdentifier(message.senderUserId);
        const contentText = this.normalizeOptionalText(message.contentText);
        return senderUserId === contactUserId && contentText === AuthService.DEMO_WELCOME_MESSAGE;
      });
    } catch (error) {
      this.logger.warn(
        `[演示账号自动登录]校验欢迎消息失败, conversationNo:${normalizedConversationNo}, userId:${targetUserId}, error:${this.extractErrorMessage(error) || 'unknown'}`,
      );
      return false;
    }
  }

  private async tryDemoLogin(
    loginId: string,
    deviceId?: string,
    timeoutMs?: number,
    legacyDeviceIdsHeader?: string,
  ): Promise<Record<string, unknown> | null> {
    try {
      return await this.mobileVerifyLogin(loginId, deviceId ?? '', timeoutMs, legacyDeviceIdsHeader);
    } catch (error) {
      if (
        error instanceof UnauthorizedException
        || error instanceof BadRequestException
      ) {
        return null;
      }
      throw error;
    }
  }

  private async tryDemoLoginSafely(
    loginId: string,
    deviceId?: string,
    timeoutMs?: number,
    legacyDeviceIdsHeader?: string,
  ): Promise<Record<string, unknown> | null> {
    try {
      return await this.tryDemoLogin(loginId, deviceId, timeoutMs, legacyDeviceIdsHeader);
    } catch (error) {
      this.logger.warn(
        `[演示账号自动登录]跳过候选账号登录探测失败, loginId:${loginId}, error:${this.extractErrorMessage(error) || 'unknown'}`,
      );
      return null;
    }
  }

  private attachDemoMeta(session: Record<string, unknown>, demoLoginId: string, initialized: boolean): Record<string, unknown> {
    const user = (session.user ?? null) as Record<string, unknown> | null;
    const resolvedNickname = this.generateDemoWechatNickname();
    const patchedUser = user
      ? {
          ...user,
          nickname: resolvedNickname,
        }
      : user;
    return {
      ...session,
      user: patchedUser,
      demo: {
        loginId: demoLoginId,
        initialized,
      },
    };
  }

  private generateDemoWechatNickname(): string {
    const nickname = this.pickRandom(AuthService.DEMO_WECHAT_NICKNAMES);
    return nickname || 'momo';
  }

  private pickRandom(values: ReadonlyArray<string>): string {
    if (values.length === 0) {
      return '';
    }
    const index = Math.floor(Math.random() * values.length);
    return values[index];
  }

  private isDemoAccountSession(session: Record<string, unknown>): boolean {
    return this.resolveSessionAccountSource(session) === AuthService.DEMO_ACCOUNT_SOURCE;
  }

  private resolveSessionAccountSource(session: Record<string, unknown>): string | null {
    const user = (session.user ?? null) as Record<string, unknown> | null;
    const normalized = this.normalizeOptionalText((user?.accountSource ?? null) as string | null | undefined);
    if (!normalized) {
      return null;
    }
    return normalized.toUpperCase();
  }

  private rememberDemoDeviceLoginId(deviceId: string | undefined, loginId: string): void {
    const normalizedDeviceId = this.normalizeDeviceId(deviceId);
    if (!normalizedDeviceId) {
      return;
    }
    this.demoLoginIdByDeviceId.set(normalizedDeviceId, loginId);
  }

  private resolveTemplateKycIdentity(templateProfile: BackendUserProfile): { realName: string; idCardNo: string } | null {
    const candidateNames = [templateProfile.maskedRealName, templateProfile.nickname];
    let resolvedName: string | null = null;
    for (const candidateName of candidateNames) {
      const normalized = this.normalizeOptionalText(candidateName);
      if (!normalized) {
        continue;
      }
      const stripped = normalized.replace(/\s+/g, '').replace(/\*/g, '');
      if (stripped.length >= 2) {
        resolvedName = stripped;
        break;
      }
    }

    const idCardNo = this.normalizeOptionalText(templateProfile.idCardNo)?.toUpperCase() ?? null;
    if (!resolvedName || !idCardNo) {
      return null;
    }
    if (!this.isValidMainlandIdCardNo(idCardNo)) {
      return null;
    }
    return {
      realName: resolvedName,
      idCardNo,
    };
  }

  private resolveDemoNickname(sourceNickname: unknown): string {
    const normalizedSourceNickname = this.normalizeOptionalText(sourceNickname) ?? '演示用户';
    return `${normalizedSourceNickname}演示`;
  }

  private resolveBankCardHolderName(templateProfile: BackendUserProfile): string {
    const candidates = [templateProfile.maskedRealName, templateProfile.nickname];
    for (const candidate of candidates) {
      const normalized = this.normalizeOptionalText(candidate);
      if (!normalized) {
        continue;
      }
      const stripped = normalized.replace(/\*/g, '').trim();
      if (stripped.length >= 2) {
        return stripped;
      }
    }
    return AuthService.DEMO_CARD_HOLDER_FALLBACK;
  }

  private normalizeMoneyLikePayload(raw: BackendMoney | undefined): Record<string, string> | null {
    if (!raw || typeof raw !== 'object') {
      return null;
    }
    const amount = this.normalizeDecimalText(raw.amount);
    if (!amount) {
      return null;
    }
    const currencyCode = this.normalizeOptionalText(raw.currencyCode)
      ?? this.normalizeOptionalText(raw.currencyUnit?.code)
      ?? 'CNY';
    return {
      amount,
      currencyCode: currencyCode.toUpperCase(),
    };
  }

  private generateDeterministicMainlandPhoneLoginId(deviceSeed: string | undefined, attempt: number): string {
    const normalizedSeed = this.normalizeOptionalText(deviceSeed) ?? AuthService.DEMO_DEFAULT_DEVICE_KEY;
    const prefixes = [
      '133', '135', '136', '137', '138', '139',
      '150', '151', '152', '157', '158', '159',
      '166', '167', '170', '171', '172', '173',
      '175', '176', '177', '178', '180', '181',
      '182', '183', '184', '185', '186', '187',
      '188', '189', '198', '199',
    ];
    const prefixSeed = `${normalizedSeed}:prefix`;
    const prefixDigits = this.deriveDeterministicDigits(prefixSeed, 2);
    const prefixIndex = Number(prefixDigits) % prefixes.length;
    const suffixSeed = `${normalizedSeed}:attempt:${attempt}`;
    const suffixDigits = this.deriveDeterministicDigits(suffixSeed, 8);
    return `${prefixes[prefixIndex]}${suffixDigits.slice(0, 8)}`;
  }

  private generateDemoCardNo(sourceCardNo: string | null, index: number, attempt: number): string {
    const normalizedSourceCardNo = sourceCardNo ?? '';
    const length = Math.max(12, Math.min(32, normalizedSourceCardNo.length || 19));
    const rawPrefix = normalizedSourceCardNo.length >= 6
      ? normalizedSourceCardNo.slice(0, 6)
      : AuthService.DEMO_CARD_PREFIX;
    const prefix = rawPrefix.padEnd(6, '0').slice(0, 6);
    const middleLength = Math.max(4, length - prefix.length - 1);
    const seed = `${Date.now()}${Math.floor(Math.random() * 1_000_000)}${index}${attempt}`;
    let middle = '';
    for (let i = 0; i < middleLength; i += 1) {
      middle += seed.charAt((i + attempt) % seed.length);
    }
    const tailSeed = Number(seed.charAt((index + attempt) % seed.length));
    const tail = Number.isFinite(tailSeed) ? String(tailSeed) : '0';
    return `${prefix}${middle}${tail}`.slice(0, length);
  }

  private normalizeCardNo(raw: string | undefined): string | null {
    if (typeof raw !== 'string') {
      return null;
    }
    const normalized = raw.trim().replace(/\s+/g, '');
    if (!/^\d{12,32}$/.test(normalized)) {
      return null;
    }
    return normalized;
  }

  private deriveDeterministicDigits(seed: string, requiredLength: number): string {
    let digits = '';
    let round = 0;
    while (digits.length < requiredLength) {
      const digest = createHash('sha256')
        .update(`${seed}#${round}`)
        .digest('hex');
      for (const char of digest) {
        digits += String(parseInt(char, 16) % 10);
        if (digits.length >= requiredLength) {
          break;
        }
      }
      round += 1;
    }
    return digits;
  }

  private async tryDeterministicDeviceDemoLogin(
    deviceId: string,
    legacyDeviceIdsHeader?: string,
  ): Promise<{ loginId: string; session: Record<string, unknown> } | null> {
    for (let attempt = 0; attempt < AuthService.DEMO_DEVICE_LOGIN_PROBE_COUNT; attempt += 1) {
      const loginId = this.generateDeterministicMainlandPhoneLoginId(deviceId, attempt);
      const session = await this.tryDemoLoginSafely(
        loginId,
        deviceId,
        AuthService.DEMO_LOGIN_PROBE_TIMEOUT_MS,
        legacyDeviceIdsHeader,
      );
      if (session && this.isDemoAccountSession(session)) {
        return { loginId, session };
      }
      if (session) {
        this.logger.warn(
          `[演示账号自动登录]忽略非演示账号, loginId:${loginId}, accountSource:${this.resolveSessionAccountSource(session) ?? 'UNKNOWN'}`,
        );
      }
    }
    return null;
  }

  private async getDemoProvisioningConfig(): Promise<{ demoTemplateLoginId: string; demoContactLoginId: string; demoLoginPassword: string }> {
    const now = Date.now();
    if (this.cachedDemoProvisioningConfig && this.cachedDemoProvisioningConfig.expiresAt > now) {
      return this.cachedDemoProvisioningConfig.value;
    }

    const internalToken = this.normalizeOptionalText(process.env.OPENAIPAY_INTERNAL_CONFIG_TOKEN);
    if (!internalToken) {
      throw new BadGatewayException({
        code: 'DEMO_AUTO_LOGIN_INIT_FAILED',
        message: '缺少 OPENAIPAY_INTERNAL_CONFIG_TOKEN，无法读取演示账号配置',
      });
    }

    let payload: BackendDemoProvisioningConfig | null = null;
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendDemoProvisioningConfig>>(
        `/api/apps/${encodeURIComponent(AuthService.DEMO_CONFIG_APP_CODE)}/demo-provisioning-config`,
        {
          headers: {
            'X-Internal-Token': internalToken,
          },
        },
      );
      payload = response.data?.data ?? null;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '读取演示账号配置失败');
    }

    const demoTemplateLoginId = this.normalizeMainlandPhoneLoginId(payload?.demoTemplateLoginId);
    const demoContactLoginId = this.normalizeMainlandPhoneLoginId(payload?.demoContactLoginId);
    const demoLoginPassword = this.normalizeOptionalText(payload?.demoLoginPassword);
    if (!demoTemplateLoginId || !demoContactLoginId || !demoLoginPassword) {
      throw new BadGatewayException({
        code: 'DEMO_AUTO_LOGIN_INIT_FAILED',
        message: '演示账号配置不完整，请在管理后台应用设置中补齐模板号/联系人号/默认密码',
      });
    }

    const resolved = { demoTemplateLoginId, demoContactLoginId, demoLoginPassword };
    this.cachedDemoProvisioningConfig = {
      value: resolved,
      expiresAt: now + AuthService.DEMO_CONFIG_CACHE_TTL_MS,
    };
    return resolved;
  }

  private isDuplicatedLoginIdError(error: unknown): boolean {
    const message = this.extractErrorMessage(error).toLowerCase();
    return message.includes('already exists')
      || message.includes('duplicate')
      || message.includes('注册失败')
      || message.includes('请检查信息后重试或直接登录');
  }

  private isBankCardAlreadyExistsError(error: unknown): boolean {
    const message = this.extractErrorMessage(error).toLowerCase();
    return message.includes('bank card already exists')
      || message.includes('duplicate entry')
      || message.includes('已存在');
  }

  private isAlreadyFriendError(error: unknown): boolean {
    const message = this.extractErrorMessage(error).toLowerCase();
    return message.includes('already friend');
  }

  private isRequestAlreadyHandledError(error: unknown): boolean {
    const message = this.extractErrorMessage(error).toLowerCase();
    return message.includes('request status does not allow')
      || message.includes('not pending')
      || message.includes('已处理');
  }

  private extractErrorMessage(error: unknown): string {
    if (error instanceof AxiosError) {
      const body = error.response?.data as BackendEnvelope<unknown> | undefined;
      return (body?.error?.message ?? error.message ?? '').trim();
    }
    if (typeof error === 'object' && error !== null && 'message' in error) {
      const maybeMessage = (error as { message?: unknown }).message;
      if (typeof maybeMessage === 'string') {
        return maybeMessage.trim();
      }
    }
    return '';
  }

  private normalizeDeviceId(raw: string | undefined): string | undefined {
    const normalized = this.normalizeOptionalText(raw);
    if (!normalized) {
      return undefined;
    }
    return normalized.slice(0, 128);
  }

  private normalizeLegacyDeviceIdsHeader(raw: string | undefined, primaryDeviceId?: string): string | undefined {
    const normalized = this.normalizeOptionalText(raw);
    if (!normalized) {
      return undefined;
    }
    const deduplicated = new Set<string>();
    normalized.split(',').forEach((item) => {
      const candidate = this.normalizeDeviceId(item);
      if (!candidate || candidate === primaryDeviceId) {
        return;
      }
      deduplicated.add(candidate);
    });
    const values = Array.from(deduplicated.values());
    return values.length > 0 ? values.join(',') : undefined;
  }

  private parseLegacyDeviceIds(raw: string | undefined, primaryDeviceId?: string): string[] {
    const normalizedHeader = this.normalizeLegacyDeviceIdsHeader(raw, primaryDeviceId);
    if (!normalizedHeader) {
      return [];
    }
    return normalizedHeader.split(',').map((item) => item.trim()).filter((item) => item.length > 0);
  }

  private buildForwardedDeviceHeaders(
    deviceId?: string,
    legacyDeviceIdsHeader?: string,
  ): Record<string, string> | undefined {
    const headers: Record<string, string> = {};
    const normalizedDeviceId = this.normalizeDeviceId(deviceId);
    const normalizedLegacyDeviceIdsHeader = this.normalizeLegacyDeviceIdsHeader(legacyDeviceIdsHeader, normalizedDeviceId);
    if (normalizedDeviceId) {
      headers['X-Device-Id'] = normalizedDeviceId;
    }
    if (normalizedLegacyDeviceIdsHeader) {
      headers['X-Legacy-Device-Ids'] = normalizedLegacyDeviceIdsHeader;
    }
    return Object.keys(headers).length > 0 ? headers : undefined;
  }

  private normalizeMainlandPhoneLoginId(raw: string | undefined): string | undefined {
    if (typeof raw !== 'string') {
      return undefined;
    }
    const normalized = raw.trim();
    if (!normalized) {
      return undefined;
    }
    const asciiDigits = Array.from(normalized)
      .map((char) => {
        const code = char.charCodeAt(0);
        if (code >= 0xff10 && code <= 0xff19) {
          return String.fromCharCode(code - 0xff10 + 0x30);
        }
        return char;
      })
      .join('')
      .replace(/[^0-9]/g, '');
    let digits = asciiDigits;
    if (digits.length === 13 && digits.startsWith('86')) {
      digits = digits.slice(2);
    } else if (digits.length === 15 && digits.startsWith('0086')) {
      digits = digits.slice(4);
    }
    if (!/^1[3-9]\d{9}$/.test(digits)) {
      return undefined;
    }
    return digits;
  }

  private isValidMainlandIdCardNo(idCardNo: string): boolean {
    if (!/^\d{17}[0-9X]$/.test(idCardNo)) {
      return false;
    }
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    const checks = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
    let sum = 0;
    for (let index = 0; index < 17; index += 1) {
      sum += Number(idCardNo.charAt(index)) * weights[index];
    }
    if (checks[sum % 11] !== idCardNo.charAt(17)) {
      return false;
    }
    const birthdayText = idCardNo.slice(6, 14);
    if (!/^\d{8}$/.test(birthdayText)) {
      return false;
    }
    const year = Number(birthdayText.slice(0, 4));
    const month = Number(birthdayText.slice(4, 6));
    const day = Number(birthdayText.slice(6, 8));
    if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(day)) {
      return false;
    }
    const birthDate = new Date(year, month - 1, day);
    if (
      birthDate.getFullYear() !== year
      || birthDate.getMonth() !== month - 1
      || birthDate.getDate() !== day
    ) {
      return false;
    }
    return birthDate.getTime() <= Date.now();
  }

  private normalizeDecimalText(raw: unknown): string | null {
    if (typeof raw === 'number') {
      if (!Number.isFinite(raw)) {
        return null;
      }
      return raw.toFixed(2);
    }
    if (typeof raw !== 'string') {
      return null;
    }
    const normalized = raw.trim();
    if (!/^-?\d+(\.\d+)?$/.test(normalized)) {
      return null;
    }
    return normalized;
  }

  private buildLoginUserEnvelope(
    authData: BackendLoginResponse,
    loginId: string,
    profileUserKey: string,
    profile: BackendUserProfile | null,
  ): Record<string, unknown> {
    const user = {
      userId: profileUserKey,
      aipayUid: this.firstNonEmpty(
        this.normalizeNumericIdentifier(profile?.aipayUid),
        this.normalizeNumericIdentifier(authData.aipayUid),
        profileUserKey,
      ),
      loginId: this.firstNonEmpty(profile?.loginId, loginId, profileUserKey),
      accountStatus: this.firstNonEmpty(profile?.accountStatus, 'ACTIVE'),
      kycLevel: this.firstNonEmpty(profile?.kycLevel, 'L0'),
      accountSource: this.firstNonEmpty(profile?.accountSource, 'REGISTER'),
      nickname: this.firstNonEmpty(profile?.nickname, authData.nickname, `用户${profileUserKey}`),
      avatarUrl: this.normalizeAvatarUrl(profile?.avatarUrl),
      countryCode: this.firstNonEmpty(profile?.countryCode),
      mobile: this.firstNonEmpty(profile?.mobile, loginId),
      maskedRealName: this.firstNonEmpty(profile?.maskedRealName),
      idCardNo: this.firstNonEmpty(profile?.idCardNo),
      gender: this.firstNonEmpty(profile?.gender),
      region: this.firstNonEmpty(profile?.region),
      birthday: this.firstNonEmpty(profile?.birthday),
    };
    return user;
  }

  private normalizeAvatarUrl(raw: unknown): string {
    const normalized = typeof raw === 'string' ? raw.trim() : '';
    if (!normalized) {
      return '';
    }
    if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
      if (normalized.includes('api.dicebear.com') && normalized.includes('/svg?')) {
        return normalized.replace('/svg?', '/png?');
      }
      return normalized;
    }
    if (normalized.startsWith('/')) {
      return this.backendHttpService.resolveAbsoluteUrl(normalized);
    }
    return normalized;
  }

  private resolveProfileUserKey(authData: BackendLoginResponse): string | null {
    return (
      resolveSubjectIdFromAccessToken(authData.accessToken) ??
      this.normalizeNumericIdentifier(authData.aipayUid) ??
      this.normalizeNumericIdentifier(authData.userId)
    );
  }

  private resolveRequiredUserId(profile: BackendUserProfile, fallbackMessage: string): string {
    const userId = this.normalizeNumericIdentifier(profile.userId) ?? this.normalizeNumericIdentifier(profile.aipayUid);
    if (!userId) {
      throw new BadGatewayException({
        code: 'UPSTREAM_EMPTY_RESPONSE',
        message: fallbackMessage,
      });
    }
    return userId;
  }

  private normalizeNumericIdentifier(raw: unknown): string | null {
    if (typeof raw === 'number') {
      if (!Number.isSafeInteger(raw) || raw <= 0) {
        return null;
      }
      return String(raw);
    }

    if (typeof raw !== 'string') {
      return null;
    }

    const normalized = raw.trim();
    if (!/^\d+$/.test(normalized)) {
      return null;
    }

    return normalized;
  }

  private firstNonEmpty(...values: Array<string | null | undefined>): string {
    for (const value of values) {
      if (typeof value === 'string' && value.trim().length > 0) {
        return value.trim();
      }
    }
    return '';
  }

  private normalizeOptionalText(raw: unknown): string | undefined {
    if (typeof raw !== 'string') {
      return undefined;
    }
    const normalized = raw.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private buildInternalUserAuthHeaders(userId: string): Record<string, string> {
    const token = buildSignedAccessToken(userId, {
      deviceId: 'app-bff-demo-auto-login',
      expiresInSeconds: 7 * 24 * 3600,
    });
    if (!token) {
      return {};
    }
    return {
      Authorization: `Bearer ${token}`,
    };
  }

  private triggerAiCreditAutoOpenInBackground(userId: string): void {
    const normalizedUserId = this.normalizeNumericIdentifier(userId);
    if (!normalizedUserId) {
      return;
    }
    void Promise.resolve()
      .then(() => this.ensureAiCreditOpened(normalizedUserId))
      .catch((error: unknown) => {
        this.logger.warn(
          `[鉴权]自动开通爱花失败, userId:${normalizedUserId}, error:${this.extractErrorMessage(error) || 'unknown'}`,
        );
      });
  }

  private async ensureAiCreditOpened(userId: string): Promise<void> {
    let agreementPack: BackendCreditOpenAgreementPack | null = null;
    try {
      const query = new URLSearchParams();
      query.set('userId', userId);
      query.set('productCode', 'AICREDIT');
      const response = await this.backendHttpService.get<BackendEnvelope<BackendCreditOpenAgreementPack>>(
        `/api/agreements/packs/credit-product-open?${query.toString()}`,
      );
      agreementPack = response.data?.data ?? null;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询爱花开通协议失败');
    }

    const agreementAccepts = (agreementPack?.agreements ?? [])
      .map((template) => {
        const templateCode = this.normalizeOptionalText(template?.templateCode);
        const templateVersion = this.normalizeOptionalText(template?.templateVersion);
        if (!templateCode || !templateVersion) {
          return null;
        }
        return { templateCode, templateVersion };
      })
      .filter((item): item is { templateCode: string; templateVersion: string } => item !== null);
    if (agreementAccepts.length === 0) {
      return;
    }

    const idempotencyKey = `AUTO_LOGIN_AICREDIT_${userId}`.slice(0, 64);
    try {
      await this.backendHttpService.post<BackendEnvelope<unknown>, Record<string, unknown>>(
        '/api/agreements/sign/credit-product-open',
        {
          userId,
          productCode: 'AICREDIT',
          idempotencyKey,
          agreementAccepts,
        },
      );
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '自动开通爱花失败');
    }
  }

  private buildBackendAccessTokenHeaders(accessToken: string | undefined): Record<string, string> | undefined {
    const normalized = this.normalizeOptionalText(accessToken);
    if (!normalized) {
      return undefined;
    }
    return {
      Authorization: `Bearer ${normalized}`,
    };
  }

  private rethrowMappedUpstreamError(error: unknown, fallbackMessage: string): never {
    if (error instanceof AxiosError) {
      const upstreamStatus = error.response?.status;
      const upstreamData = error.response?.data as BackendEnvelope<unknown> | undefined;
      const code = upstreamData?.error?.code ?? 'UPSTREAM_ERROR';
      const message = upstreamData?.error?.message ?? fallbackMessage;

      if (upstreamStatus === 401) {
        throw new UnauthorizedException({
          code,
          message,
        });
      }
      if (upstreamStatus === 400) {
        throw new BadRequestException({
          code,
          message,
        });
      }
      throw new BadGatewayException({
        code,
        message,
      });
    }
    if (
      error instanceof UnauthorizedException
      || error instanceof BadRequestException
      || error instanceof BadGatewayException
    ) {
      throw error;
    }
    this.logger.error(`[鉴权]业务异常, request:${fallbackMessage}`, (error as Error | undefined)?.stack);
    throw new BadGatewayException({
      code: 'UPSTREAM_ERROR',
      message: fallbackMessage,
    });
  }
}
