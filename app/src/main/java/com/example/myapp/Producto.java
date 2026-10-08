package com.example.myapp;

public class Producto {
    private String Nombre;
    private String Marca;
    private String Categoria;
    private String Precio;
    private String Stock;
    private String Tamaño;
    private String TipoReplica;

    public Producto() {
        // Constructor vacío requerido por Firebase Realtime Database
    }

    public Producto(String Nombre, String Marca, String Categoria, String Precio, String Stock, String Tamaño, String TipoReplica) {
        this.Nombre = Nombre;
        this.Marca = Marca;
        this.Categoria = Categoria;
        this.Precio = Precio;
        this.Stock = Stock;
        this.Tamaño = Tamaño;
        this.TipoReplica = TipoReplica;
    }

    //GET Y SET
    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getTipoReplica() {
        return TipoReplica;
    }

    public void setTipoReplica(String tipoReplica) {
        TipoReplica = tipoReplica;
    }

    public String getCategoria() {
        return Categoria;
    }

    public void setCategoria(String categoria) {
        Categoria = categoria;
    }

    public String getMarca() {
        return Marca;
    }

    public void setMarca(String marca) {
        Marca = marca;
    }

    public String getPrecio() {
        return Precio;
    }

    public void setPrecio(String precio) {
        Precio = precio;
    }

    public String getStock() {
        return Stock;
    }

    public void setStock(String stock) {
        Stock = stock;
    }

    public String getTamaño() {
        return Tamaño;
    }

    public void setTamaño(String tamaño) {
        Tamaño = tamaño;
    }
}
