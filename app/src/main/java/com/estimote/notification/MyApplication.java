package com.estimote.notification;

import android.app.Application;

import com.estimote.cloud_plugin.common.EstimoteCloudCredentials;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.estimote.notification.Util.Costants;
import com.estimote.notification.Util.NotificationsManager;


public class MyApplication extends Application {

    public CloudCredentials cloudCredentials = new EstimoteCloudCredentials("light-on-oxn", Costants.APP_TOKEN);
    private NotificationsManager notificationsManager;

    public void enableBeaconNotifications() {
        notificationsManager = new NotificationsManager(this);
        notificationsManager.startMonitoring();
    }

}
