package com.wcb.contactlist.util;

import android.database.Cursor;
import android.text.TextUtils;

import com.wcb.contactlist.adapter.ContactAdapter;
import com.wcb.contactlist.model.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wcb on 2017/4/27.
 * 处理数据模型工具类
 */

public class DataUtil {

    public static List<SortModel> filledData(List<SortModel> date) {
        CharacterParser characterParser = CharacterParser.getInstance();//汉字转换成拼音的类
        PinyinComparator pinyinComparator = new PinyinComparator();//实例化汉字转拼音类
        List<SortModel> mSortList = new ArrayList<SortModel>();
        for (int i = 0; i < date.size(); i++) {
            SortModel sortModel = date.get(i);
            String pinyin = characterParser.getSelling(date.get(i).getName());//汉字转换成拼音
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) { // 正则表达式，判断首字母是否是英文字母
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }
            mSortList.add(sortModel);
        }
        Collections.sort(mSortList, pinyinComparator); // 根据a-z进行排序源数据
        return mSortList;
    }


    public static List<SortModel> getModels(Cursor cursor) {//遍历cursor中的模型并进行排序
        if (cursor == null) return null;
        List<SortModel> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                String display_name = cursor.getString(1);
                String data1 = cursor.getString(2);
                if (TextUtils.isEmpty(display_name) || TextUtils.isEmpty(data1)) continue;
                SortModel bean = new SortModel();
                bean.setName(display_name);
                bean.setPhoneNum(data1);
                list.add(bean);
            }
            if (list != null && list.size() > 0) {
                list = filledData(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();//关闭cursor，释放资源
            }
        }
        return list;
    }


    //根据输入框中的值来过滤数据并更新ListView
    public static void filterData(ContactAdapter adapter, List<SortModel> sourceDateList, String filterStr) {
        if (sourceDateList == null || sourceDateList.size() == 0) return;
        CharacterParser characterParser = CharacterParser.getInstance();//汉字转换成拼音的类
        PinyinComparator pinyinComparator = new PinyinComparator();
        List<SortModel> filterDateList = new ArrayList<SortModel>();
        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : sourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }
}
