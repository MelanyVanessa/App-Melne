package com.example.myapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ProductoTest {

    @Test
    public void desdeMapa_leeFormatoAntiguoConTextos() {
        // Así quedaban guardados los productos antes del cambio de modelo.
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", "Sauvage Elixir");
        datos.put("marca", "Dior");
        datos.put("precio", "$160.000");
        datos.put("stock", "12");
        datos.put("tamaño", "100ml");

        Producto p = Producto.desdeMapa("abc", datos);

        assertEquals("abc", p.getId());
        assertEquals("Sauvage Elixir", p.getNombre());
        assertEquals(160000, p.getPrecio());
        assertEquals(12, p.getStock());
        assertEquals("100ml", p.getTamano());
        assertFalse(p.isAgotado());
    }

    @Test
    public void desdeMapa_leeFormatoNuevoConNumeros() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", "Good Girl");
        datos.put("precio", 175000L);
        datos.put("stock", 0L);
        datos.put("tamano", "80");
        datos.put("genero", "Mujer");

        Producto p = Producto.desdeMapa("x", datos);

        assertEquals(175000, p.getPrecio());
        assertEquals("80", p.getTamano());
        assertEquals("Mujer", p.getGenero());
        assertTrue(p.isAgotado());
    }

    @Test
    public void desdeMapa_camposFaltantesQuedanVacios() {
        Producto p = Producto.desdeMapa("x", new HashMap<>());
        assertEquals("", p.getNombre());
        assertEquals(0, p.getPrecio());
        assertTrue(p.isAgotado());
    }

    @Test
    public void numero_ignoraTextoSinDigitos() {
        assertEquals(0, Producto.numero("gratis"));
        assertEquals(0, Producto.numero(null));
        assertEquals(1500, Producto.numero(1500.9));
    }
}
