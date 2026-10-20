package com.example.myapp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidades de formato para mostrar precios y tamaños. */
public final class Formato {

    private Formato() {
    }

    /** 160000 -> "$160.000 COP" */
    public static String precio(long valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor) + " COP";
    }

    /** Para cifras grandes: 17325000 -> "$17,3 M COP"; por debajo de un millón usa precio(). */
    public static String precioCorto(long valor) {
        if (valor < 1_000_000) {
            return precio(valor);
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setDecimalSeparator(',');
        return "$" + new DecimalFormat("0.#", simbolos).format(valor / 1_000_000.0) + " M COP";
    }

    /** "100" -> "100ml"; "100ml" o "3.4 oz" se dejan igual. */
    public static String tamano(String tamano) {
        if (tamano == null || tamano.trim().isEmpty()) {
            return "";
        }
        String t = tamano.trim();
        return t.matches("[0-9]+") ? t + "ml" : t;
    }
}
