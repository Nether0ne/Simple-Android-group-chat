package com.example.yanst.groupchat.Service;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.yanst.groupchat.API.API;
import com.example.yanst.groupchat.API.NetworkClient;
import com.example.yanst.groupchat.Entity.NotificationData;
import com.example.yanst.groupchat.Entity.RequestNotification;
import com.example.yanst.groupchat.MainActivity;
import com.example.yanst.groupchat.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("DEBUG-TAG", "Notification received!");

        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);

        // Если приложение свернуто
        if (myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            showNotification(remoteMessage);
        }
    }

    public void showNotification(RemoteMessage msg) {
            String title = msg.getData().get("title"),
                    body =  msg.getData().get("body");

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("title",  title);
            intent.putExtra("body",  body);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(largeIcon)
                    .setColor(Color.parseColor("#4B8A08"))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_sound))
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.MAGENTA, 500, 1000)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = getString(R.string.default_notification_channel_id);
                NotificationChannel channel = new NotificationChannel(channelId,   title, NotificationManager.IMPORTANCE_MAX);
                channel.setDescription(body);
                notificationManager.createNotificationChannel(channel);
                notificationBuilder.setChannelId(channelId);
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

            Log.d("DEBUG-TAG", "Notification was shown!");
    }

    public void sendNotification(Context c, String msg, String user, String type)
    {
        NotificationData notificationData = new NotificationData(user, msg, "", type);
        notificationData.setBody(buildBody(notificationData));
        notificationData.setTitle(buildTitle(notificationData));

        RequestNotification requestNotification = new RequestNotification();
        //token is id , whom you want to send notification ,
        requestNotification.setToken("/topics/test");
        requestNotification.setNotificationData(notificationData);

        Retrofit retrofit = NetworkClient.getRetrofitClient();
        API api = retrofit.create(API.class);
        Call call = api.sendChatNotification(requestNotification);


        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("DEBUG-TAG", "Notification send!");
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("DEBUG-TAG", "Notification failed :(");
            }
        });

    }

    private String buildTitle(NotificationData data)
    {
        String title = "";

        switch(data.getType())
        {
            case "new_user":
                title = buildBody(data);
                break;
            case "message":
                title = "Нове повідомлення від " + data.getUser();
                break;
            default:
                break;
        }

        return title;
    }

    private String buildBody(NotificationData data)
    {
        String body = data.getUser();

        switch(data.getType())
        {
            case "new_user":
                body += " приєднався до чату!";
                break;
            case "message":
                body += ": " + data.getBody();
                break;
            default:
                break;
        }

        return body;
    }
}
