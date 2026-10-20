package com.example.myapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MelneApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Guarda en el teléfono lo último descargado: el catálogo se ve aunque no haya internet.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Tema.aplicar(Tema.actual(this));
    }
}
