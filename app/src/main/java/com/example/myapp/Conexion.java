package com.example.myapp;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Avisa cuando no hay conexión con Firebase. Espera unos segundos antes de avisar
 * para no mostrar el mensaje durante la conexión inicial.
 */
public class Conexion {

    private static final long ESPERA_MS = 4000;

    private final View raiz;
    private final View ancla;
    private final Runnable alPerderConexion;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DatabaseReference estado = FirebaseDatabase.getInstance().getReference(".info/connected");
    private Snackbar aviso;
    private boolean conectado;

    private final Runnable avisar = new Runnable() {
        @Override
        public void run() {
            if (conectado) {
                return;
            }
            aviso = Snackbar.make(raiz, R.string.sin_conexion_aviso, Snackbar.LENGTH_INDEFINITE);
            if (ancla != null && ancla.getVisibility() == View.VISIBLE) {
                aviso.setAnchorView(ancla);
            }
            aviso.show();
            alPerderConexion.run();
        }
    };

    private final ValueEventListener escucha = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            conectado = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
            handler.removeCallbacks(avisar);
            if (conectado) {
                if (aviso != null) {
                    aviso.dismiss();
                    aviso = null;
                }
            } else {
                handler.postDelayed(avisar, ESPERA_MS);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    public Conexion(View raiz, View ancla, Runnable alPerderConexion) {
        this.raiz = raiz;
        this.ancla = ancla;
        this.alPerderConexion = alPerderConexion;
    }

    public void iniciar() {
        estado.addValueEventListener(escucha);
    }

    public void detener() {
        estado.removeEventListener(escucha);
        handler.removeCallbacks(avisar);
    }

    public boolean estaConectado() {
        return conectado;
    }
}
