package com.evergreen.android.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

public class ReflectUtils {

  public static <T> T getField(Object targetInstance, String fieldName) {
    try {
      Field f = getFieldInternal(targetInstance, fieldName);
      return (T) f.get(targetInstance);
    } catch (Throwable e) {
      return null;
    }
  }

  public static void setField(Object targetInstance, String fieldName, Object val) {
    try {
      Field f = getFieldInternal(targetInstance, fieldName);
      f.set(targetInstance, val);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @NotNull
  private static Field getFieldInternal(Object targetInstance, String fieldName)
      throws NoSuchFieldException {
    Class<?> cls = targetInstance.getClass();
    Field f = null;

    while (f == null) {
      try {
        f = cls.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        cls = cls.getSuperclass();
      }

      if (cls == null) {
        throw new NoSuchFieldException();
      }
    }

    f.setAccessible(true);
    return f;
  }

  public static <T> T callMethod(Object targetInstance, String methodName, Object... args) {
    try {
      final Class<?> clazz = targetInstance.getClass();
      Method method = findMethod(clazz, methodName, getParameterTypes(args));

      return (T) method.invoke(targetInstance, args);
    } catch (Throwable e) {
      return null;
    }
  }

  private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramsTypes)
      throws NoSuchMethodException {
    Method method = null;
    Class cls = clazz;

    while (method == null) {
      try {
        method = cls.getDeclaredMethod(methodName, paramsTypes);
      } catch (NoSuchMethodException e) {
        cls = clazz.getSuperclass();
      }

      if (cls == null) {
        throw new NoSuchMethodException();
      }
    }

    method.setAccessible(true);
    return method;
  }

  private static Class<?>[] getParameterTypes(Object... args) {
    Class<?>[] parameterTypes = null;

    if (args != null && args.length > 0) {
      parameterTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        Object param = args[i];
        parameterTypes[i] = param == null ? null : param.getClass();
      }
    }
    return parameterTypes;
  }
}
