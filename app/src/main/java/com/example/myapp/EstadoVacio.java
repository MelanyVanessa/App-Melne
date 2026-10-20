package com.example.myapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.android.material.button.MaterialButton;

/** Controla la vista incluida desde vista_estado_vacio.xml. */
public class EstadoVacio {

    private final View raiz;
    private final ImageView icono;
    private final TextView titulo, mensaje;
    private final MaterialButton accion;

    public EstadoVacio(View raiz) {
        this.raiz = raiz;
        icono = raiz.findViewById(R.id.imgEstado);
        titulo = raiz.findViewById(R.id.tvEstadoTitulo);
        mensaje = raiz.findViewById(R.id.tvEstadoMensaje);
        accion = raiz.findViewById(R.id.btnEstadoAccion);
    }

    /** Muestra el estado. Si textoAccion es 0 no se muestra botón. */
    public void mostrar(@DrawableRes int iconoRes, @StringRes int tituloRes, CharSequence mensajeTexto,
                        @StringRes int textoAccion, View.OnClickListener alPulsar) {
        icono.setImageResource(iconoRes);
        titulo.setText(tituloRes);
        mensaje.setText(mensajeTexto);
        if (textoAccion != 0) {
            accion.setText(textoAccion);
            accion.setOnClickListener(alPulsar);
            accion.setVisibility(View.VISIBLE);
        } else {
            accion.setVisibility(View.GONE);
        }
        raiz.setVisibility(View.VISIBLE);
    }

    public void ocultar() {
        raiz.setVisibility(View.GONE);
    }
}
