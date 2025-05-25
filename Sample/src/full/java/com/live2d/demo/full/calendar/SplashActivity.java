package com.live2d.demo.full.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 스플래시 테마 적용
        setTheme(R.style.Theme_Demo_Splash); // 테마명은 프로젝트에 맞게
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // 스플래시 레이아웃
        getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isFirstLaunch", true)
                .apply();
        boolean isFirstLaunch = isFirstLaunch();
        Log.d("SplashActivity", "isFirstLaunch: " + isFirstLaunch);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstLaunch()) {
                    // 최초 실행 시 이름 입력 화면으로 이동
                    Log.d("SplashActivity", "이름 입력 액티비티로 이동");
                    startActivity(new Intent(SplashActivity.this, NameInputActivity.class));
                } else {
                    // 이후 실행 시 메인 화면으로 이동
                    Log.d("SplashActivity", "메인 액티비티로 이동");
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish(); // 스플래시 액티비티 종료
            }
        }, 1500); // 1.5초 대기 후 실행
    }

    // 최초 실행 여부 확인
    private boolean isFirstLaunch() {
        return getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getBoolean("isFirstLaunch", true);
    }

    // 리셋 함수: 버튼 클릭 시 호출되어 SharedPreferences 초기화
//    private void resetFirstLaunch() {
//        getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//                .edit()
//                .putBoolean("isFirstLaunch", true)  // 최초 실행 상태로 설정
//                .apply();
//    }
}