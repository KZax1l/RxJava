package org.core.library;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.List;

/**
 * Created by Zsago on 2016/7/7.
 */
public class NotificationBase {
    private Context context;
    /**
     * Notification构造器
     */
    private NotificationCompat.Builder mBuilder;
    /**
     * Notification管理
     */
    private NotificationManager mNotificationManager;
    /**
     * Notification的ID
     */
    public static final int NOTIFY_ID = 10010;

    public NotificationBase(Context context) {
        if (context == null) {
            throw new NullPointerException("context can not be null!");
        }
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
    }

    /**
     * 初始化通知栏
     */
    void initNotify(int resId) {
        mBuilder.setContentTitle("测试标题")
                .setContentText("测试内容")
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
//				.setNumber(number)//显示数量
                .setTicker("测试通知来啦")//通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
//				.setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(resId);
    }

    /**
     * @param activity      点击通知项后跳转的页面
     * @param contentTitle
     * @param contentText
     * @param contentTicker
     */
    public void showIntentActivityNotify(Class activity, String contentTitle, String contentText, String contentTicker, int resId) {
        // Notification.FLAG_ONGOING_EVENT --设置常驻 Flag;Notification.FLAG_AUTO_CANCEL 通知栏上点击此通知后自动清除此通知
//		notification.flags = Notification.FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
        mBuilder.setAutoCancel(true)//点击后让通知将消失
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(contentTicker)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setOngoing(false)
                .setSmallIcon(resId);
        //点击的意图ACTION是跳转到Intent
        Intent resultIntent = new Intent(context, activity);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
     * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT
     * 点击去除： Notification.FLAG_AUTO_CANCEL
     */
    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(), flags);
        return pendingIntent;
    }

    /**
     * 用于判断当前显示的Activity是否是传参的Activity
     *
     * @param context
     * @param topActivity
     * @return
     */
    private boolean isAppOnForeground(Context context, Class topActivity) {
        if (context == null) return false;
        ActivityManager mActivityManager = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> taskInfos = mActivityManager.getRunningTasks(1);
        if (taskInfos.size() > 0) {
            String className = taskInfos.get(0).topActivity.getClassName();
            if (className.equals(topActivity.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除当前创建的通知栏
     */
    public void clearNotify(int notifyId) {
        mNotificationManager.cancel(notifyId);//删除一个特定的通知ID对应的通知
//		mNotification.cancel(getResources().getString(R.string.app_name));
    }

    /**
     * 清除所有通知栏
     */
    public void clearAllNotify() {
        mNotificationManager.cancelAll();// 删除你发的所有通知
    }
}
