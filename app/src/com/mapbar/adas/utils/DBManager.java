package com.mapbar.adas.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mapbar.adas.FaultCode;
import com.mapbar.adas.GlobalUtil;
import com.mapbar.adas.Physicaltem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guomin on 2018/11/12.
 */

public class DBManager {
    private String DB_NAME = "physical.db";

    private SQLiteDatabase sqliteDB;

    private DBManager() {
        String dbPath = "/data/data/com.miyuan.obd"
                + "/databases/" + DB_NAME;
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                boolean flag = new File("/data/data/com.miyuan.obd/databases/").mkdirs();
                FileOutputStream out = new FileOutputStream(dbPath);
                InputStream in = GlobalUtil.getMainActivity().getAssets().open(DB_NAME);
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                while ((readBytes = in.read(buffer)) != -1)
                    out.write(buffer, 0, readBytes);
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sqliteDB = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    public static DBManager getInstance() {
        return DBManager.InstanceHolder.INSTANCE;
    }

    //查询选择题
    public Map<Integer, Physicaltem> getAll() {
        HashMap<Integer, Physicaltem> physicaltemHashMap = new HashMap<>();
        try {
            String table = "physical_item";
            Cursor cursor = sqliteDB.query(table, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int index = cursor.getInt(cursor.getColumnIndex("index"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int min = cursor.getInt(cursor.getColumnIndex("min"));
                int max = cursor.getInt(cursor.getColumnIndex("max"));
                boolean compare = cursor.getInt(cursor.getColumnIndex("compare")) == 0 ? false : true;
                String desc = cursor.getString(cursor.getColumnIndex("desc"));
                String high_appearance = cursor.getString(cursor.getColumnIndex("high_appearance"));
                String higt_reason = cursor.getString(cursor.getColumnIndex("higt_reason"));
                String higt_resolvent = cursor.getString(cursor.getColumnIndex("higt_resolvent"));
                String low_appearance = cursor.getString(cursor.getColumnIndex("low_appearance"));
                String low_reason = cursor.getString(cursor.getColumnIndex("low_reason"));
                String low_resolvent = cursor.getString(cursor.getColumnIndex("low_resolvent"));
                int socre = cursor.getInt(cursor.getColumnIndex("socre"));
                Physicaltem physicaltem = new Physicaltem();
                physicaltem.setId(id);
                physicaltem.setIndex(index);
                physicaltem.setType(type);
                physicaltem.setName(name);
                physicaltem.setMin(min);
                physicaltem.setMax(max);
                physicaltem.setCompare(compare);
                physicaltem.setDesc(desc);
                physicaltem.setHigh_appearance(high_appearance);
                physicaltem.setHigt_reason(higt_reason);
                physicaltem.setHigt_resolvent(higt_resolvent);
                physicaltem.setLow_appearance(low_appearance);
                physicaltem.setLow_reason(low_reason);
                physicaltem.setLow_resolvent(low_resolvent);
                physicaltem.setSocre(socre);
                physicaltem.setStyle(0);
                physicaltemHashMap.put(physicaltem.getIndex(), physicaltem);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return physicaltemHashMap;
    }

    public List<FaultCode> getInfoForCode(String code) {
        ArrayList<FaultCode> codes = new ArrayList<>();
        try {
            String table = "code";
            Cursor cursor = sqliteDB.rawQuery("select * from " + table + " where id = ?", new String[]{code});
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String suit = cursor.getString(cursor.getColumnIndex("suit"));
                String desc_ch = cursor.getString(cursor.getColumnIndex("desc_ch"));
                String desc_en = cursor.getString(cursor.getColumnIndex("desc_en"));
                String detail = cursor.getString(cursor.getColumnIndex("detail"));
                String system = cursor.getString(cursor.getColumnIndex("system"));
                FaultCode codeItem = new FaultCode();
                codeItem.setId(id);
                codeItem.setSuit(suit);
                codeItem.setDesc_ch(desc_ch);
                codeItem.setDesc_en(desc_en);
                codeItem.setDetail(detail);
                codeItem.setSystem(system);
                codes.add(codeItem);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codes;
    }

    public static class InstanceHolder {
        private static final DBManager INSTANCE = new DBManager();
    }
}
