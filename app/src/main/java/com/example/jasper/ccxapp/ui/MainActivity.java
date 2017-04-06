package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jasper.ccxapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText usernameChatToET;
    private Button beginChatBtn;
    private EditText txtMsgET;
    private Button sendTxtMsgBtn;
    private TextView chatContentTV;
    private ImageView chatImageView;
    private Button sendImgMsgBtn;

    private String usernameChatTo;
    private Conversation mConversation;
    private String txtMsg;
    private String chatContentStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        beginChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginChat();
            }
        });

        sendTxtMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTxt();
            }
        });

        sendImgMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

            }
        });

    }

    private void init() {

        JMessageClient.registerEventReceiver(this);

        usernameChatToET = (EditText) findViewById(R.id.to_chat_username);
        beginChatBtn = (Button) findViewById(R.id.begin_chat);
        txtMsgET = (EditText) findViewById(R.id.text_message);
        sendTxtMsgBtn = (Button) findViewById(R.id.send_message);
        chatContentTV = (TextView) findViewById(R.id.chat_content);
        chatImageView = (ImageView) findViewById(R.id.chat_Image);
        sendImgMsgBtn = (Button) findViewById(R.id.send_Img_btn);
    }

    private void beginChat() {
        usernameChatTo = usernameChatToET.getText().toString().trim();
        mConversation = Conversation.createSingleConversation(usernameChatTo);
    }

    private void sendTxt() {
        txtMsg = txtMsgET.getText().toString().trim();
        //Message msg = JMessageClient.createSingleTextMessage(usernameChatTo,txtMsg);
        Message message = mConversation.createSendMessage(new TextContent(txtMsg));
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseDesc) {
                if (responseCode == 0) {
                    //消息发送成功
                    Log.i("test", "文本发送成功");
                } else {
                    //消息发送失败
                    Log.e("test", "文本发送失败");
                }
            }
        });

        JMessageClient.sendMessage(message);
    }

    private void sendImg(Bitmap bitmap) {
        Message message = mConversation.createSendMessage(new ImageContent(bitmap));
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseDesc) {
                if (responseCode == 0) {
                    //消息发送成功
                    Log.i("test", "图片发送成功");

                } else {
                    //消息发送失败
                    Log.e("test", "图片发送失败");
                }
            }
        });
        JMessageClient.sendMessage(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            // 获取相机返回的数据，并转换为Bitmap图片格式 ，这是缩略图
            Bitmap bitmap = (Bitmap) bundle.get("data");

            chatImageView.setImageBitmap(bitmap);

            sendImg(bitmap);
        }
    }

    private void sendFile() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }


    private Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        File file = new File(pathString);
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(pathString);
        } else {
            Log.e("test", "该图片不存在");
        }
        return bitmap;
    }

    public void onEventMainThread(MessageEvent event) {
        Message msg = event.getMessage();

        switch (msg.getContentType()) {
            case text:
                //处理文字消息
                Log.i("test", "收到文本消息");
                TextContent textContent = (TextContent) msg.getContent();
                chatContentStr += textContent.getText();
                chatContentTV.setText(chatContentStr);
                break;
            case image:
                Log.i("test", "收到图片消息");
                ImageContent imageContent = (ImageContent) msg.getContent();
                imageContent.downloadOriginImage(msg, new DownloadCompletionCallback() {
                    @Override
                    public void onComplete(int i, String s, File file) {
                        Log.i("test", "收到的图片下载完成");
                        Bitmap img = getDiskBitmap(file.getPath());//图片本地地址
                        chatImageView.setImageBitmap(img);
                    }
                });

        }
    }

    /**
     * 类似MessageEvent事件的接收，上层在需要的地方增加OfflineMessageEvent事件的接收
     * 即可实现离线消息的接收。
     **/
    public void onEvent(OfflineMessageEvent event) {
        //获取事件发生的会话对象
        Conversation conversation = event.getConversation();
        List<Message> newMessageList = event.getOfflineMessageList();//获取此次离线期间会话收到的新消息列表
        System.out.println(String.format(Locale.SIMPLIFIED_CHINESE, "收到%d条来自%s的离线消息。\n", newMessageList.size(), conversation.getTargetId()));
    }


    /**
     * 如果在JMessageClient.init时启用了消息漫游功能，则每当一个会话的漫游消息同步完成时
     * sdk会发送此事件通知上层。
     **/
    public void onEvent(ConversationRefreshEvent event) {
        //获取事件发生的会话对象
        Conversation conversation = event.getConversation();
        //获取事件发生的原因，对于漫游完成触发的事件，此处的reason应该是
        //MSG_ROAMING_COMPLETE
        ConversationRefreshEvent.Reason reason = event.getReason();
        System.out.println(String.format(Locale.SIMPLIFIED_CHINESE, "收到ConversationRefreshEvent事件,待刷新的会话是%s.\n", conversation.getTargetId()));
        System.out.println("事件发生的原因 : " + reason);
    }

}

