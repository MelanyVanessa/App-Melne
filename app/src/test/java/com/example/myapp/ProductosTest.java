package com.example.myapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ProductosTest {

    private static Producto producto(String nombre, String marca, String genero, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setMarca(marca);
        p.setGenero(genero);
        p.setStock(stock);
        return p;
    }

    private final List<Producto> lista = Arrays.asList(
            producto("Sauvage Elixir", "Dior", "Hombre", 3),
            producto("Good Girl", "Carolina Herrera", "Mujer", 0),
            producto("Libre Intense", "YSL", "Mujer", 8));

    @Test
    public void buscar_ignoraMayusculasYTildes() {
        assertEquals(1, Productos.buscar(lista, "DIÓR").size());
        assertEquals(1, Productos.buscar(lista, "girl").size());
        assertEquals(3, Productos.buscar(lista, "  ").size());
    }

    @Test
    public void filtros_porGeneroYStock() {
        assertEquals(2, Productos.porGenero(lista, "mujer").size());
        assertEquals(1, Productos.porStock(lista, true).size());
        assertEquals(2, Productos.porStock(lista, false).size());
    }

    @Test
    public void nuevos_devuelveLosUltimosPrimero() {
        List<Producto> nuevos = Productos.nuevos(lista, 2);
        assertEquals(2, nuevos.size());
        assertEquals("Libre Intense", nuevos.get(0).getNombre());
    }

    @Test
    public void formato_precioYTamano() {
        assertEquals("$1.250.000 COP", Formato.precio(1250000));
        assertEquals("$0 COP", Formato.precio(0));
        assertEquals("$17,3 M COP", Formato.precioCorto(17325000));
        assertEquals("$950.000 COP", Formato.precioCorto(950000));
        assertEquals("100ml", Formato.tamano("100"));
        assertEquals("3.4 oz", Formato.tamano("3.4 oz"));
        assertEquals("", Formato.tamano(null));
    }
}
