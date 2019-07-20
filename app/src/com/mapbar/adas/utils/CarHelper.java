package com.mapbar.adas.utils;

import android.text.TextUtils;

import com.mapbar.adas.CarInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by guomin on 2018/2/7.
 */
public class CarHelper {

    public static List<CarInfo> contactsFilter(String filterStr, List<CarInfo> carInfos) {
        List<CarInfo> mFilterList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {     // 若过滤字符为空，则直接返回所有列表信息
            mFilterList = carInfos;
        } else {
            mFilterList.clear();
            for (CarInfo carInfo : carInfos) {
                String upperFilterStr = filterStr.toUpperCase();
                String rawName = carInfo.getRawName();
                String pinyinName = carInfo.getPinyinName();
                // 联系人姓名中是否包含此搜索的过滤字符串
                // 或者联系人姓名的拼音字符串是否以搜索的过滤字符串为开头
                if (rawName.toUpperCase().contains(upperFilterStr)
                        || pinyinName.startsWith(upperFilterStr)) {
                    mFilterList.add(carInfo);
                }
            }
        }
        Collections.sort(mFilterList);      // 排序
        return mFilterList;
    }

    // 创建侧边栏中的字母索引
    public static List<String> setupLetterIndexList(List<CarInfo> contactInfoList) {
        List<String> mLetterIndexList = new ArrayList<>();
        boolean found = false;
        for (CarInfo contactInfo : contactInfoList) {
            String firstLetter = contactInfo.getSortLetters().substring(0, 1);
            if (!mLetterIndexList.contains(firstLetter) && !"#".equals(firstLetter)) {
                mLetterIndexList.add(firstLetter);
            }
            if (!found && "#".equals(firstLetter)) {    // 只要找到#字符就不再进行判断
                found = true;
            }
        }
        Collections.sort(mLetterIndexList);     // 排序
        if (found) {                            // 若发现有"#"
            mLetterIndexList.add("#");          // 则只在列表最后添加"#"
        }
        return mLetterIndexList;
    }

    // 创建联系人列表的信息
    public static List<CarInfo> setupContactInfoList(List<CarInfo> contacts) {
        for (CarInfo contact : contacts) {
            contact.setRawName(contact.getName());        // rawName

            // 只会对中文转成的汉字拼音进行大写处理
            String pinyinName = PinyinUtils.toPinyinString(contact.getName(), PinyinUtils.CASE_UPPERCASE);
            pinyinName = pinyinName.toUpperCase();  // 若包含英文字母则额外再进行大写处理
            contact.setPinyinName(pinyinName);  // pinyinName

//            String sortLetters = setupSortLetters(contact.getName());
            contact.setSortLetters(contact.getLetter()); // sortLetters

        }
        Collections.sort(contacts);  // 排序
        return contacts;
    }

    // 创建用于排序的字母串：默认返回的字符串全部为大写字母或者#
    // 规则定义：
    // 汉字开头时，只取第一个汉字的拼音；
    // 英文开头时，只截取从开头到非字母字符之前的字母
    private static String setupSortLetters(String contact) {
        String firstChar = String.valueOf(contact.charAt(0));   // 获取第一个字符
        int mode = PinyinUtils.CASE_UPPERCASE | PinyinUtils.TRIM_NON_CHAR;
        String pinyin = PinyinUtils.toPinyinString(firstChar, mode);
        if (!TextUtils.isEmpty(pinyin)) {       // 首个字符是汉字 (简单的爱)
            return pinyin;                      // JIAN
        } else {
            String words = contact.split("[^a-zA-Z]")[0]; // 或者以英文串开头 (q$100w)
            if (!TextUtils.isEmpty(words)) {
                return words.toUpperCase();               // Q
            } else {                // 其他的字符归类到#中 ($$Mr.Dj)
                return "#";         // #
            }
        }
    }
}
