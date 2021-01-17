package top.itjj.pwdmgr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import top.itjj.pwdmgr.service.PasswordService;
import top.itjj.pwdmgr.service.imp.PasswordServiceImp;
import top.itjj.pwdmgr.util.DatabaseUtil;
import top.itjj.pwdmgr.pojo.Password;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {

    private PasswordService passwordService;
    private DrawerLayout drawerLayout;
    private EditText etType, etAccount, etPassword, etSearchValue;
    private ListView passwordList;
    // 被查询的密码是否存在（当密码列表不为空的时候）
    private static boolean IS_PASSWORD_NOT_EXIST = false;
    // 导入是否成功（用来接收来自FileOperateActivity）发送的广播数据
    private boolean isImportSuccess;
    private ImportStateReceiver importStateReceiver;
    private int times = 0;
    private final int REQUEST_PERMISSION = 0;
    private SharedPreferences preferences;

    /**
     * 应用程序启动时调用
     *
     * @param savedInstanceState savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        preferences = getSharedPreferences("help", Context.MODE_PRIVATE);
        // 判断是否在启动时弹出帮助对话框
        if (preferences.getBoolean("isAlertHelpDialogOnStart", true)) {
            alertHelpDialogOnStart();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void alertHelpDialogOnStart() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("使用帮助")
                    .setMessage(getFileContent("help"))
                    .setPositiveButton("我知道了", null)
                    .setNegativeButton("不再弹出", (dialogInterface, i) -> {
                        // 点了不再弹出后，将false存入SharedPreferences中
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isAlertHelpDialogOnStart", false);
                        editor.apply();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("提示")
                                .setMessage("如需获取帮助可以在菜单栏里面找到")
                                .setPositiveButton("好的", null)
                                .create()
                                .show();
                    })
                    .create()
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查软件所需权限是否被给予
     */
    private void checkPermission() {
        times++;
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissionsList.size() != 0) {
                if (times == 1) {
                    requestPermissions(permissionsList.toArray(new String[0]),
                            REQUEST_PERMISSION);
                } else {
                    new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle("提示")
                            .setMessage("获取不到授权，APP将无法正常使用，请允许APP获取权限！")
                            .setPositiveButton("确定", (arg0, arg1) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(permissionsList.toArray(
                                            new String[0]),
                                            REQUEST_PERMISSION);
                                }
                            }).setNegativeButton("取消",
                            (arg0, arg1) -> finish()).show();
                }
            }
        }
    }

    /**
     * 返回权限申请结果
     *
     * @param requestCode requestCode
     * @param permissions permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission();
    }

    /**
     * 当当前Activity被销毁时，销毁广播
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(importStateReceiver);
    }

    /**
     * 初始化应用程序
     */
    public void init() {
        // 检查权限
        checkPermission();
        // 初始化PasswordService，第一次启动时创建数据库
        passwordService = new PasswordServiceImp(
                new DatabaseUtil(this,
                        "itjj_pwd.db",
                        null,
                        1));
        // 查询所有密码数据并放到一个List中
        List<Password> fullPasswordList = passwordService.queryPasswords(null,
                false);
        // 初始化各个控件并设置对应的监听
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            /**
             * 如果查询完一条密码（搜索框内有内容），且侧边栏被关闭<br/>
             * 调用这个方法来清空搜索框中的文本，并且重新将数据库中的所有密码数据放到密码列表区
             *
             * @param drawerView drawerView
             */
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!fullPasswordList.isEmpty() && etSearchValue.getText() != null) {
                    etSearchValue.setText("");
                    setListView(passwordService.queryPasswords(null, false));
                }
            }
        });

        etType = findViewById(R.id.et_type);
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);

        etSearchValue = findViewById(R.id.search_value);
        etSearchValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /**
             * 当从搜索框中输入内容且按了搜索键后调用此方法来查询密码
             *
             * @param textView textView
             * @param i i
             * @param keyEvent keyEvent
             * @return 返回true
             */
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                List<Password> searchPasswordList = passwordService.queryPasswords(
                        etSearchValue.getText().toString(), true);
                IS_PASSWORD_NOT_EXIST = searchPasswordList.isEmpty();
                setListView(searchPasswordList);
                return true;
            }
        });

        Button btnSave = findViewById(R.id.btn_save);
        Button btnUpdate = findViewById(R.id.btn_update);
        Button btnDelete = findViewById(R.id.btn_delete);

        ThisClickListener clickListener = new ThisClickListener();
        btnSave.setOnClickListener(clickListener);
        btnUpdate.setOnClickListener(clickListener);
        btnDelete.setOnClickListener(clickListener);

        passwordList = findViewById(R.id.password_list);

        // 当应用程序启动（所有组件初始化完毕）时，将数据库中的所有密码数据放到密码列表区
        setListView(fullPasswordList);
        // 给ImportStateReceiver类注册广播
        importStateReceiver = new ImportStateReceiver();
        IntentFilter filter = new IntentFilter("IMPORT_ACTION");
        registerReceiver(importStateReceiver, filter);
    }

    /**
     * 创建一个菜单
     *
     * @param paramMenu Menu对象
     * @return 返回true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu paramMenu) {
        this.getMenuInflater().inflate(R.menu.more_menu, paramMenu);
        return true;
    }

    /**
     * 当软件继续运行的时候，判断isImportSuccess是否为true，如果是，则说明用户进行了导入密码的操作<br/>
     * 然后刷新密码列表，将isImportSuccess重置为false
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isImportSuccess) {
            setListView(passwordService.queryPasswords(null, false));
            isImportSuccess = false;
        }
    }

    /**
     * 菜单项被选择
     *
     * @param paramMenuItem MenuItem对象
     * @return 返回true
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        switch (paramMenuItem.getItemId()) {
            // 导入密码
            case R.id.import_password:
                Intent intentImport = new Intent(this, FileOperateActivity.class);
                intentImport.putExtra("label", "请选择sst密码文件");
                intentImport.putExtra("editable", false);
                startActivity(intentImport);
                break;
            // 导出密码
            case R.id.export_password:
                Intent intentExport = new Intent(this, FileOperateActivity.class);
                intentExport.putExtra("label", "请选择导出路径");
                intentExport.putExtra("editable", true);
                intentExport.putExtra("passwordList",
                        (Serializable) passwordService.queryPasswords(
                                null, false));
                startActivity(intentExport);
                break;
            // 使用帮助
            case R.id.help:
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("使用帮助")
                            .setMessage(getFileContent("help"))
                            .setPositiveButton("我知道了", null)
                            .create()
                            .show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            // 关于软件
            case R.id.about:
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("关于软件")
                            .setMessage(getFileContent("about"))
                            .setPositiveButton("我知道了", null)
                            .create()
                            .show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            // 退出软件
            case R.id.exit:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(paramMenuItem);
        }
        return true;
    }

    /**
     * 从assets文件夹读取文本文件内容
     *
     * @param fileName 文件名
     * @return 返回读取到的字符串
     * @throws IOException 抛出IO异常
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getFileContent(String fileName) throws IOException {
        String text = null;
        AssetManager manager = getAssets();
        InputStream stream = manager.open(fileName + ".txt");
        int i;
        byte[] buff = new byte[4096];
        while ((i = stream.read(buff)) != -1) {
            text = new String(buff, 0, i, StandardCharsets.UTF_8);
        }
        return text;
    }

    /**
     * 获取用户的文本框输入
     *
     * @return 返回一个Passwords对象
     */
    public Password getUserInput() {
        Password passwords = new Password();
        String type = this.etType.getText().toString();
        String account = this.etAccount.getText().toString();
        String password = this.etPassword.getText().toString();
        passwords.setType(type);
        passwords.setAccount(account);
        passwords.setPassword(password);
        return passwords;
    }

    /**
     * 将数据库中的密码数据类型名称添加到侧滑列表中
     *
     * @param passwordList 数据库中的密码数据列表
     */
    public void setListView(List<Password> passwordList) {
        List<String> targetList = new ArrayList<>();
        String emptyHint = "你没有保存过任何密码！";

        if (passwordList.isEmpty() && !IS_PASSWORD_NOT_EXIST) {
            etSearchValue.setVisibility(View.GONE);
            targetList.add(emptyHint);
        } else {
            etSearchValue.setVisibility(View.VISIBLE);
            for (Password password : passwordList) targetList.add(password.getType());
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                targetList);
        this.passwordList.setAdapter(arrayAdapter);
        this.passwordList.setOnItemClickListener((param1AdapterView, param1View, param1Int,
                                                  param1Long) -> {
            String type = (String) param1AdapterView.getItemAtPosition(param1Int);
            if (!type.equals(emptyHint)) {
                List<Password> getList = passwordService.queryPasswords(type, false);
                etType.setText(getList.get(0).getType());
                etAccount.setText(getList.get(0).getAccount());
                etPassword.setText(getList.get(0).getPassword());
            }
            drawerLayout.closeDrawers();
        });
    }

    /**
     * 广播接收类，用来接收FileOperateActivity发送的广播数据
     */
    class ImportStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取广播中发送的状态数据，默认值是false
            isImportSuccess = intent.getBooleanExtra("state", false);
        }
    }

    /**
     * 按钮事件类
     */
    private class ThisClickListener implements View.OnClickListener {
        private final MainActivity mainActivity = MainActivity.this;

        @SuppressLint("NonConstantResourceId")
        public void onClick(View param1View) {
            // 判断输入的内容是否含有空值
            Password passwords = mainActivity.getUserInput();
            String type = passwords.getType();
            String account = passwords.getAccount();
            String password = passwords.getPassword();
            if (type == null || "".equals(type)
                    || account == null || "".equals(account)
                    || password == null || "".equals(password)) {
                Toast.makeText(mainActivity, "不能有空值！", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (param1View.getId()) {
                // 保存
                case R.id.btn_save:
                    Toast.makeText(mainActivity,
                            passwordService.insertPassword(passwords) > 0 ? "保存成功！" :
                                    "保存失败！", Toast.LENGTH_SHORT).show();
                    setListView(passwordService.queryPasswords(null, false));
                    break;
                // 更新
                case R.id.btn_update:
                    Toast.makeText(mainActivity,
                            passwordService.updatePassword(passwords) > 0 ? "修改成功！" :
                                    "修改失败！", Toast.LENGTH_SHORT).show();
                    setListView(passwordService.queryPasswords(null, false));
                    break;
                // 删除
                case R.id.btn_delete:
                    int deleteRows = passwordService.deletePassword(passwords.getType());
                    String hint = "删除失败！";
                    if (deleteRows > 0) {
                        hint = "删除成功！";
                        mainActivity.etType.setText("");
                        mainActivity.etAccount.setText("");
                        mainActivity.etPassword.setText("");
                        setListView(passwordService.queryPasswords(null,
                                false));
                    }
                    Toast.makeText(mainActivity, hint, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}