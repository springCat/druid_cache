package org.springcat.druidcache.util;

/**
 * Created by springcat on 16/6/5.
 */
public class StrKit {

    public static boolean isBlank(String s){
        return  s == null || s.length() == 0;
    }

    public static boolean isNotBlank(String s){
        return  s != null && s.length() > 0;
    }
}
