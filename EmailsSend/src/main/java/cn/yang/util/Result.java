package cn.yang.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum Result {
    SUCCESS("Y"),FAILED("N");

    Result(String name){
        try {
            Field method= getClass().getSuperclass().getDeclaredField("name");
            method.setAccessible(true);
            method.set(this,name);
            method.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
