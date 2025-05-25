package com.live2d.demo.full.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;

public class AlarmScheduler {

    // 요청 코드 구분: 푸시 알림용, 챗봇 알림용
    private static final int REQUEST_CODE_PUSH = 1001;
    private static final int REQUEST_CODE_CHATBOT = 1002;

    // 기존 Calendar를 사용하는 예약 방식 - 푸시 알림용으로 분리하여 재정의
    public static void schedulePushAlarm(Context context, Calendar calendar, String title) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Android S 이상: 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.w("AlarmScheduler", "정확한 알람 권한이 없어 설정 화면으로 이동");
                return;
            }
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.live2d.demo.PUSH_ALARM"); // 푸시 알림 액션 구분
        intent.putExtra("title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PUSH,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("AlarmScheduler", "푸시 알림 예약됨: " + calendar.getTime());
    }

    // 챗봇 알림 예약 함수 추가 (MainActivity 이동 + 사용자명 전달용)
    public static void scheduleChatbotAlarm(Context context, Calendar calendar, String userName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Android S 이상: 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.w("AlarmScheduler", "정확한 알람 권한이 없어 설정 화면으로 이동");
                return;
            }
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.live2d.demo.CHATBOT_ALARM"); // 챗봇 알림 액션 구분
        intent.putExtra("userName", userName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_CHATBOT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("AlarmScheduler", "챗봇 알림 예약됨: " + calendar.getTime());
    }

    // '며칠 전 + 시각' 방식으로 예약하는 함수 (푸시 알림용)
    public static void schedulePushAlarmWithOffset(Context context, long eventTimeInMillis, String title,
                                                   int daysBefore, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventTimeInMillis);
        calendar.add(Calendar.DATE, -daysBefore);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 과거 시간은 예약하지 않음
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "과거 시간으로 알림 예약 시도됨. 예약하지 않음.");
            return;
        }

        schedulePushAlarm(context, calendar, title);
    }

    // 챗봇 알림의 '며칠 전 + 시각' 방식 예약 함수 (필요 시 구현)
    public static void scheduleChatbotAlarmWithOffset(Context context, long eventTimeInMillis, String userName,
                                                      int daysBefore, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventTimeInMillis);
        calendar.add(Calendar.DATE, -daysBefore);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 과거 시간은 예약하지 않음
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "과거 시간으로 챗봇 알림 예약 시도됨. 예약하지 않음.");
            return;
        }

        scheduleChatbotAlarm(context, calendar, userName);
    }

    // 알림 취소 - 푸시 알림용
    public static void cancelPushAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.live2d.demo.PUSH_ALARM");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PUSH,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("AlarmScheduler", "푸시 알림 취소됨");
    }

    // 알림 취소 - 챗봇 알림용
    public static void cancelChatbotAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.live2d.demo.CHATBOT_ALARM");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_CHATBOT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("AlarmScheduler", "챗봇 알림 취소됨");
    }
}
