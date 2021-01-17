package top.itjj.pwdmgr.dao.imp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import top.itjj.pwdmgr.dao.PasswordDao;
import top.itjj.pwdmgr.pojo.Password;
import top.itjj.pwdmgr.util.DatabaseUtil;

/**
 * 数据层-接口实现类
 */
public class PasswordDaoImp implements PasswordDao {
    private final SQLiteDatabase database;

    public PasswordDaoImp(DatabaseUtil util) {
        database = util.getWritableDatabase();
    }

    /**
     * 查询密码
     *
     * @param type 密码类型
     * @param isShadowQuery 是否执行模糊查询
     * @return 如果type为空，返回一条密码，否则返回一个密码列表
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    @Override
    public List<Password> queryPasswords(String type, boolean isShadowQuery) throws RuntimeException {
        List<Password> passwordList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from passwords");
        Cursor cursor = database.rawQuery(sql.toString(), null);
        if (type != null && !"".equals(type)) {
            if (isShadowQuery) {
                sql.append(" where type like ?");
                type = "%" + type + "%";
            } else {
                sql.append(" where type = ?");
            }
            cursor = database.rawQuery(sql.toString(), new String[]{type});
        }
        while (cursor.moveToNext()) {
            Password password = new Password();
            password.setType(cursor.getString(cursor.getColumnIndex("type")));
            password.setAccount(cursor.getString(cursor.getColumnIndex("account")));
            password.setPassword(cursor.getString(cursor.getColumnIndex("password")));
            passwordList.add(password);
        }
        cursor.close();
        return passwordList;
    }

    /**
     * 增加密码
     *
     * @param password 密码对象
     * @return 返回一个增加成功的条数，如果大于0，增加成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    @Override
    public long insertPassword(Password password) throws RuntimeException {
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", password.getType());
        contentValues.put("account", password.getAccount());
        contentValues.put("password", password.getPassword());
        return database.insert("passwords", null, contentValues);
    }

    /**
     * 修改密码
     *
     * @param password 密码对象
     * @return 返回一个修改成功的条数，如果大于0，修改成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    @Override
    public int updatePassword(Password password) throws RuntimeException {
        ContentValues contentValues = new ContentValues();
        contentValues.put("account", password.getAccount());
        contentValues.put("password", password.getPassword());
        return database.update("passwords", contentValues, "type = ?",
                new String[]{password.getType()});
    }

    /**
     * 根据密码类型删除密码
     *
     * @param type 密码类型
     * @return 返回一个删除成功的条数，如果大于0，删除成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    @Override
    public int deletePassword(String type) throws RuntimeException {
        return database.delete("passwords", "type = ?", new String[]{type});
    }

    /**
     * 从文件中导入密码数据到数据库中
     *
     * @param importFilePath 存放密码数据的文件路径
     * @return 返回一个导入成功的条数，如果大于0，导入成功，否则失败
     * @throws IOException 抛出文件操作过程中发生的异常，比如文件内容格式错误等等
     */
    @Override
    public long importPasswords(String importFilePath) throws IOException, RuntimeException {
        long rows = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(importFilePath)));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] passwordInfo = line.split(" ");
            ContentValues contentValues = new ContentValues();
            contentValues.put("type", passwordInfo[0]);
            contentValues.put("account", passwordInfo[1]);
            contentValues.put("password", passwordInfo[2]);
            rows += database.insert("passwords", null, contentValues);
        }
        reader.close();
        return rows / 2;
    }

    /**
     * 导出密码数据到文件中
     *
     * @param passwordList   数据库中的密码数据列表
     * @param exportFilePath 导出的密码文件所在路径
     * @return 导出成功，返回true，否则返回false
     * @throws IOException 抛出文件操作过程中发生的异常
     */
    @Override
    public boolean exportPasswords(List<Password> passwordList, String exportFilePath)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportFilePath)));
        if (passwordList.isEmpty()) {
            return false;
        }
        for (Password password : passwordList) {
            String passwordInfo = password.getType() +
                    " " +
                    password.getAccount() +
                    " " +
                    password.getPassword() +
                    "\r";
            writer.write(passwordInfo);
        }
        writer.close();
        return true;
    }
}
