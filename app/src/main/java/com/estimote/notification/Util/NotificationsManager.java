package com.estimote.notification.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.estimote.internal_plugins_api.cloud.proximity.ProximityAttachment;
import com.estimote.notification.MainActivity;
import com.estimote.notification.MyApplication;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;

import cz.msebera.android.httpclient.Header;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import com.loopj.android.http.*;
public class NotificationsManager {

    private Context context;
    private NotificationManager notificationManager;
    private Notification helloNotification;
    private Notification goodbyeNotification;
    private int notificationId = 1;
    private final AsyncHttpClient client = new AsyncHttpClient();
    private final String BASE_URL_ON = Costants.HOME_URL + "/server/Ciabatta/ON";
    private final String BASE_URL_OFF = Costants.HOME_URL + "/server/Ciabatta/OFF";

    public NotificationsManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.helloNotification = buildNotification("Hello", "You're near your beacon, I'm turning on the light");
        this.goodbyeNotification = buildNotification("Bye bye", "You've left the proximity of your beacon, I'm turning off the light");
    }

    private Notification buildNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        return new NotificationCompat.Builder(context, "content_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    public void startMonitoring() {
        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, ((MyApplication) context).cloudCredentials)
                        .withOnErrorAction(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .build();

        ProximityZone zone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("light-on-oxn", "example-proximity-zone")
                .inCustomRange(3.0)
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        notificationManager.notify(notificationId, helloNotification);
                        client.get(BASE_URL_ON, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Toast toast = Toast.makeText(context, "SUCCESS!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast toast = Toast.makeText(context, "FAILED!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        return null;
                    }
                })
                .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        notificationManager.notify(notificationId, goodbyeNotification);
                        client.get(BASE_URL_OFF, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Toast toast = Toast.makeText(context, "SUCCESS!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast toast = Toast.makeText(context, "FAILED!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        return null;
                    }
                })
                .create();
        proximityObserver.addProximityZone(zone);
        proximityObserver.start();

    }
}
