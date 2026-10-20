package com.example.myapp;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Comprime una foto elegida por el usuario y la convierte en un data URI
 * ("data:image/jpeg;base64,...") que se guarda directamente en Realtime Database.
 * Glide sabe cargar este formato, así que se muestra igual que una URL normal.
 */
public final class Imagenes {

    private static final int LADO_MAXIMO = 800;
    private static final int BYTES_OBJETIVO = 150 * 1024;

    private Imagenes() {
    }

    /** Debe llamarse fuera del hilo principal. */
    public static String comprimirComoDataUri(ContentResolver resolver, Uri uri) throws IOException {
        // 1. Leer solo las dimensiones para decodificar a un tamaño cercano al final.
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        try (InputStream in = abrir(resolver, uri)) {
            BitmapFactory.decodeStream(in, null, limites);
        }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            throw new IOException("El archivo no es una imagen válida");
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = 1;
        while (Math.max(limites.outWidth, limites.outHeight) / (opciones.inSampleSize * 2) >= LADO_MAXIMO) {
            opciones.inSampleSize *= 2;
        }

        Bitmap bitmap;
        try (InputStream in = abrir(resolver, uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opciones);
        }
        if (bitmap == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        // 2. Escalar al tamaño final y corregir la rotación de las fotos de cámara.
        Matrix matrix = new Matrix();
        float escala = Math.min(1f, (float) LADO_MAXIMO / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        matrix.postScale(escala, escala);
        matrix.postRotate(rotacion(resolver, uri));
        Bitmap escalado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // 3. Comprimir a JPEG bajando la calidad hasta un tamaño razonable.
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int calidad = 80;
        do {
            salida.reset();
            escalado.compress(Bitmap.CompressFormat.JPEG, calidad, salida);
            calidad -= 10;
        } while (salida.size() > BYTES_OBJETIVO && calidad >= 40);

        if (escalado != bitmap) {
            escalado.recycle();
        }
        bitmap.recycle();
        return "data:image/jpeg;base64," + Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP);
    }

    private static InputStream abrir(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) {
            throw new IOException("No se pudo abrir la imagen");
        }
        return in;
    }

    private static int rotacion(ContentResolver resolver, Uri uri) {
        try (InputStream in = abrir(resolver, uri)) {
            int orientacion = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
