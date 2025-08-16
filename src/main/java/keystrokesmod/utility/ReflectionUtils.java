package keystrokesmod.utility;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class ReflectionUtils {
    /**
     * 调用方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T call(@NotNull Object object, @NotNull String method, Object... params) {
        final MethodData data = new MethodData(object.getClass(), method, params);

        try {
            return (T) getMethod(data).invoke(object, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用方法，并指定类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T call(@NotNull Object object, @NotNull String method,
                             Object @NotNull [] params, Class<?> @NotNull [] paramTypes) {
        if (params.length != paramTypes.length) {
            throw new RuntimeException("Param length not match with paramTypes length");
        }

        final MethodData data = new MethodData(object.getClass(), method, paramTypes);

        try {
            return (T) getMethod(data).invoke(object, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    private static final class MethodData {
        private final Class<?> aClass;
        private final String method;
        private final Class<?>[] params;

        MethodData(@NotNull Class<?> aClass, @NotNull String method, Object... params) {
            this(aClass, method, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new));
        }
    }

    @Data
    @AllArgsConstructor
    private static final class FieldData {
        private final Class<?> aClass;
        private final String field;
    }

    private static final Map<MethodData, Method> methodMap = new Object2ObjectOpenHashMap<>();
    private static final Map<FieldData, Field> fieldMap = new Object2ObjectOpenHashMap<>();
    private static final Map<Method, Consumer<?>> fastMethodMap = new Object2ObjectOpenHashMap<>();

    /**
     * 获取方法，递归查找父类中的方法
     */
    private static @NotNull Method getMethod(@NotNull MethodData data) {
        Method result = methodMap.get(data);
        if (result != null)
            return result;

        Class<?> currentClass = data.getAClass();
        while (currentClass != null) {
            try {
                final Method target = currentClass.getDeclaredMethod(data.getMethod(), data.getParams());
                target.setAccessible(true);
                methodMap.put(data, target);
                return target;
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();  // 继续查找父类
            }
        }
        throw new RuntimeException("Method not found: " + data.getMethod());
    }

    /**
     * 获取字段，递归查找父类中的字段
     */
    private static @NotNull Field getField(@NotNull FieldData data) {
        Field result = fieldMap.get(data);
        if (result != null)
            return result;

        Class<?> currentClass = data.getAClass();
        while (currentClass != null) {
            try {
                final Field target = currentClass.getDeclaredField(data.getField());
                target.setAccessible(true);

                int modifiers = target.getModifiers();
                if (Modifier.isFinal(modifiers)) {
                    ReflectionUtils.set(target, "modifiers", modifiers & ~Modifier.FINAL);
                }

                fieldMap.put(data, target);
                return target;
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();  // 继续查找父类
            }
        }
        throw new RuntimeException("Field not found: " + data.getField());
    }

    /**
     * 调用静态方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T callDeclared(@NotNull Class<?> aClass, @NotNull String method, Object... params) {
        final MethodData data = new MethodData(aClass, method, params);

        try {
            return (T) getMethod(data).invoke(null, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(@NotNull Object object, @NotNull String field) {
        final FieldData data = new FieldData(object.getClass(), field);

        try {
            return (T) getField(data).get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取静态字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDeclared(@NotNull Class<?> aClass, @NotNull String field) {
        final FieldData data = new FieldData(aClass, field);

        try {
            return (T) getField(data).get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置字段值
     */
    public static void set(@NotNull Object object, @NotNull String field, Object value) {
        final FieldData data = new FieldData(object.getClass(), field);

        try {
            getField(data).set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置静态字段值
     */
    public static void setDeclared(@NotNull Class<?> aClass, @NotNull String field, Object value) {
        final FieldData data = new FieldData(aClass, field);

        try {
            getField(data).set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E> @NotNull Consumer<T> getFastMethod(Class<E> objClass, @Nullable E object, Method method, Class<T> parClass)
            throws Throwable {
        Consumer<?> result = fastMethodMap.get(method);
        if (result != null)
            return (Consumer<T>) result;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.unreflect(method);
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                MethodType.methodType(Consumer.class, objClass),
                MethodType.methodType(void.class, Object.class),
                methodHandle,
                MethodType.methodType(void.class, parClass)
        );
        Consumer<T> target = (Consumer<T>) callSite.getTarget().invoke(object);
        fastMethodMap.put(method, target);
        return target;
    }

    public static boolean isFastMethod(Consumer<?> consumer) {
        return fastMethodMap.containsValue(consumer);
    }

    public static @NotNull String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace
        // 1: this method
        // 2: caller
        // 3: the method called caller
        if (stackTrace.length < 3)
            return "Unknown Source";
        String[] splits = stackTrace[3].getClassName().split("\\.");
        return splits[splits.length - 1];
    }

    public static @NotNull String getCallerClassName(final int extraDepth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace
        // 1: this method
        // 2: caller
        // 3: the method called caller
        if (stackTrace.length < 3 + extraDepth)
            return "Unknown Source";
        String[] splits = stackTrace[3 + extraDepth].getClassName().split("\\.");
        return splits[splits.length - 1];
    }

    public static @NotNull String getCallerMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace
        // 1: this method
        // 2: caller
        // 3: the method called caller
        if (stackTrace.length < 3)
            return "Unknown Source";
        return stackTrace[3].getMethodName();
    }

    public static @NotNull String getCallerMethodName(final int extraDepth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace
        // 1: this method
        // 2: caller
        // 3: the method called caller
        if (stackTrace.length < 3 + extraDepth)
            return "Unknown Source";
        return stackTrace[3 + extraDepth].getMethodName();
    }
}
