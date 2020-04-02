package com.example.yanst.groupchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanst.groupchat.Entity.ChatMessage;
import com.example.yanst.groupchat.Service.MessagingService;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {
    private MessagingService messagingService;
    private int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagingService = new MessagingService();

        // Если пользователь не залогинен
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Запуск активити авторизации
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // Если пользователь авторизован - поприветствуем его всплывающим сообщением
            Toast.makeText(this,
                    getResources().getString(R.string.welcome) + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            // Загрузим сообщения
            displayChatMessages();
        }

        displayChatMessages();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Read Input
                EditText input = findViewById(R.id.input);

                // Create message
                ChatMessage chatMessage = new ChatMessage(input.getText().toString(),
                        FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getDisplayName());

                // Push notification
                messagingService.sendNotification(getApplicationContext(), chatMessage.getMessageText(), chatMessage.getMessageUser(), "message");

                // Query to database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(chatMessage);

                // Очищаем ввод
                input.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        super.onCreateOptionsMenu(m);
        getMenuInflater().inflate(R.menu.main_menu, m);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        getResources().getString(R.string.first_welcome),
                        Toast.LENGTH_LONG)
                        .show();
                FirebaseMessaging.getInstance().subscribeToTopic("test")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();

                                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                                    // The user is new, show them a fancy intro screen!
                                    messagingService.sendNotification(getApplicationContext(), "", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), "new_user");
                                }
                    }
                });

                displayChatMessages();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.sign_in_error),
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    getResources().getString(R.string.sign_out),
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }

        return true;
    }

    private void displayChatMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("HH:mm (dd-MM-yy)",
                        model.getMessageTime()));
            }
        };



        listOfMessages.setAdapter(adapter);
    }

    private void createNotification(ChatMessage chatMessage) {
        messagingService.sendNotification(getApplicationContext(), chatMessage.getMessageText(), chatMessage.getMessageUser(), "message");
    }
}
