package com.example.myapp;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Base común: activa edge-to-edge y aplica los insets del sistema a la vista R.id.main. */
public abstract class BaseActivity extends AppCompatActivity {

    protected void configurarPantalla(@LayoutRes int layout) {
        EdgeToEdge.enable(this);
        setContentView(layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Mientras se escribe, la barra inferior se oculta para no subir con el teclado.
            boolean tecladoVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            ocultarConTeclado(R.id.bottomNavigation, tecladoVisible);
            ocultarConTeclado(R.id.adminBottomNavigation, tecladoVisible);
            // Ya se aplicaron a la raíz: así la barra inferior no vuelve a sumar el margen del sistema.
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void ocultarConTeclado(@IdRes int id, boolean tecladoVisible) {
        View vista = findViewById(id);
        if (vista != null) {
            vista.setVisibility(tecladoVisible ? View.GONE : View.VISIBLE);
        }
    }
}
