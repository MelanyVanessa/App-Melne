package com.example.myapp;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/** Apariencia elegida en Perfil. Por defecto la app usa el modo claro. */
public final class Tema {

    public static final int CLARO = 0;
    public static final int OSCURO = 1;
    public static final int SISTEMA = 2;

    private static final String PREFS = "preferencias";
    private static final String CLAVE = "tema";

    private Tema() {
    }

    public static int actual(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(CLAVE, CLARO);
    }

    public static void guardarYAplicar(Context context, int tema) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(CLAVE, tema).apply();
        aplicar(tema);
    }

    public static void aplicar(int tema) {
        int modo;
        if (tema == OSCURO) {
            modo = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (tema == SISTEMA) {
            modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
            modo = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
