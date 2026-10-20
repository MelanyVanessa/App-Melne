package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends BaseActivity {

    private static final long DURACION_MS = 1500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable irAInicio = () -> {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_splash);
        handler.postDelayed(irAInicio, DURACION_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(irAInicio);
        super.onDestroy();
    }
}
