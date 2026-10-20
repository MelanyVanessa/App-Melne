package com.example.myapp;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

/** Ayudas para formularios con TextInputLayout. */
public final class Campos {

    private Campos() {
    }

    /** Quita el mensaje de error de cada campo apenas el usuario vuelve a escribir. */
    public static void limpiarErrorAlEscribir(TextInputLayout... campos) {
        for (TextInputLayout til : campos) {
            if (til.getEditText() == null) {
                continue;
            }
            til.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    til.setError(null);
                }
            });
        }
    }

    public static String texto(TextInputLayout til) {
        return til.getEditText() == null ? "" : til.getEditText().getText().toString().trim();
    }
}
