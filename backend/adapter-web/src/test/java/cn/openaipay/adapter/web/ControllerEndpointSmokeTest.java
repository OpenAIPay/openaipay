package cn.openaipay.adapter.web;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import cn.openaipay.domain.coupon.model.CouponSceneType;
import cn.openaipay.domain.coupon.model.CouponTemplate;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;
import cn.openaipay.domain.coupon.model.CouponValueType;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ControllerEndpointSmokeTest 统一接口覆盖回归测试。
 *
 * 目标：
 * 1. 自动扫描 adapter-web 的全部 Controller。
 * 2. 自动执行每个 Mapping 方法，避免新增接口无测试覆盖。
 *
 * 说明：
 * - 该测试重点保障“接口可被调用、签名不破坏、依赖注入可构造”。
 * - 对少量业务预期异常（如参数非法、资源不存在）允许通过，不视为失败。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class ControllerEndpointSmokeTest {

    private static final String BASE_PACKAGE = "cn.openaipay.adapter";
    private static final int MIN_EXPECTED_ENDPOINT_COUNT = 40;
    private static final String MOBILE_TOP_UP_REWARD_TEMPLATE_CODE = "MOBILE_TOPUP_REWARD_2_5";

    @Test
    void shouldInvokeAllMappedControllerEndpoints() throws Exception {
        List<Class<?>> controllers = discoverControllers();
        Assertions.assertFalse(controllers.isEmpty(), "未扫描到任何 Controller");

        List<String> failures = new ArrayList<>();
        Map<String, Integer> endpointCountByController = new TreeMap<>();
        int totalEndpointCount = 0;

        for (Class<?> controllerClass : controllers) {
            Object controller = instantiateController(controllerClass);
            List<Method> endpointMethods = discoverEndpointMethods(controllerClass);
            endpointCountByController.put(controllerClass.getSimpleName(), endpointMethods.size());

            for (Method method : endpointMethods) {
                totalEndpointCount++;
                Object[] args = buildMethodArguments(method);
                try {
                    method.setAccessible(true);
                    method.invoke(controller, args);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getTargetException();
                    if (!isAllowedBusinessException(cause)) {
                        failures.add(endpointId(controllerClass, method) + " -> " + cause.getClass().getSimpleName()
                                + ": " + Objects.toString(cause.getMessage(), ""));
                    }
                } catch (Throwable ex) {
                    failures.add(endpointId(controllerClass, method) + " -> " + ex.getClass().getSimpleName()
                            + ": " + Objects.toString(ex.getMessage(), ""));
                }
            }
        }

        Assertions.assertTrue(
                totalEndpointCount >= MIN_EXPECTED_ENDPOINT_COUNT,
                "Controller Mapping 数量异常，actual=" + totalEndpointCount
        );
        Assertions.assertTrue(
                failures.isEmpty(),
                "存在未通过的接口 Smoke 用例:\n" + String.join("\n", failures)
        );
        Assertions.assertTrue(
                endpointCountByController.values().stream().noneMatch(count -> count == 0),
                "存在未发现 Mapping 的 Controller: " + endpointCountByController
        );
    }

    private List<Class<?>> discoverControllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        List<Class<?>> classes = new ArrayList<>();
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> clazz = Class.forName(candidate.getBeanClassName());
            if (!clazz.getSimpleName().endsWith("Controller")) {
                continue;
            }
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            if (!clazz.getPackageName().contains(".web")) {
                continue;
            }
            classes.add(clazz);
        }
        classes.sort(Comparator.comparing(Class::getName));
        return classes;
    }

    private List<Method> discoverEndpointMethods(Class<?> controllerClass) {
        List<Method> methods = new ArrayList<>();
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (hasMappingAnnotation(method)) {
                methods.add(method);
            }
        }
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }

    private boolean hasMappingAnnotation(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(RequestMapping.class);
    }

    private Object instantiateController(Class<?> controllerClass) throws Exception {
        Constructor<?> constructor = selectConstructor(controllerClass);
        Object[] args = new Object[constructor.getParameterCount()];
        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            args[i] = provideValue(parameters[i].getType(), parameters[i].getName(), true);
        }
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private Constructor<?> selectConstructor(Class<?> controllerClass) {
        Constructor<?>[] constructors = controllerClass.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException("Controller 无可用构造器: " + controllerClass.getName());
        }
        return List.of(constructors).stream()
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new IllegalStateException("Controller 构造器选择失败: " + controllerClass.getName()));
    }

    private Object[] buildMethodArguments(Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            args[i] = provideValue(parameters[i].getType(), resolveParameterHint(parameters[i]), false);
        }
        return args;
    }

    private String resolveParameterHint(Parameter parameter) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            if (!requestParam.value().isBlank()) {
                return requestParam.value();
            }
            if (!requestParam.name().isBlank()) {
                return requestParam.name();
            }
        }
        return parameter.getName();
    }

    private Object provideValue(Class<?> type, String hintName, boolean constructorDependency) throws Exception {
        if (type == String.class) {
            return defaultStringValue(hintName);
        }
        if (type == Long.class || type == long.class) {
            return 1L;
        }
        if (type == Integer.class || type == int.class) {
            return 1;
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.TRUE;
        }
        if (type == Short.class || type == short.class) {
            return (short) 1;
        }
        if (type == Byte.class || type == byte.class) {
            return (byte) 1;
        }
        if (type == Double.class || type == double.class) {
            return 1.0D;
        }
        if (type == Float.class || type == float.class) {
            return 1.0F;
        }
        if (type == Character.class || type == char.class) {
            return 'a';
        }
        if (type == BigDecimal.class) {
            return BigDecimal.ONE;
        }
        if (type == LocalDate.class) {
            return LocalDate.now();
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (type == CurrencyUnit.class) {
            return CurrencyUnit.of("CNY");
        }
        if (type == Money.class) {
            return Money.of(CurrencyUnit.of("CNY"), BigDecimal.ONE);
        }
        if (type == byte[].class) {
            return new byte[] {1, 2, 3};
        }
        if (type == MultipartFile.class) {
            return new MockMultipartFile("file", "file.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
        }
        if (type == Locale.class) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        if (type == Optional.class) {
            return Optional.empty();
        }
        if (Collection.class.isAssignableFrom(type)) {
            if (Set.class.isAssignableFrom(type)) {
                return Set.of();
            }
            return List.of();
        }
        if (Map.class.isAssignableFrom(type)) {
            return Map.of();
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? null : constants[0];
        }
        if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        }
        if (type.isRecord()) {
            return instantiateRecord(type);
        }
        if (!constructorDependency && hasNoArgsConstructor(type)) {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        return mockWithDefaultAnswer(type);
    }

    private boolean hasNoArgsConstructor(Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    private Object instantiateRecord(Class<?> recordType) throws Exception {
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            parameterTypes[i] = components[i].getType();
            values[i] = provideValue(components[i].getType(), components[i].getName(), false);
        }
        Constructor<?> canonicalConstructor = recordType.getDeclaredConstructor(parameterTypes);
        canonicalConstructor.setAccessible(true);
        return canonicalConstructor.newInstance(values);
    }

    private Object mockWithDefaultAnswer(Class<?> type) {
        return Mockito.mock(type, invocation -> {
            Class<?> returnType = invocation.getMethod().getReturnType();
            String methodName = invocation.getMethod().getName();
            Object[] arguments = invocation.getArguments();
            if (returnType == void.class) {
                return null;
            }
            if (returnType == Optional.class) {
                if ("findTemplateByCode".equals(methodName)
                        && arguments.length == 1
                        && MOBILE_TOP_UP_REWARD_TEMPLATE_CODE.equals(arguments[0])) {
                    return Optional.of(mockMobileTopUpRewardTemplate());
                }
                return Optional.empty();
            }
            if (returnType == String.class) {
                return defaultStringValue(methodName);
            }
            if (returnType == Long.class || returnType == long.class) {
                return 0L;
            }
            if (returnType == Integer.class || returnType == int.class) {
                return 0;
            }
            if (returnType == Boolean.class || returnType == boolean.class) {
                return false;
            }
            if (returnType == Double.class || returnType == double.class) {
                return 0D;
            }
            if (returnType == Float.class || returnType == float.class) {
                return 0F;
            }
            if (returnType == BigDecimal.class) {
                return BigDecimal.ZERO;
            }
            if (returnType == LocalDate.class) {
                return LocalDate.now();
            }
            if (returnType == LocalDateTime.class) {
                return LocalDateTime.now();
            }
            if (returnType == CurrencyUnit.class) {
                return CurrencyUnit.of("CNY");
            }
            if (returnType == Money.class) {
                return Money.zero(CurrencyUnit.of("CNY"));
            }
            if (returnType == byte[].class) {
                return new byte[] {1};
            }
            if (Collection.class.isAssignableFrom(returnType)) {
                if (Set.class.isAssignableFrom(returnType)) {
                    return new LinkedHashSet<>();
                }
                return new ArrayList<>();
            }
            if (Map.class.isAssignableFrom(returnType)) {
                return new LinkedHashMap<>();
            }
            if (returnType.isEnum()) {
                Object[] constants = returnType.getEnumConstants();
                return constants.length == 0 ? null : constants[0];
            }
            if (returnType.isArray()) {
                return Array.newInstance(returnType.getComponentType(), 0);
            }
            if (returnType.isRecord()) {
                return instantiateRecord(returnType);
            }
            if (hasNoArgsConstructor(returnType)) {
                Constructor<?> constructor = returnType.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private String defaultStringValue(String hintName) {
        String lower = hintName == null ? "" : hintName.toLowerCase(Locale.ROOT);
        if (lower.contains("productcode")) {
            return CreditProductCodes.AICREDIT;
        }
        if (lower.contains("currency")) {
            return "CNY";
        }
        if (lower.contains("mimetype") || lower.contains("contenttype")) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (lower.contains("filename") || lower.contains("originalname") || lower.endsWith("name")) {
            return "file";
        }
        if (lower.contains("status")) {
            return "ACTIVE";
        }
        if (lower.contains("scene")) {
            return "TRADE_PAY";
        }
        return "mock-value";
    }

    private CouponTemplate mockMobileTopUpRewardTemplate() {
        LocalDateTime now = LocalDateTime.now();
        return new CouponTemplate(
                1L,
                MOBILE_TOP_UP_REWARD_TEMPLATE_CODE,
                "手机充值奖励红包",
                CouponSceneType.PAYMENT_INCENTIVE,
                CouponValueType.FIXED,
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("2.50")),
                null,
                null,
                Money.zero(CurrencyUnit.of("CNY")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("1000.00")),
                1000,
                0,
                1,
                now.minusDays(1),
                now.plusDays(30),
                now.minusDays(1),
                now.plusDays(30),
                "PLATFORM",
                "{}",
                CouponTemplateStatus.ACTIVE,
                "test",
                "test",
                now.minusDays(1),
                now
        );
    }

    private boolean isAllowedBusinessException(Throwable throwable) {
        return throwable instanceof IllegalArgumentException
                || throwable instanceof NoSuchElementException;
    }

    private String endpointId(Class<?> controllerClass, Method method) {
        return controllerClass.getSimpleName() + "#" + method.getName();
    }
}
