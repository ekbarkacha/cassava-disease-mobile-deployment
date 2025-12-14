/*
 * Project: CassavaCare
 * File: SplashActivity.java
 * Description: Splash screen activity that handles initial app setup, edge-to-edge display,
 *              and navigation to MainActivity after any heavy initialization.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Run heavy stuff in background
        new Thread(() -> {
            // Heavy initialization
            //loadModel();
            //preloadData();

            // After done, go to main on UI thread
            runOnUiThread(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            });
        }).start();
    }
}