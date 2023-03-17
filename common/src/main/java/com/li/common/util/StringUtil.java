package com.li.common.util;

/**
 * 字符串工具类
 */
public class StringUtil {


    /**
     * 判断字符串是否是空字符串
     * @param str 字符串
     * @return true 传入的字符串不为空字符串
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 将字符串的首字母转大写
     * @param str 字符串
     * @return /
     */
    public static String captureFirst(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * 将字符串的首字母转小写
     * @param str 字符串
     * @return /
     */
    public static String lowerFirst(String str) {
        return changeFirstCharacterCase(str, false);
    }

    /**
     * 将字符串的首字母转成大写/小写
     * @param str 字符串
     * @param capitalize true 转大写
     * @return /
     */
    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        }
        else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars);
    }


    public static void main(String[] args) {
        String str = "hello world";
        System.out.println(captureFirst(str));
        System.out.println(lowerFirst(str));
    }
}
