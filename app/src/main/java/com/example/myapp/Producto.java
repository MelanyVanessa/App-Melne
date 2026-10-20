package com.example.myapp;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Producto {

    private String id;
    private String nombre;
    private String marca;
    private String categoria;
    private String genero;
    private long precio;
    private int stock;
    private String tamano;
    private String tipoReplica;
    private String imagenUrl;

    public Producto() {

    }


    public static Producto desdeMapa(String id, Map<String, Object> datos) {
        Producto p = new Producto();
        p.id = id;
        if (datos == null) {
            return p;
        }
        p.nombre = texto(datos.get("nombre"));
        p.marca = texto(datos.get("marca"));
        p.categoria = texto(datos.get("categoria"));
        p.genero = texto(datos.get("genero"));
        p.precio = numero(datos.get("precio"));
        p.stock = (int) numero(datos.get("stock"));
        String tamano = texto(datos.get("tamano"));
        p.tamano = !tamano.isEmpty() ? tamano : texto(datos.get("tamaño"));
        p.tipoReplica = texto(datos.get("tipoReplica"));
        p.imagenUrl = texto(datos.get("imagenUrl"));
        return p;
    }

    private static String texto(Object valor) {
        return valor == null ? "" : valor.toString().trim();
    }

    static long numero(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).longValue();
        }
        String digitos = texto(valor).replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(digitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean isAgotado() {
        return stock <= 0;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public long getPrecio() {
        return precio;
    }

    public void setPrecio(long precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public String getTipoReplica() {
        return tipoReplica;
    }

    public void setTipoReplica(String tipoReplica) {
        this.tipoReplica = tipoReplica;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
