package top.itjj.pwdmgr.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库管理工具类
 */
public class DatabaseUtil extends SQLiteOpenHelper {

    public DatabaseUtil(Context paramContext, String paramString,
                        SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt) {
        super(paramContext, paramString, paramCursorFactory, paramInt);
    }

    /**
     * 创建一张passwords表
     *
     * @param paramSQLiteDatabase SQLiteDatabase
     */
    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL("" +
                "create table passwords(type varchar(30) primary key, account varchar(30), " +
                "password varchar(30))");
    }

    /**
     * 当数据表结构更新时所执行的方法
     *
     * @param paramSQLiteDatabase SQLiteDatabase
     * @param paramInt1           paramInt1
     * @param paramInt2           paramInt2
     */
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
    }
}
