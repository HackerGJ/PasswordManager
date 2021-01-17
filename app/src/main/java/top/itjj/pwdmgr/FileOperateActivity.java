package top.itjj.pwdmgr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.itjj.pwdmgr.pojo.Password;
import top.itjj.pwdmgr.service.PasswordService;
import top.itjj.pwdmgr.service.imp.PasswordServiceImp;
import top.itjj.pwdmgr.util.DatabaseUtil;

/**
 * 导入和导出界面
 */
public class FileOperateActivity extends AppCompatActivity {
    private PasswordService passwordService;
    private ListView fileList;
    private EditText etFileName;
    private String filePath;// 要传递的文件名全路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_operate);
        init();
    }

    public void init() {
        passwordService = new PasswordServiceImp(
                new DatabaseUtil(this,
                        "itjj_pwd.db",
                        null,
                        1));
        fileList = findViewById(R.id.file_list);
        etFileName = findViewById(R.id.et_file_name);
        Button btnOk = findViewById(R.id.btn_ok);
        Button btnCancel = findViewById(R.id.btn_cancel);
        ThisClickListener clickListener = new ThisClickListener();
        btnOk.setOnClickListener(clickListener);
        btnCancel.setOnClickListener(clickListener);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra("label"));
        if (!intent.getBooleanExtra("editable", true)) {
            etFileName.setEnabled(false);
            etFileName.setHint("");
        }
        setFileList(Environment.getExternalStorageDirectory());
    }

    /**
     * 将当前文件目录下的所有文件显示在列表框中
     *
     * @param currentFile 当前文件对象
     */
    public void setFileList(File currentFile) {
        // 显示在列表中的文件名
        List<String> targetList = new ArrayList<>();
        if (!currentFile.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
            targetList.add("返回上一级");
        }
        File[] listFile = currentFile.listFiles();
        for (File file : listFile) {
            targetList.add(file.getName());
        }
        ListAdapter listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                targetList);
        fileList.setAdapter(listAdapter);
        fileList.setOnItemClickListener((adapterView, view, i, l) -> {
            String currentPath = currentFile.getPath() + "/" + fileList.getItemAtPosition(i);
            if (fileList.getItemAtPosition(i).equals("返回上一级")) {
                // 返回上一级目录
                currentPath = currentFile.getParent();
                setFileList(new File(currentPath));
            } else if (new File(currentPath).isDirectory()) {
                // 进入下一级目录
                setFileList(new File(currentPath));
                filePath = currentPath;
            } else {
                // 判断是否为sst(SpaceSplitText)密码文件，如果是，显示在编辑框额内并给filePath赋值，否则提示用户
                String getClickFileName = fileList.getItemAtPosition(i).toString();
                if (getClickFileName.endsWith(".sst")) {
                    // 给filePath赋值
                    etFileName.setText(getClickFileName);
                    filePath = currentPath;
                } else {
                    etFileName.setText("");
                    Toast.makeText(FileOperateActivity.this, "请选择一个sst密码文件！",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private class ThisClickListener implements View.OnClickListener {
        private final FileOperateActivity fileOperateActivity = FileOperateActivity.this;

        @SuppressLint("NonConstantResourceId")
        @SuppressWarnings("unchecked")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_ok:
                    String fileName = etFileName.getText().toString();
                    if (!"".equals(fileName) && fileName != null) {
                        // 如果文本输入框可编辑，就是导出密码，否则就是导入密码
                        if (etFileName.isEnabled()) {
                            // 导出密码
                            String targetPath = filePath + "/" + fileName + ".sst";
                            Intent intent = fileOperateActivity.getIntent();
                            boolean isSuccess = passwordService.exportPasswords(
                                    (List<Password>) intent.getSerializableExtra(
                                            "passwordList"),
                                    targetPath);
                            String exportHint = "导出失败！";
                            if (isSuccess) {
                                exportHint = "成功导出到" + targetPath + "中！";
                            }
                            Toast.makeText(fileOperateActivity, exportHint,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // 导入密码
                            long importRows = passwordService.importPasswords(filePath);
                            String importHint = "导入失败！";
                            Intent state = new Intent("IMPORT_ACTION");
                            // 执行状态，成功导入后，给MainActivity发送一条广播
                            boolean isSuccess = false;
                            if (importRows > 0) {
                                importHint = "密码数据成功导入！";
                                isSuccess = true;
                            }
                            state.putExtra("state", isSuccess);
                            sendBroadcast(state);
                            Toast.makeText(fileOperateActivity, importHint,
                                    Toast.LENGTH_SHORT).show();
                        }
                        fileOperateActivity.finish();
                    } else {
                        Toast.makeText(fileOperateActivity, "文件名不能为空！",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_cancel:
                    fileOperateActivity.finish();
                    break;
            }
        }
    }
}