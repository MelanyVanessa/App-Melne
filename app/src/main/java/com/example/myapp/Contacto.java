package com.example.myapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/** Abre WhatsApp, Instagram y mapas con los datos de la tienda definidos en strings.xml. */
public final class Contacto {

    private Contacto() {
    }

    public static void whatsApp(Context context, String mensaje) {
        String url = "https://wa.me/" + context.getString(R.string.whatsapp_numero);
        if (mensaje != null && !mensaje.isEmpty()) {
            url += "?text=" + Uri.encode(mensaje);
        }
        abrir(context, Uri.parse(url));
    }

    public static void instagram(Context context) {
        abrir(context, Uri.parse("https://instagram.com/" + context.getString(R.string.instagram_usuario)));
    }

    public static void mapa(Context context, String direccion) {
        abrir(context, Uri.parse("geo:0,0?q=" + Uri.encode(direccion)));
    }

    private static void abrir(Context context, Uri uri) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_sin_app, Toast.LENGTH_SHORT).show();
        }
    }
}
