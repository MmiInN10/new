package com.live2d.demo.full.calendar;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;
import com.live2d.demo.full.utils.GoogleCalendarHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddActivity extends BaseActivity {
    private TextView tvSelectedDate, tvStartTime, tvEndTime, tvAlarmTime;
    private EditText editTextEventTitle;
    private ImageView btnConfirm;
    private Switch switchTime;
    private LinearLayout timePickerLayout;

    private int selectedAlarmHour = 9;
    private int selectedAlarmMinute = 0;
    private int selectedAlarmDaysBefore = 0;

    private String selectedStartTime = "10:00";
    private String selectedEndTime = "11:00";

    private boolean isTimeSet = false;

    private GoogleCalendarHelper googleCalendarHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        getSupportActionBar().hide();

        tvSelectedDate = findViewById(R.id.tv_selected_date);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        btnConfirm = findViewById(R.id.btn_confirm);
        switchTime = findViewById(R.id.switch_time);
        timePickerLayout = findViewById(R.id.time_picker_layout);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvAlarmTime = findViewById(R.id.tv_alarm_time);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ImageView ivDateForward = findViewById(R.id.ivDateForward);
        ImageView ivAlarmForward = findViewById(R.id.ivAlarmForward);
        LinearLayout alarmSettingLayout = findViewById(R.id.alarm_setting_layout);

        googleCalendarHelper = new GoogleCalendarHelper(this);

        switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timePickerLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            isTimeSet = isChecked;
        });

        tvStartTime.setOnClickListener(v -> showMaterialTimePicker(true));
        tvEndTime.setOnClickListener(v -> showMaterialTimePicker(false));

        btnConfirm.setOnClickListener(v -> {
            String title = editTextEventTitle.getText().toString();
            // 일정 제목이 비어있거나 날짜가 선택되지 않았으면 토스트 띄우고 return
            if (title.isEmpty() || tvSelectedDate.getText().toString().isEmpty()) {
                showCustomToast("일정 제목과 날짜를 입력하세요.");
                return;
            }
            saveEventToGoogleCalendar(title);

            if (!tvAlarmTime.getText().toString().equals("시간 설정")) {
                scheduleAlarm(title);
            }
//            if (switchChatbotAlarm.isChecked()) {
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//            }
        });

        ivDateForward.setOnClickListener(v -> showDatePickerDialog());

        boolean isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (!isEditMode) {
            String title = editTextEventTitle.getText().toString();
            String date = tvSelectedDate.getText().toString();
            String eventId = title + "_" + date;

            // 개별 이벤트 알람 시간도 초기화
            prefs.edit()
                    .remove("alarm_hour_" + eventId)
                    .remove("alarm_minute_" + eventId)
                    .remove("alarm_days_before_" + eventId)
                    .apply();
            tvAlarmTime.setText("시간 설정");
        }

        final boolean[] isAlarmSettingVisible = {false};
        ivAlarmForward.setOnClickListener(v -> {
            isAlarmSettingVisible[0] = !isAlarmSettingVisible[0];
            alarmSettingLayout.setVisibility(isAlarmSettingVisible[0] ? View.VISIBLE : View.GONE);
        });

        String title = editTextEventTitle.getText().toString();
        String date = tvSelectedDate.getText().toString();
        String eventId = title + "_" + date;

        int savedHour = prefs.getInt("alarm_hour_" + eventId, -1);
        int savedMinute = prefs.getInt("alarm_minute_" + eventId, -1);
        selectedAlarmDaysBefore = prefs.getInt("alarm_days_before_" + eventId, 0);

        tvAlarmTime.setText((savedHour != -1 && savedMinute != -1) ?
                String.format("%02d:%02d", savedHour, savedMinute) : "시간 설정");

        tvAlarmTime.setOnClickListener(v -> showAlarmTimePickerBottomSheet());

        int hour = getIntent().getIntExtra("alarm_hour", -1);
        int minute = getIntent().getIntExtra("alarm_minute", -1);
        if (hour != -1 && minute != -1) {
            tvAlarmTime.setText(String.format("%02d:%02d", hour, minute));
            prefs.edit().putInt("alarm_hour", hour).putInt("alarm_minute", minute).apply();
        }
        ImageView ivCancel = findViewById(R.id.ivCancel);
        ivCancel.setOnClickListener(v -> finish());

    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvSelectedDate.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showAlarmTimePickerBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottomsheet_alarm_time_picker, null);
        TimePicker timePicker = bottomSheetView.findViewById(R.id.time_picker);
        Button btnConfirm = bottomSheetView.findViewById(R.id.btn_confirm_time);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            selectedAlarmHour = hour;
            selectedAlarmMinute = minute;

            tvAlarmTime.setText(String.format("%02d:%02d", hour, minute));

            String title = editTextEventTitle.getText().toString();
            String date = tvSelectedDate.getText().toString();
            String eventId = title + "_" + date;

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt("alarm_hour_" + eventId, hour)
                    .putInt("alarm_minute_" + eventId, minute)
                    .apply();

            View customToastView = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
            TextView textView = customToastView.findViewById(R.id.toast_text);
            textView.setText(String.format("알림 시간 설정: %02d:%02d", hour, minute));

            Toast toast = new Toast(this);
            toast.setView(customToastView);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();

            dialog.dismiss();
        });
    }

    private void showMaterialTimePicker(boolean isStart) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(10)
                .setMinute(0)
                .setTitleText(isStart ? "시작 시간 선택" : "종료 시간 선택")
                .build();

        picker.show(getSupportFragmentManager(), isStart ? "startTimePicker" : "endTimePicker");

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            String formattedTime = String.format("%02d:%02d", hour, minute);

            if (isStart) {
                selectedStartTime = formattedTime;
                tvStartTime.setText("시작: " + formattedTime);
            } else {
                selectedEndTime = formattedTime;
                tvEndTime.setText("종료: " + formattedTime);
            }
        });
    }

    private void saveEventToGoogleCalendar(String title) {
        String selectedDateStr = tvSelectedDate.getText().toString();
        if (selectedDateStr.isEmpty()) {
            showCustomToast("날짜를 선택하세요.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date selectedDate = sdf.parse(selectedDateStr);

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
                calendar.setTime(selectedDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                DateTime startDateTime;
                DateTime endDateTime;

                if (isTimeSet) {
                    String[] startParts = selectedStartTime.split(":");
                    String[] endParts = selectedEndTime.split(":");

                    Calendar startCal = (Calendar) calendar.clone();
                    startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
                    startCal.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

                    Calendar endCal = (Calendar) calendar.clone();
                    endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
                    endCal.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));

                    startDateTime = new DateTime(startCal.getTime(), TimeZone.getTimeZone("Asia/Seoul"));
                    endDateTime = new DateTime(endCal.getTime(), TimeZone.getTimeZone("Asia/Seoul"));
                }else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    String startDateStr = dateFormat.format(calendar.getTime());

                    Calendar endCal = (Calendar) calendar.clone();
                    endCal.add(Calendar.DAY_OF_MONTH, 1);
                    String endDateStr = dateFormat.format(endCal.getTime());

                    startDateTime = new DateTime(startDateStr);
                    endDateTime = new DateTime(endDateStr);
                }
                googleCalendarHelper.insertEvent(title, startDateTime, endDateTime, !isTimeSet, new GoogleCalendarHelper.InsertEventCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    showCustomToast("일정이 저장되었습니다.");
                                    if (!"시간 설정".equals(tvAlarmTime.getText().toString())) {
                                        PreferenceManager.getDefaultSharedPreferences(AddActivity.this).edit()
                                                .putInt("alarm_hour", selectedAlarmHour)
                                                .putInt("alarm_minute", selectedAlarmMinute)
                                                .apply();
                                    }
                                    finish();
                                });
                            }
                            @Override
                            public void onFailure(Exception e) {
                                if (e instanceof UserRecoverableAuthIOException) {
                                    startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), 9999);
                                } else {
                                    runOnUiThread(() ->
                                            showCustomToast("일정 저장 실패: " + e.getMessage())
                                    );
                                }
                            }
                        }
                );

            } catch (Exception e) {
                runOnUiThread(() -> showCustomToast("일정 저장 실패"));
            }
        });
    }

    private void scheduleAlarm(String title) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedDateStr = tvSelectedDate.getText().toString();
        String eventId = title + "_" + selectedDateStr;

        int hour = prefs.getInt("alarm_hour_" + eventId, 9);
        int minute = prefs.getInt("alarm_minute_" + eventId, 0);

        if (!selectedDateStr.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                calendar.setTime(sdf.parse(selectedDateStr));
            } catch (Exception e) {
                calendar = Calendar.getInstance();
            }
            calendar.add(Calendar.DATE, -selectedAlarmDaysBefore);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            AlarmScheduler.scheduleAlarm(this, calendar, title);
        }
    }

    private void cancelScheduledAlarm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String title = editTextEventTitle.getText().toString();
        String date = tvSelectedDate.getText().toString();
        String eventId = title + "_" + date;

        int alarmHour = prefs.getInt("alarm_hour_" + eventId, -1);
        int alarmMinute = prefs.getInt("alarm_minute_" + eventId, -1);

        if (alarmHour != -1 && alarmMinute != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
            calendar.set(Calendar.MINUTE, alarmMinute);
            calendar.set(Calendar.SECOND, 0);

            AlarmScheduler.cancelAlarm(this);
            prefs.edit().remove("alarm_hour").remove("alarm_minute").apply();

            showCustomToast("알림이 취소되었습니다.");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9999 && resultCode == RESULT_OK) {
            // 권한 동의 후 다시 일정 저장
            String title = editTextEventTitle.getText().toString();
            saveEventToGoogleCalendar(title);  // 일정 저장 재시도
        }
    }
}