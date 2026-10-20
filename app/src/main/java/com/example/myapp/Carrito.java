package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrito de compras. La lógica es Java puro (testeable); la instancia compartida
 * de la app se guarda en SharedPreferences para sobrevivir al cierre de la app.
 */
public class Carrito {

    public static final long COSTO_ENVIO = 8000;

    private static final String PREFS = "carrito";
    private static final String CLAVE_ITEMS = "items";
    private static Carrito instancia;

    /** Línea del carrito: copia de los datos del producto más la cantidad. */
    public static class Item {
        public final String productoId;
        public final String nombre;
        public final String marca;
        public final String tamano;
        public final long precio;
        public final String imagenUrl;
        public final int stock;
        public int cantidad;

        public Item(String productoId, String nombre, String marca, String tamano,
                    long precio, String imagenUrl, int stock, int cantidad) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.marca = marca;
            this.tamano = tamano;
            this.precio = precio;
            this.imagenUrl = imagenUrl;
            this.stock = stock;
            this.cantidad = cantidad;
        }

        public long total() {
            return precio * cantidad;
        }
    }

    private final Map<String, Item> items = new LinkedHashMap<>();
    private SharedPreferences prefs;

    public static synchronized Carrito obtener(Context context) {
        if (instancia == null) {
            instancia = new Carrito();
            instancia.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            instancia.cargar();
        }
        return instancia;
    }

    /**
     * Agrega una unidad del producto.
     *
     * @return false si no hay stock suficiente.
     */
    public boolean agregar(Producto producto) {
        Item item = items.get(producto.getId());
        int cantidadActual = item == null ? 0 : item.cantidad;
        if (cantidadActual + 1 > producto.getStock()) {
            return false;
        }
        if (item == null) {
            items.put(producto.getId(), new Item(producto.getId(), producto.getNombre(), producto.getMarca(),
                    producto.getTamano(), producto.getPrecio(), producto.getImagenUrl(), producto.getStock(), 1));
        } else {
            item.cantidad++;
        }
        guardar();
        return true;
    }

    /**
     * Suma o resta unidades. Si la cantidad llega a 0 se elimina la línea.
     *
     * @return false si se intentó superar el stock disponible.
     */
    public boolean cambiarCantidad(String productoId, int delta) {
        Item item = items.get(productoId);
        if (item == null) {
            return true;
        }
        int nueva = item.cantidad + delta;
        if (delta > 0 && nueva > item.stock) {
            return false;
        }
        if (nueva <= 0) {
            items.remove(productoId);
        } else {
            item.cantidad = nueva;
        }
        guardar();
        return true;
    }

    /** Vuelve a poner una línea que se quitó (para "Deshacer"). */
    public void restaurar(Item item) {
        if (item.cantidad <= 0) {
            item.cantidad = 1;
        }
        items.put(item.productoId, item);
        guardar();
    }

    public void vaciar() {
        items.clear();
        guardar();
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public int cantidadTotal() {
        int total = 0;
        for (Item item : items.values()) {
            total += item.cantidad;
        }
        return total;
    }

    public long subtotal() {
        long total = 0;
        for (Item item : items.values()) {
            total += item.total();
        }
        return total;
    }

    public long envio() {
        return items.isEmpty() ? 0 : COSTO_ENVIO;
    }

    public long total() {
        return subtotal() + envio();
    }

    /** Texto del pedido que se envía por WhatsApp. */
    public String mensajePedido() {
        StringBuilder sb = new StringBuilder("Hola Melne, quiero hacer este pedido:\n\n");
        for (Item item : items.values()) {
            sb.append("• ").append(item.cantidad).append(" x ").append(item.nombre);
            String tamano = Formato.tamano(item.tamano);
            if (!tamano.isEmpty()) {
                sb.append(" (").append(tamano).append(")");
            }
            sb.append(" — ").append(Formato.precio(item.total())).append('\n');
        }
        sb.append("\nSubtotal: ").append(Formato.precio(subtotal()))
                .append("\nEnvío: ").append(Formato.precio(envio()))
                .append("\nTotal: ").append(Formato.precio(total()));
        return sb.toString();
    }

    private void guardar() {
        if (prefs == null) {
            return;
        }
        JSONArray array = new JSONArray();
        try {
            for (Item item : items.values()) {
                JSONObject o = new JSONObject();
                o.put("productoId", item.productoId);
                o.put("nombre", item.nombre);
                o.put("marca", item.marca);
                o.put("tamano", item.tamano);
                o.put("precio", item.precio);
                o.put("imagenUrl", item.imagenUrl);
                o.put("stock", item.stock);
                o.put("cantidad", item.cantidad);
                array.put(o);
            }
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(CLAVE_ITEMS, array.toString()).apply();
    }

    private void cargar() {
        String json = prefs.getString(CLAVE_ITEMS, null);
        if (json == null) {
            return;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Item item = new Item(o.getString("productoId"), o.optString("nombre"), o.optString("marca"),
                        o.optString("tamano"), o.optLong("precio"), o.optString("imagenUrl"),
                        o.optInt("stock"), o.optInt("cantidad", 1));
                items.put(item.productoId, item);
            }
        } catch (JSONException e) {
            items.clear();
        }
    }
}
