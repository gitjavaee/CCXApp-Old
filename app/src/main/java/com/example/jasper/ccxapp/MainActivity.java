package com.example.jasper.ccxapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {

    private TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);

        Bmob.initialize(this, "11c7be8b5fad9ba573d2641f495dbc48");
    }

    public void CreateUser(View view) {
        final Person person = new Person();
        person.setUsername("Jasper");
        person.setPassword("123456");
        person.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(MainActivity.this, "添加数据成功，返回objectId为：" + objectId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "创建数据失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void GetUser(View view) {
        BmobQuery<Person> personQuery = new BmobQuery<Person>();
        personQuery.setLimit(50);
        personQuery.findObjects(new FindListener<Person>() {
            @Override
            public void done(List<Person> object, BmobException e) {
                if (e == null){
                    Toast.makeText(MainActivity.this, "查询成功：共"+object.size()+"条数据。", Toast.LENGTH_SHORT).show();
                    for (Person person : object){
                        text.append(person.getUsername()+" " +person.getPassword());
                    }

                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }
}

