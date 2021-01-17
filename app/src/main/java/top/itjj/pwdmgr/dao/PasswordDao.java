package top.itjj.pwdmgr.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import top.itjj.pwdmgr.pojo.Password;

/**
 * 数据层-接口
 */
public interface PasswordDao {
    /**
     * 查询密码
     *
     * @param type 密码类型
     * @param isShadowQuery 是否执行模糊查询
     * @return 如果type为空，返回一条密码，否则返回一个密码列表
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    public List<Password> queryPasswords(String type, boolean isShadowQuery) throws RuntimeException;

    /**
     * 增加密码
     *
     * @param password 密码对象
     * @return 返回一个增加成功的条数，如果大于0，增加成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    public long insertPassword(Password password) throws RuntimeException;

    /**
     * 修改密码
     *
     * @param password 密码对象
     * @return 返回一个修改成功的条数，如果大于0，修改成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    public int updatePassword(Password password) throws RuntimeException;

    /**
     * 根据密码类型删除密码
     *
     * @param type 密码类型
     * @return 返回一个删除成功的条数，如果大于0，删除成功，否则失败
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    public int deletePassword(String type) throws RuntimeException;

    /**
     * 从文件中导入密码数据到数据库中
     *
     * @param importFilePath 存放密码数据的文件路径
     * @return 返回一个导入成功的条数，如果大于0，导入成功，否则失败
     * @throws IOException 抛出文件操作过程中发生的异常，比如文件内容格式错误等等
     * @throws RuntimeException 当执行SQL语句出错时，抛出运行时异常
     */
    public long importPasswords(String importFilePath) throws IOException, RuntimeException;

    /**
     * 导出密码数据到文件中
     *
     * @param passwordList   数据库中的密码数据列表
     * @param exportFilePath 导出的密码文件所在路径
     * @return 导出成功，返回true，否则返回false
     * @throws IOException 抛出文件操作过程中发生的异常
     */
    public boolean exportPasswords(List<Password> passwordList, String exportFilePath)
            throws IOException;
}
