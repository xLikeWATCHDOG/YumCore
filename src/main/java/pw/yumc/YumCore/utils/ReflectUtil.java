package pw.yumc.YumCore.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft反射类
 *
 * @since 2015年12月14日 下午1:35:11
 * @author 许凯
 */
@SuppressWarnings("all")
public class ReflectUtil {

    public static Field getDeclaredFieldByName(final Class source, final String name) {
        try {
            final Field field = source.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Field> getDeclaredFieldByType(final Class source, final Class type) {
        final List<Field> list = new ArrayList<>();
        for (final Field field : source.getDeclaredFields()) {
            if (field.getType() == type) {
                field.setAccessible(true);
                list.add(field);
            }
        }
        return list;
    }

    public static Method getDeclaredMethod(final Class clzz, final String methodName, final Class... args) {
        try {
            return clzz.getDeclaredMethod(methodName, args);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getDeclaredMethodByNameAndParams(final Class source, final String name, final Class... args) {
        for (final Method method : findMethodByParams(source.getDeclaredMethods(), args)) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public static List<Method> getDeclaredMethodByNameAndType(final Class source, final String name, final Class returnType) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : source.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Method> getDeclaredMethodByParams(final Class source, final Class... args) {
        return findMethodByParams(source.getDeclaredMethods(), args);
    }

    public static List<Method> getDeclaredMethodByParamsAndType(final Class source, final Class returnType, final Class... args) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : findMethodByParams(source.getDeclaredMethods(), args)) {
            if (method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Method> getDeclaredMethodByType(final Class source, final Class returnType) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : source.getDeclaredMethods()) {
            if (method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static Field getFieldByName(final Class source, final String name) {
        try {
            final Field field = source.getField(name);
            field.setAccessible(true);
            return field;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Field> getFieldByType(final Class source, final Class type) {
        final List<Field> list = new ArrayList<>();
        for (final Field field : source.getFields()) {
            if (field.getType() == type) {
                field.setAccessible(true);
                list.add(field);
            }
        }
        return list;
    }

    public static Object getHandle(final Object bukkitObj) {
        try {
            return bukkitObj.getClass().getMethod("getHandle").invoke(bukkitObj);
        } catch (final Exception e) {
        }
        return null;
    }

    public static Method getMethodByNameAndParams(final Class source, final String name, final Class... args) {
        for (final Method method : findMethodByParams(source.getMethods(), args)) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public static List<Method> getMethodByNameAndType(final Class source, final String name, final Class returnType) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : source.getMethods()) {
            if (method.getName().equals(name) && method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Method> getMethodByParams(final Class source, final Class... args) {
        return findMethodByParams(source.getMethods(), args);
    }

    public static List<Method> getMethodByParamsAndType(final Class source, final Class returnType, final Class... args) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : findMethodByParams(source.getMethods(), args)) {
            if (method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Method> getMethodByType(final Class source, final Class returnType) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : source.getMethods()) {
            if (method.getReturnType().equals(returnType)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static void invokeMethod(final Object object, final String methodName, final Class arg, final Object value) {
        try {
            final Method m = object.getClass().getDeclaredMethod(methodName, arg);
            m.invoke(object, value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeMethod(final Object object, final String methodName, final Class[] args, final Object[] value) {
        try {
            final Method m = object.getClass().getDeclaredMethod(methodName, args);
            m.invoke(object, value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeMethod(final Object object, final String methodName, final Object value) {
        try {
            final Method m = object.getClass().getDeclaredMethod(methodName, value.getClass());
            m.invoke(object, value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Method> findMethodByParams(final Method[] methods, final Class... args) {
        final List<Method> list = new ArrayList<>();
        start:
        for (final Method method : methods) {
            if (method.getParameterTypes().length == args.length) {
                final Class[] array = method.getParameterTypes();
                for (int i = 0; i < args.length; i++) {
                    if (!array[i].equals(args[i])) {
                        continue start;
                    }
                }
                method.setAccessible(true);
                list.add(method);
            }
        }
        return list;
    }
}
