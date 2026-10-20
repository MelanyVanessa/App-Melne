package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Inicio de sesión del administrador con Firebase Auth (correo y contraseña).
 * Además de autenticarse, la cuenta debe figurar en /admins/{uid} = true.
 */
public class LoginActivity extends BaseActivity {

    /** Nombre de la clase de la pantalla a abrir tras iniciar sesión. */
    public static final String EXTRA_DESTINO = "destino";

    private TextInputLayout tilCorreo, tilClave;
    private EditText etCorreo, etClave;
    private Button btnIngresar;
    private View progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_login);

        tilCorreo = findViewById(R.id.tilCorreo);
        tilClave = findViewById(R.id.tilClave);
        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        progreso = findViewById(R.id.progreso);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnIngresar.setOnClickListener(v -> ingresar());
        etClave.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ingresar();
                return true;
            }
            return false;
        });
        // Al corregir, el error desaparece.
        Campos.limpiarErrorAlEscribir(tilCorreo, tilClave);
    }

    private void ingresar() {
        String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString();
        tilCorreo.setError(null);
        tilClave.setError(null);
        if (TextUtils.isEmpty(correo)) {
            tilCorreo.setError(getString(R.string.error_correo_requerido));
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.error_correo_invalido));
            etCorreo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(clave)) {
            tilClave.setError(getString(R.string.error_clave_requerida));
            etClave.requestFocus();
            return;
        }

        bloquear(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, clave)
                .addOnSuccessListener(resultado -> verificarAdmin(resultado.getUser()))
                .addOnFailureListener(e -> {
                    bloquear(false);
                    if (e instanceof FirebaseNetworkException) {
                        tilClave.setError(getString(R.string.error_sin_conexion_login));
                    } else {
                        // La contraseña se borra; el correo se conserva para no volver a escribirlo.
                        etClave.setText("");
                        tilClave.setError(getString(R.string.error_login));
                        etClave.requestFocus();
                    }
                });
    }

    private void verificarAdmin(FirebaseUser usuario) {
        if (usuario == null) {
            bloquear(false);
            return;
        }
        FirebaseDatabase.getInstance().getReference("admins").child(usuario.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    if (Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                        irADestino();
                    } else {
                        rechazar();
                    }
                })
                .addOnFailureListener(e -> rechazar());
    }

    private void rechazar() {
        FirebaseAuth.getInstance().signOut();
        bloquear(false);
        tilCorreo.setError(getString(R.string.error_no_admin));
    }

    private void irADestino() {
        String destino = getIntent().getStringExtra(EXTRA_DESTINO);
        Intent intent = new Intent();
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.removeExtra(EXTRA_DESTINO);
        intent.setClassName(this, destino != null ? destino : AdminDashboardActivity.class.getName());
        startActivity(intent);
        finish();
    }

    private void bloquear(boolean ocupado) {
        btnIngresar.setEnabled(!ocupado);
        btnIngresar.setText(ocupado ? R.string.ingresando : R.string.ingresar);
        progreso.setVisibility(ocupado ? View.VISIBLE : View.INVISIBLE);
    }
}
