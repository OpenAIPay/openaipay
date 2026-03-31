package cn.openaipay.domain.creditaccount.model;

/**
 * 信用账户类型。
 *
 * 业务约定：
 * - 爱花账户号前缀使用 CA
 * - 爱借账户号前缀使用 LA
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum CreditAccountType {
    /** 爱花账户。 */
    AICREDIT("CA"),
    /** 借贷账户。 */
    LOAN_ACCOUNT("LA");

    /** 账号前缀。 */
    private final String accountNoPrefix;

    CreditAccountType(String accountNoPrefix) {
        this.accountNoPrefix = accountNoPrefix;
    }

    /**
     * 处理单号。
     */
    public String accountNoPrefix() {
        return accountNoPrefix;
    }

    /**
     * 处理单号。
     */
    public String defaultAccountNo(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        return accountNoPrefix + userId;
    }

    /**
     * 处理单号。
     */
    public static CreditAccountType fromAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("accountNo must not be blank");
        }
        String normalized = accountNo.trim().toUpperCase();
        if (normalized.startsWith(LOAN_ACCOUNT.accountNoPrefix)) {
            return LOAN_ACCOUNT;
        }
        return AICREDIT;
    }
}
