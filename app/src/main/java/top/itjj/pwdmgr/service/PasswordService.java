package top.itjj.pwdmgr.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import top.itjj.pwdmgr.pojo.Password;

/**
 * 服务层-接口
 */
public interface PasswordService {
    /**
     * 查询密码
     *
     * @param type 密码类型
     * @param isShadowQuery 是否执行模糊查询
     * @return 如果type不为空，返回一条密码，否则返回一个密码列表
     */
    public List<Password> queryPasswords(String type, boolean isShadowQuery);

    /**
     * 增加密码
     *
     * @param password 密码对象
     * @return 返回一个增加成功的条数，如果大于0，增加成功，否则失败
     */
    public long insertPassword(Password password);

    /**
     * 修改密码
     *
     * @param password 密码对象
     * @return 返回一个修改成功的条数，如果大于0，修改成功，否则失败
     */
    public int updatePassword(Password password);

    /**
     * 根据密码类型删除密码
     *
     * @param type 密码类型
     * @return 返回一个删除成功的条数，如果大于0，删除成功，否则失败
     */
    public int deletePassword(String type);

    /**
     * 从文件中导入密码数据到数据库中
     *
     * @param importFilePath 存放密码数据的文件路径
     * @return 返回一个导入成功的条数，如果大于0，导入成功，否则失败
     */
    public long importPasswords(String importFilePath);

    /**
     * 导出密码数据到文件中
     *
     * @param passwordList   数据库中的密码数据列表
     * @param exportFilePath 导出的密码文件所在路径
     * @return 导出成功，返回true，否则返回false
     */
    public boolean exportPasswords(List<Password> passwordList, String exportFilePath);
}
