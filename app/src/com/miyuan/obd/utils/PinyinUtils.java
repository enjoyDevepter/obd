package com.miyuan.obd.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by guomin on 2018/2/7.
 */
public class PinyinUtils {

    // 组 0
    public static final int NONE = 0x0000;                // 全部小写字母，没有分隔符
    // 组 1
    public static final int CASE_CAPITALIZE = 0x0001;     // 拼音首字母大写
    public static final int CASE_UPPERCASE = 0x0002;      // 全部大写字母
    // 组 2
    public static final int LETTER_FIRST = 0x0004;        // 获取拼音首字母，方向为从左到右
    public static final int LETTER_FIRST_INV = 0x0008;    // 获取拼音首字母，方向为从右到左
    public static final int LETTER_LAST = 0x0010;         // 获取拼音尾字母，方向为从左到右
    public static final int LETTER_LAST_INV = 0x0020;     // 获取拼音尾字母，方向为从右到左
    // 组 3
    public static final int TRIM_NON_CHAR = 0x0040;       // 去掉非字符
    // 组 4 (可以根据规则进行扩展)
    public static final int SEPARATOR_BLANK = 0x0080;     // 分隔符：空格
    public static final int SEPARATOR_POINT = 0x0100;     // 分隔符：英文句号
    public static final int SEPARATOR_HYPHEN = 0x0200;    // 分隔符：连字符


    // 默认：全部小写字母，没有分隔符
    public static String toPinyinString(String hanzi) {
        return toPinyinString(hanzi, NONE);
    }

    public static String toPinyinString(String hanzi, int mode) {
        StringBuilder builder = new StringBuilder();
        if (hanzi != null) {
            int length = hanzi.length();                // 长度
            String tmp;
            for (int i = 0; i < length; i++) {
                char hanziChar = hanzi.charAt(i);       // 获取指定索引号的字符
                if (checkHanziChar(hanziChar)) {
                    tmp = toPinyinChar(hanziChar);      // 全部小写
                    if ((mode & CASE_CAPITALIZE) != NONE) {
                        tmp = capitalize(tmp);          // 首字母大写 (高优先级)
                    } else if ((mode & CASE_UPPERCASE) != NONE) {
                        tmp = tmp.toUpperCase();        // 全部大写
                    }

                    if ((mode & LETTER_FIRST) != NONE || (mode & LETTER_FIRST_INV) != NONE) {
                        tmp = tmp.substring(0, 1);              // 拼音首字母 (高优先级)
                    } else if ((mode & LETTER_LAST) != NONE || (mode & LETTER_LAST_INV) != NONE) {
                        tmp = tmp.substring(tmp.length() - 1);  // 拼音尾字母
                    }
                } else {
                    tmp = Character.toString(hanziChar);
                    if ((mode & TRIM_NON_CHAR) != NONE) {
                        tmp = "";           // 去掉非字符
                    }
                }

                String separator = "";      // 默认不加分隔符
                if ((mode & SEPARATOR_BLANK) != NONE) {
                    separator = " ";        // 加入空格分隔符 (高优先级)
                } else if ((mode & SEPARATOR_POINT) != NONE) {
                    separator = ".";        // 加入英文句号分隔符
                } else if ((mode & SEPARATOR_HYPHEN) != NONE) {
                    separator = "-";        // 加入连字符分隔符
                }

                if (i >= length - 1) {      // 跳过最后一个汉字拼音的分隔符
                    separator = "";
                }

                // add the string to builder now
                if ((mode & LETTER_FIRST_INV) != NONE || (mode & LETTER_LAST_INV) != NONE) { // RTL
                    builder.insert(0, tmp);         // 1. 将tmp插入到头部，实现从右到左的方向
                    builder.insert(0, separator);   // 2. 将分隔符插入到头部
                } else {    // LTR - LeftToRight
                    builder.append(tmp);            // 1. 将tmp追加到尾部，实现从左到右的方向
                    builder.append(separator);      // 2. 将分隔符追加到尾部
                }
            }   // for (int i = 0; i < length; i++)
        }   // if (hanzi != null)
        return builder.toString();
    }

    public static String toPinyinChar(char hanziChar) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);      // 全部小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);   // 没有声调
        format.setVCharType(HanyuPinyinVCharType.WITH_V);       // 使用v
        return toPinyinChar(hanziChar, format);
    }

    public static String toPinyinChar(char hanziChar, HanyuPinyinOutputFormat format) {
        String[] result = null;
        try {
            // 非汉字字符放回null
            result = PinyinHelper.toHanyuPinyinStringArray(hanziChar, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return result != null ? result[0] : "";
    }

    // 检查输入字符是否匹配到unicode中的汉字区间内
    public static boolean checkHanziChar(char hanziChar) {
        return Character.toString(hanziChar).matches("[\\u4E00-\\u9FA5]+");
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}