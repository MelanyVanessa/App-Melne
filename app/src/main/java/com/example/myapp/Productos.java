package com.example.myapp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Acceso a /producto en Firebase y filtros sobre la lista de productos. */
public final class Productos {

    private Productos() {
    }

    public static DatabaseReference referencia() {
        return FirebaseDatabase.getInstance().getReference("producto");
    }

    @SuppressWarnings("unchecked")
    public static List<Producto> desdeSnapshot(DataSnapshot snapshot) {
        List<Producto> lista = new ArrayList<>();
        for (DataSnapshot hijo : snapshot.getChildren()) {
            Object valor = hijo.getValue();
            if (valor instanceof Map) {
                lista.add(Producto.desdeMapa(hijo.getKey(), (Map<String, Object>) valor));
            }
        }
        return lista;
    }

    /** Filtra por nombre o marca, sin distinguir mayúsculas ni tildes. */
    public static List<Producto> buscar(List<Producto> productos, String texto) {
        String consulta = normalizar(texto);
        if (consulta.isEmpty()) {
            return new ArrayList<>(productos);
        }
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (normalizar(p.getNombre()).contains(consulta) || normalizar(p.getMarca()).contains(consulta)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public static List<Producto> porGenero(List<Producto> productos, String genero) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (normalizar(genero).equals(normalizar(p.getGenero()))) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public static List<Producto> porStock(List<Producto> productos, boolean agotados) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.isAgotado() == agotados) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /** Los últimos productos agregados primero (las claves push() de Firebase son cronológicas). */
    public static List<Producto> nuevos(List<Producto> productos, int maximo) {
        List<Producto> resultado = new ArrayList<>(productos);
        Collections.reverse(resultado);
        return resultado.size() > maximo ? new ArrayList<>(resultado.subList(0, maximo)) : resultado;
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
