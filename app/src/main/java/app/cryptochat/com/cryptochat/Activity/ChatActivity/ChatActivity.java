package app.cryptochat.com.cryptochat.Activity.ChatActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.cryptochat.com.cryptochat.Manager.AuthManager;
import app.cryptochat.com.cryptochat.Manager.ChatManager;
import app.cryptochat.com.cryptochat.Manager.TransportStatus;
import app.cryptochat.com.cryptochat.Models.MessageModel;
import app.cryptochat.com.cryptochat.Models.MyUserModel;
import app.cryptochat.com.cryptochat.R;
import app.cryptochat.com.cryptochat.Tools.EndlessScrollListener;

public class ChatActivity extends AppCompatActivity {
    ListView listViewMessages;
    ArrayList<ChatViewModel> chatList = new ArrayList<>();
    ArrayList<MessageModel> messageList = new ArrayList<>();
    ChatAdapter chatAdapter;
    ChatManager chatManager = new ChatManager();
    AuthManager authManager = new AuthManager();
    ChatViewModel chatViewModel;

    private int offset = 0;
    private int limit = 15;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 0);
        userName = intent.getStringExtra("userName");

        MyUserModel myUser = authManager.getMyUser();
        TextView textViewInformation = (TextView) findViewById(R.id.textViewInfomation);
        chatManager.getHistoryUser(myUser.getToken(), userId, limit, offset, (s, t) -> {
            if(t == TransportStatus.TransportStatusSuccess){
                messageList = s;
                for(MessageModel messageModel : s){
                    chatViewModel = new ChatViewModel(messageModel);
                    chatList.add(chatViewModel);
                }
                chatAdapter.notifyDataSetChanged();
                if(s.size() == 0){
                    textViewInformation.setText("У Вас пока нет сообщений");
                }else {
                    textViewInformation.setText("");
                }
            }else if (t == TransportStatus.TransportStatusNotInternet){
                Toast.makeText(this, "Проверьте интернет соединение", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Возникла ошибка", Toast.LENGTH_SHORT).show();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(userName);

        EditText inputMsg = (EditText) findViewById(R.id.inputMsg);
        // Listener EditText
        inputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button sendButton = (Button) findViewById(R.id.btnSend);
        // Отправить сообщение
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(inputMsg.getText().toString());
                inputMsg.setText("");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        listViewMessages = (ListView) findViewById(R.id.listView);
        listViewMessages.setRotation(180);
        chatAdapter = new ChatAdapter(this, chatList);
        listViewMessages.setAdapter(chatAdapter);


        // Подгрузка
        listViewMessages.setOnScrollListener(new EndlessScrollListener(){
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                offset = totalItemsCount;
                chatManager.getHistoryUser(myUser.getToken(), userId, limit, offset, (s, t) -> {
                    if(t == TransportStatus.TransportStatusSuccess){
                        messageList = s;
                        for(MessageModel messageModel : s) {
                            ChatViewModel chatViewModel = new ChatViewModel(messageModel);
                            chatList.add(chatViewModel);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });
    }

    private void addMessage(String message, boolean fromMe) {
        chatAdapter.add(new ChatViewModel(message, fromMe, null));
        chatAdapter.notifyDataSetChanged();
    }

    private void sendMessage(String message) {
        if (message.length() == 0) return;

        addMessage(message, true);
    }
}
