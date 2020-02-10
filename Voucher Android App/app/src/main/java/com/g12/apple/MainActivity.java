package com.g12.apple;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.g12.apple.appleTvLogin.AppleTvLoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchLogin(View view) {
        Intent intent = new Intent(this, AppleTvLoginActivity.class);
        startActivity(intent);
    }
}
