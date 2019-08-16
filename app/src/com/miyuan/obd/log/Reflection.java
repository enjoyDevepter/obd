package com.miyuan.obd.log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by xiaoyee on 11/16/16.
 * 反射工具类
 */

public class Reflection {
    private static HashMap<String, HashMap<String, Field>> sCache;

    static {
        sCache = new HashMap<>();
    }

    public static <T> Object getField(T obj, String str) {
        try {
            Field privateField = getPrivateField(obj, str);
            if (privateField != null) {
                privateField.setAccessible(true);
                return privateField.get(obj);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getPrivateField(Object obj, String str) {
        if (obj == null) {
            return null;
        }
        Class cls;
        if (obj instanceof Class) {
            cls = (Class) obj;
        } else {
            cls = obj.getClass();
        }
        Field fromCache = fromCache(cls, str);
        if (fromCache != null) {
            return fromCache;
        }
        Class cls2 = cls;
        while (fromCache == null && cls2 != null) {
            try {
                fromCache = cls2.getDeclaredField(str);
            } catch (NoSuchFieldException e) {
                //
            }
            if (fromCache == null) {
                cls2 = cls2.getSuperclass();
            }
        }
        if (fromCache == null) {
            return fromCache;
        }
        toCache(cls, str, fromCache);
        return fromCache;
    }

    private static Field fromCache(Class cls, String str) {
        HashMap hashMap = (HashMap) sCache.get(cls.getName());
        return hashMap == null ? null : (Field) hashMap.get(str);
    }

    private static void toCache(Class cls, String str, Field field) {
        HashMap hashMap = (HashMap) sCache.get(cls.getName());
        if (hashMap == null) {
            hashMap = new HashMap();
            sCache.put(cls.getName(), hashMap);
        }
        hashMap.put(str, field);
    }

    public static boolean setField(Object obj, String str, Object obj2) {
        Field privateField = getPrivateField(obj, str);
        if (privateField != null) {
            try {
                privateField.setAccessible(true);
                privateField.set(obj, obj2);
                return true;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Object call(Object obj, String str, Object... objArr) {
        int i = 0;
        try {
            Class[] clsArr = new Class[(objArr == null ? 0 : objArr.length)];
            while (i < clsArr.length) {
                clsArr[i] = objArr[i].getClass();
                i++;
            }
            return call(obj, str, clsArr, objArr);
        } catch (Throwable e) {
//            SELog.m627w(e);
            return null;
        }
    }

    public static Object call(Object obj, String str, Class[] clsArr, Object... objArr) {
        Object t = null;
        try {
            Class cls = obj.getClass();
            Method method = null;
            while (method == null && cls != null) {
                try {
                    method = cls.getDeclaredMethod(str, clsArr);
                } catch (NoSuchMethodException e) {
                    //
                }
                cls = cls.getSuperclass();
            }
            if (method != null) {
                method.setAccessible(true);
                t = method.invoke(obj, objArr);
            }
        } catch (Throwable e2) {
//            SELog.m627w(e2);
        }
        return t;
    }

    public static Object callStatic(String str, String str2, Object[] objArr) {
        try {
            return callStatic(Class.forName(str), str2, objArr);
        } catch (Throwable e) {
//            SELog.m627w(e);
            return null;
        }
    }

    public static Object callStatic(String str, String str2, Class[] clsArr, Object[] objArr) {
        try {
            return callStatic(Class.forName(str), str2, clsArr, objArr);
        } catch (Throwable e) {
//            SELog.m627w(e);
            return null;
        }
    }

    public static Object callStatic(Class cls, String str, Object[] objArr) {
        int i = 0;
        try {
            Class[] clsArr = new Class[(objArr == null ? 0 : objArr.length)];
            while (i < clsArr.length) {
                clsArr[i] = objArr[i].getClass();
                i++;
            }
            return callStatic(cls, str, clsArr, objArr);
        } catch (Throwable e) {
//            SELog.m627w(e);
            return null;
        }
    }

    public static Object callStatic(Class cls, String str, Class[] clsArr, Object[] objArr) {
        Object t = null;
        try {
            Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                t = declaredMethod.invoke(null, objArr);
            }
        } catch (Throwable e) {
//            SELog.m627w(e);
        }
        return t;
    }

}
