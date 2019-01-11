package eu.mydayyy.tictactoebot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;


public class Util implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static String MODULE_PATH = null;
    private static int proxySmallIconId = 0;

    private static String CHANNEL_ID = "eu.mydayyy.tictactoebot.xposeddebuglogger";
    private static int notificationId = 1;
    //private static Context notificationContext;

    static void delay(Runnable r, int delay) {
        new Handler(Looper.getMainLooper()).postDelayed(r, delay);
    }
    static void delay(Runnable r) {
        delay(r, 1000);
    }

    static void sendLocalNotification(String message, Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID);

        builder.setSmallIcon(proxySmallIconId);
        builder.setContentText(message);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        initializeNotificationChannel(nm, builder);

        if (nm != null) {
            nm.notify(notificationId++, builder.build());
        } else {
            XposedBridge.log("Cannot push notification. NotificationManager is null.");
        }
    }

    private static void initializeNotificationChannel(NotificationManager nm, NotificationCompat.Builder builder) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "XposedDebugNotification",
                NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(channel);
        builder.setChannelId(CHANNEL_ID);
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if(!resparam.packageName.equals("it.megasoft78.trispad.android")) {
            return;
        }

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        proxySmallIconId = resparam.res.addResource(modRes, R.mipmap.ic_launcher);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }
}
