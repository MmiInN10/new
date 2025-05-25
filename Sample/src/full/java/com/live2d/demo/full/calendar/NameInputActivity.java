package com.live2d.demo.full.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;

public class NameInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_input);

        EditText etName = findViewById(R.id.etName);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        getSupportActionBar().hide();

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstLaunch", false)
                        .putString("userName", name)
                        .apply();

                Intent intent = new Intent(NameInputActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NameInputActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}