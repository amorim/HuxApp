package org.lamorim.huxflooderapp.notification;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.activity.MainActivity;
import org.lamorim.huxflooderapp.utility.HttpRequest;

import java.util.HashMap;

/**
 * Created by lucas on 24/12/2016.
 */

public class NotificationIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_DELETE = "ACTION_DELETE";

    public NotificationIntentService() {
        super(NotificationIntentService.class.getSimpleName());
    }

    public static Intent createIntentStartNotificationService(Context context) {
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent createIntentDeleteNotification(Context context) {
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.setAction(ACTION_DELETE);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "onHandleIntent, started handling a notification event");
        try {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                processStartNotification();
            }
            if (ACTION_DELETE.equals(action)) {
                processDeleteNotification(intent);
            }
        } finally {
            TaskBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void processDeleteNotification(Intent intent) {

    }

    private void processStartNotification() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (MainActivity.IS_FOREGROUND || !prefs.getBoolean("checkbox_notification", false))
            return;
        try {
            SharedPreferences sp = getSharedPreferences("credentials", Context.MODE_PRIVATE);
            String username = sp.getString("username", "");
            if (username.isEmpty())
                return;
            while (true) {
                if (!prefs.getBoolean("checkbox_notification", false))
                    break;
                if (sp.getString("username", "").isEmpty())
                    break;
                HttpRequest req = new HttpRequest("https://casaamorim.no-ip.biz:5053/jobs/getStatistic");
                HashMap<String, String> params =new HashMap<>();
                params.put("username", username);
                JSONObject json = req.prepare(HttpRequest.Method.POST).withData(params).sendAndReadJSON();
                int totTasks = json.getInt("totalRunningJobs"), totProgress = json.getInt("totalProgress");
                if (totTasks == 0) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(NOTIFICATION_ID);
                    break;
                }
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this);
                mBuilder.setSmallIcon(R.drawable.ic_notif);
                String title = getResources().getQuantityString(R.plurals.notification_title, totTasks, totTasks);
                String text = String.format(getString(R.string.notification_text), totProgress);
                mBuilder.setOngoing(true);
                mBuilder.setContentTitle(title);
                mBuilder.setContentText(text);
                mBuilder.setAutoCancel(true);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_hux_big);
                mBuilder.setLargeIcon(largeIcon);
                mBuilder.setColor(ContextCompat.getColor(this, R.color.huxBlue));
                mBuilder.setProgress(100, totProgress, false);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        NOTIFICATION_ID,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setDeleteIntent(TaskBroadcastReceiver.getDeleteIntent(this));
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (!MainActivity.IS_FOREGROUND)
                mNotificationManager.notify(1, mBuilder.build());
                else
                break;
                Thread.sleep(10000);
            }
        }
        catch (java.io.IOException ex) {
            Log.e("Exceção", "Ocorreu uma exceção ao tentar fazer o download de informações do servidor. Verifique a conexão e compatibilidade de métodos. Mensagem de erro: " + ex.getMessage());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
