package com.example.myapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CarritoTest {

    private static Producto producto(String id, String nombre, long precio, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setTamano("100");
        return p;
    }

    @Test
    public void agregar_sumaCantidadYCalculaTotales() {
        Carrito carrito = new Carrito();
        Producto sauvage = producto("1", "Sauvage", 160000, 5);

        assertTrue(carrito.agregar(sauvage));
        assertTrue(carrito.agregar(sauvage));
        assertTrue(carrito.agregar(producto("2", "Good Girl", 175000, 3)));

        assertEquals(2, carrito.getItems().size());
        assertEquals(3, carrito.cantidadTotal());
        assertEquals(495000, carrito.subtotal());
        assertEquals(Carrito.COSTO_ENVIO, carrito.envio());
        assertEquals(495000 + Carrito.COSTO_ENVIO, carrito.total());
    }

    @Test
    public void agregar_respetaElStock() {
        Carrito carrito = new Carrito();
        Producto p = producto("1", "Libre", 150000, 1);

        assertTrue(carrito.agregar(p));
        assertFalse(carrito.agregar(p));
        assertFalse(carrito.agregar(producto("2", "Agotado", 100000, 0)));
        assertEquals(1, carrito.cantidadTotal());
    }

    @Test
    public void cambiarCantidad_aCeroEliminaLaLinea() {
        Carrito carrito = new Carrito();
        carrito.agregar(producto("1", "Sauvage", 160000, 5));

        assertFalse(carrito.cambiarCantidad("1", 5));
        carrito.cambiarCantidad("1", -1);

        assertTrue(carrito.estaVacio());
        assertEquals(0, carrito.total());
    }

    @Test
    public void mensajePedido_incluyeProductosYTotal() {
        Carrito carrito = new Carrito();
        carrito.agregar(producto("1", "Sauvage", 160000, 5));

        String mensaje = carrito.mensajePedido();

        assertTrue(mensaje.contains("1 x Sauvage (100ml)"));
        assertTrue(mensaje.contains("Total: $168.000 COP"));
    }
}
