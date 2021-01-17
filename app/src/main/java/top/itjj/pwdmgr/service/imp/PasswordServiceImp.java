package top.itjj.pwdmgr.service.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.itjj.pwdmgr.dao.PasswordDao;
import top.itjj.pwdmgr.dao.imp.PasswordDaoImp;
import top.itjj.pwdmgr.pojo.Password;
import top.itjj.pwdmgr.service.PasswordService;
import top.itjj.pwdmgr.util.DatabaseUtil;

/**
 * 服务层-接口实现类
 */
public class PasswordServiceImp implements PasswordService {
    PasswordDao passwordDao;

    public PasswordServiceImp(DatabaseUtil util) {
        passwordDao = new PasswordDaoImp(util);
    }

    /**
     * 查询密码
     *
     * @param type 密码类型
     * @param isShadowQuery 是否执行模糊查询
     * @return 如果type为空，返回一条密码，否则返回一个密码列表
     */
    @Override
    public List<Password> queryPasswords(String type, boolean isShadowQuery) {
        List<Password> passwordList = new ArrayList<>();
        try {
            passwordList = passwordDao.queryPasswords(type, isShadowQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return passwordList;
    }

    /**
     * 增加密码
     *
     * @param password 密码对象
     * @return 返回一个增加成功的条数，如果大于0，增加成功，否则失败
     */
    @Override
    public long insertPassword(Password password) {
        long rows;
        try {
            // 如果保存的密码在数据库中已经存在，返回0
            if (isTypeRepeat(password.getType())) {
                return 0;
            }
            rows = passwordDao.insertPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return rows;
    }

    /**
     * 修改密码
     *
     * @param password 密码对象
     * @return 返回一个修改成功的条数，如果大于0，修改成功，否则失败
     */
    @Override
    public int updatePassword(Password password) {
        int rows;
        try {
            // 如果修改的密码类型在数据库中不存在（类型不重复），返回0
            if (!isTypeRepeat(password.getType())) {
                return 0;
            }
            rows = passwordDao.updatePassword(password);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return rows;
    }

    /**
     * 根据密码类型删除密码
     *
     * @param type 密码类型
     * @return 返回一个删除成功的条数，如果大于0，删除成功，否则失败
     */
    @Override
    public int deletePassword(String type) {
        int rows;
        try {
            // 如果删除的密码类型在数据库中不存在（类型不重复），返回0
            if (!isTypeRepeat(type)) {
                return 0;
            }
            rows = passwordDao.deletePassword(type);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return rows;
    }

    /**
     * 从文件中导入密码数据到数据库中
     *
     * @param importFilePath 存放密码数据的文件路径
     * @return 返回一个导入成功的条数，如果大于0，导入成功，否则失败
     */
    @Override
    public long importPasswords(String importFilePath) {
        long rows;
        try {
            rows = passwordDao.importPasswords(importFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return rows;
    }

    /**
     * 导出密码数据到文件中
     *
     * @param passwordList   数据库中的密码数据列表
     * @param exportFilePath 导出的密码文件所在路径
     * @return 导出成功，返回true，否则返回false
     */
    @Override
    public boolean exportPasswords(List<Password> passwordList, String exportFilePath) {
        boolean success;
        try {
            success = passwordDao.exportPasswords(passwordList, exportFilePath);
        } catch (IOException e) {
            return false;
        }
        return success;
    }

    /**
     * 判断密码类型是否重复
     *
     * @param type 密码类型
     * @return 如果传入的密码类型在数据库中存在，返回true，默认返回false
     */
    public boolean isTypeRepeat(String type) {
        for (Password queryPassword : queryPasswords(null, false)) {
            String queryType = queryPassword.getType();
            if (type.equals(queryType)) {
                return true;
            }
        }
        return false;
    }

}
