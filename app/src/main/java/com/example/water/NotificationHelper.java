package com.example.water;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * 通知工具类 — 发送打卡成功通知
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "water_channel";
    private static final String CHANNEL_NAME = "饮水提醒";
    private static final int NOTIFICATION_ID = 1001;

    /**
     * 发送打卡成功的通知
     *
     * @param context 上下文
     * @param amount  本次饮水量
     * @param dailyTotal 当日累计饮水量
     */
    public static void showCheckInNotification(Context context, int amount, int dailyTotal) {
        // 1. 创建通知渠道（Android 8.0+ 必须）
        createNotificationChannel(context);

        // 2. 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("✅ 打卡成功")
                .setContentText("本次饮水 " + amount + " ml，今日共 " + dailyTotal + " ml")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("本次饮水 " + amount + " ml\n今日累计 " + dailyTotal + " ml\n继续加油！💪"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);   // 点击后自动消失

        // 3. 发送通知
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * 创建通知渠道
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("饮水打卡相关通知");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
