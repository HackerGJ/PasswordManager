package top.itjj.pwdmgr.pojo;

import java.io.Serializable;

/**
 * 实体类-密码
 */
public class Password implements Serializable {
    private String type;// 类型
    private String account;// 账号
    private String password;// 密码

    public Password() {
    }

    public Password(String type, String account, String password) {
        this.type = type;
        this.account = account;
        this.password = password;
    }

    public String getType() {
        return this.type.trim();
    }

    public String getAccount() {
        return this.account.trim();
    }

    public String getPassword() {
        return this.password.trim();
    }

    public void setType(String paramString) {
        this.type = paramString;
    }

    public void setAccount(String paramString) {
        this.account = paramString;
    }

    public void setPassword(String paramString) {
        this.password = paramString;
    }

    @Override
    public String toString() {
        return "Password{" +
                "type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

