package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.userBackListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Jasper on 2017/4/6.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText usernameET;
    private EditText passwordET;
    private Button signInBtn;
    private TextView signUpBtn;
    private String username;
    private String password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ifLogin();

        usernameET = (EditText)findViewById(R.id.login_username);
        passwordET = (EditText)findViewById(R.id.login_password);
        signInBtn = (Button)findViewById(R.id.btn_sign_in);
        signUpBtn = (TextView) findViewById(R.id.btn_sign_up);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forLogin();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegister();
            }
        });
    }

    private void ifLogin() {
        FileInputStream fis = null;
        try {
            File file = new File(getFilesDir(), "info.properties");
            if(!file.exists()){
                return;
            }
            fis = new FileInputStream(file);
            Properties pro = new Properties();
            pro.load(fis);
            if(pro.get("userName").toString().equals("") || pro.get("userName") == null){
                return;
            }else{
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void toRegister() {
        startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
        this.finish();
    }

    private void forLogin() {
        username = usernameET.getText().toString().trim();
        password = passwordET.getText().toString().trim();
        if(!checkUserName(username)){
            return;
        }else if(!checkpassword(password)){
            return;
        }
        userDB.forUserLogin(username, password, new userBackListener() {
            @Override
            public void showResult(boolean result, String message) {
                if(result){
                    saveUser(username, password);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }else{
                    showDialog("用户名或密码错误");
                }
            }
        });
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", null).show();
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkUserName(String userName){
        if(userName.length() < 5){
            showDialog("用户名不应少于5位字符");
            return false;
        }
        return true;
    }

    private boolean checkpassword(String password){
        if(password.length() < 6){
            showDialog("密码不应少于6位字符");
            return false;
        }
        return true;
    }

    private void saveUser(String username, String password) {
        try {
            // 使用Android上下问获取当前项目的路径
            File file = new File(this.getFilesDir(), "info.properties");
            // 创建输出流对象
            FileOutputStream fos = new FileOutputStream(file);
            // 创建属性文件对象
            Properties pro = new Properties();
            // 设置用户名或密码
            pro.setProperty("userName", username);
            pro.setProperty("password", password);
            // 保存文件
            pro.store(fos, "info.properties");
            // 关闭输出流对象
            fos.close();
//            // 使用Android上下问获取当前项目的路径
//            File file2 = new File(this.getFilesDir(), "infoRequest.properties");
//            // 创建输出流对象
//            FileOutputStream fos2 = new FileOutputStream(file2);
//            // 创建属性文件对象
//            Properties pro2 = new Properties();
//            // 设置用户名或密码
//            pro2.setProperty("userName", username);
//            pro2.setProperty("requestName", "");
//            pro2.setProperty("reason","");
//            // 保存文件
//            pro2.store(fos, "infoRequest.properties");
//            // 关闭输出流对象
//            fos2.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
