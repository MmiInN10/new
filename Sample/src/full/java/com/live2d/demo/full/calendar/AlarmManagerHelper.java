package com.live2d.demo.full.calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AlarmManagerHelper {

    // 알림 예약하는 정적 메서드
    public static void setAlarm(Context context, int requestCode, String title, long alarmTimeInMillis) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);

        // PendingIntent로 AlarmReceiver를 트리거
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("alarm_title_" + requestCode, title).apply();

        try {
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                );
                Log.d("AlarmManagerHelper", "알림 예약 시간: " + alarmTimeInMillis);
            }
        } catch (Exception e) {
            Log.e("AlarmManagerHelper", "알림 예약 실패: " + e.getMessage());
        }
    }
    public static class ChatbotAlarmHelper {
        private static final int REQUEST_CODE = 2001; // 기존 푸시 알림과 다른 코드

        public static void setChatbotAlarm(Context context, long alarmTimeInMillis) {
            Intent intent = new Intent(context, ChatbotAlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                );
                Log.d("ChatbotAlarmHelper", "챗봇 알림 예약됨: " + alarmTimeInMillis);
            }
        }
    }
    public static void cancelAllAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);

        for (int requestCode = 0; requestCode < 100; requestCode++) {
            String title = prefs.getString("alarm_title_" + requestCode, null);
            if (title == null) continue;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("title", title);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                prefs.edit().remove("alarm_title_" + requestCode).apply();
            }
        }
    }
}