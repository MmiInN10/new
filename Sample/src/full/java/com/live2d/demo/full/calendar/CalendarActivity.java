package com.live2d.demo.full.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;
import com.live2d.demo.full.SettingsActivity;

public class CalendarActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar); // XML 파일 이름에 맞게 수정

        ImageButton buttonMain = findViewById(R.id.button_main);
        ImageButton buttonCalendar = findViewById(R.id.button_calendar);
        ImageButton buttonSetting = findViewById(R.id.button_setting);

        buttonMain.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        buttonCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, CalendarActivity.class);
        });

        buttonSetting.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
