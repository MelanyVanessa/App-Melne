package com.example.myapp;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

/** Base para las pantallas de administración: exigen haber iniciado sesión como admin. */
public abstract class AdminActivity extends BaseActivity {

    /**
     * Llamar al inicio de onCreate. Si no hay sesión, redirige al login (conservando los
     * extras para volver luego a esta pantalla), cierra la actual y devuelve false.
     */
    protected boolean verificarSesion() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return true;
        }
        Intent login = new Intent(this, LoginActivity.class);
        if (getIntent().getExtras() != null) {
            login.putExtras(getIntent().getExtras());
        }
        login.putExtra(LoginActivity.EXTRA_DESTINO, getClass().getName());
        startActivity(login);
        finish();
        return false;
    }

    /** Abre una pantalla de admin; si no hay sesión, pasa primero por el login. */
    public static void abrir(Activity desde, Class<? extends AdminActivity> destino) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            desde.startActivity(new Intent(desde, destino));
        } else {
            desde.startActivity(new Intent(desde, LoginActivity.class)
                    .putExtra(LoginActivity.EXTRA_DESTINO, destino.getName()));
        }
    }

    protected void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
}
