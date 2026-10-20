package com.example.myapp;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catálogo de ejemplo con 10 productos y fotos de Pexels. El archivo solo existe en
 * app/src/debug/assets, así que esta función no está disponible en la versión publicada.
 */
public final class CatalogoEjemplo {

    private static final String ARCHIVO = "catalogo_ejemplo.json";

    private CatalogoEjemplo() {
    }

    public static boolean disponible(Context context) {
        try {
            String[] archivos = context.getAssets().list("");
            return archivos != null && Arrays.asList(archivos).contains(ARCHIVO);
        } catch (IOException e) {
            return false;
        }
    }

    /** Reemplaza TODOS los productos de /producto por los del catálogo de ejemplo, en una sola escritura. */
    public static Task<Void> reemplazarCatalogo(Context context) throws IOException, JSONException {
        JSONArray lista = new JSONArray(leer(context));
        DatabaseReference ref = Productos.referencia();
        Map<String, Object> nuevos = new LinkedHashMap<>();
        for (int i = 0; i < lista.length(); i++) {
            JSONObject o = lista.getJSONObject(i);
            Producto p = new Producto();
            p.setNombre(o.getString("nombre"));
            p.setMarca(o.optString("marca"));
            p.setCategoria(o.optString("categoria"));
            p.setGenero(o.optString("genero"));
            p.setPrecio(o.getLong("precio"));
            p.setStock(o.optInt("stock"));
            p.setTamano(o.optString("tamano"));
            p.setTipoReplica(o.optString("tipoReplica"));
            p.setImagenUrl(o.optString("imagenUrl"));
            nuevos.put(ref.push().getKey(), p);
        }
        return ref.setValue(nuevos);
    }

    private static String leer(Context context) throws IOException {
        try (InputStream in = context.getAssets().open(ARCHIVO)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
