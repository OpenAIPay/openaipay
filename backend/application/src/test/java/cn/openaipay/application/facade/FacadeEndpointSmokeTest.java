package cn.openaipay.application.facade;

import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
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
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

/**
 * FacadeEndpointSmokeTest 统一门面接口覆盖回归测试。
 *
 * <p>目标：
 * 1. 自动扫描 application 模块下全部 FacadeImpl。
 * 2. 自动执行每个公开门面方法，避免新增 Facade 接口无测试覆盖。</p>
 *
 * <p>说明：
 * - 该测试重点保障“门面可构造、方法签名不破坏、主要编排逻辑可被触达”。
 * - 对少量业务预期异常（如参数非法、资源不存在、协议未开通）允许通过，不视为失败。</p>
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class FacadeEndpointSmokeTest {

    private static final String BASE_PACKAGE = "cn.openaipay.application";
    private static final int MIN_EXPECTED_FACADE_COUNT = 30;
    private static final int MIN_EXPECTED_METHOD_COUNT = 80;

    @Test
    void shouldInvokeAllPublicFacadeMethods() throws Exception {
        List<Class<?>> facades = discoverFacades();
        Assertions.assertFalse(facades.isEmpty(), "未扫描到任何 FacadeImpl");

        List<String> failures = new ArrayList<>();
        int totalMethodCount = 0;

        for (Class<?> facadeClass : facades) {
            Object facade = instantiateFacade(facadeClass);
            List<Method> methods = discoverPublicFacadeMethods(facadeClass);
            for (Method method : methods) {
                totalMethodCount++;
                Object[] args = buildMethodArguments(method);
                try {
                    method.setAccessible(true);
                    method.invoke(facade, args);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getTargetException();
                    if (!isAllowedBusinessException(cause)) {
                        failures.add(endpointId(facadeClass, method) + " -> " + cause.getClass().getSimpleName()
                                + ": " + Objects.toString(cause.getMessage(), ""));
                    }
                } catch (Throwable ex) {
                    failures.add(endpointId(facadeClass, method) + " -> " + ex.getClass().getSimpleName()
                            + ": " + Objects.toString(ex.getMessage(), ""));
                }
            }
        }

        Assertions.assertTrue(
                facades.size() >= MIN_EXPECTED_FACADE_COUNT,
                "FacadeImpl 数量异常，actual=" + facades.size()
        );
        Assertions.assertTrue(
                totalMethodCount >= MIN_EXPECTED_METHOD_COUNT,
                "Facade 方法数量异常，actual=" + totalMethodCount
        );
        Assertions.assertTrue(
                failures.isEmpty(),
                "存在未通过的 Facade Smoke 用例:\n" + String.join("\n", failures)
        );
    }

    private List<Class<?>> discoverFacades() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));

        List<Class<?>> classes = new ArrayList<>();
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> clazz = Class.forName(candidate.getBeanClassName());
            if (!clazz.getSimpleName().endsWith("FacadeImpl")) {
                continue;
            }
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            if (!clazz.getPackageName().contains(".facade.")
                    && !clazz.getPackageName().contains(".deliver.admin")) {
                continue;
            }
            classes.add(clazz);
        }
        classes.sort(Comparator.comparing(Class::getName));
        return classes;
    }

    private List<Method> discoverPublicFacadeMethods(Class<?> facadeClass) {
        List<Method> methods = new ArrayList<>();
        for (Method method : facadeClass.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }
            methods.add(method);
        }
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }

    private Object instantiateFacade(Class<?> facadeClass) throws Exception {
        Constructor<?> constructor = selectConstructor(facadeClass);
        Object[] args = new Object[constructor.getParameterCount()];
        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            args[i] = provideValue(parameters[i].getType(), parameters[i].getName(), true);
        }
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private Constructor<?> selectConstructor(Class<?> facadeClass) {
        Constructor<?>[] constructors = facadeClass.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException("Facade 无可用构造器: " + facadeClass.getName());
        }
        return List.of(constructors).stream()
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new IllegalStateException("Facade 构造器选择失败: " + facadeClass.getName()));
    }

    private Object[] buildMethodArguments(Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            args[i] = provideValue(parameters[i].getType(), parameters[i].getName(), false);
        }
        return args;
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
            return Boolean.FALSE;
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
            if (returnType == void.class) {
                return null;
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
            if (returnType == Optional.class) {
                return Optional.empty();
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
        if (lower.contains("currency")) {
            return "CNY";
        }
        if (lower.contains("fundcode")) {
            return FundProductCodes.DEFAULT_FUND_CODE;
        }
        if (lower.contains("productcode")) {
            return CreditProductCodes.AICREDIT;
        }
        if (lower.contains("loginid") || lower.contains("mobile")) {
            return "13800138000";
        }
        if (lower.contains("password")) {
            return "777444";
        }
        if (lower.contains("couponno")) {
            return "COUPON202603210001";
        }
        if (lower.contains("accountno")) {
            return "ACC202603210001";
        }
        if (lower.contains("order") || lower.contains("request") || lower.contains("business")
                || lower.contains("trade") || lower.contains("branch") || lower.contains("xid")) {
            return "NO202603210001";
        }
        if (lower.contains("status")) {
            return "ACTIVE";
        }
        if (lower.contains("scene")) {
            return "TRADE_PAY";
        }
        if (lower.contains("month")) {
            return "2026-03";
        }
        return "mock-value";
    }

    private boolean isAllowedBusinessException(Throwable throwable) {
        return throwable instanceof IllegalArgumentException
                || throwable instanceof NoSuchElementException
                || throwable instanceof IllegalStateException
                || throwable instanceof ForbiddenException;
    }

    private String endpointId(Class<?> facadeClass, Method method) {
        return facadeClass.getSimpleName() + "#" + method.getName();
    }
}
