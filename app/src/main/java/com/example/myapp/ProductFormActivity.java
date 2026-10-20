package com.example.myapp;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Formulario para crear un producto nuevo o editar uno existente (EXTRA_PRODUCTO_ID). */
public class ProductFormActivity extends AdminActivity {

    public static final String EXTRA_PRODUCTO_ID = "producto_id";

    private final ExecutorService segundoPlano = Executors.newSingleThreadExecutor();
    private final DatabaseReference estadoConexion = FirebaseDatabase.getInstance().getReference(".info/connected");

    private TextInputLayout tilNombre, tilMarca, tilPrecio, tilStock, tilTamano;
    private AutoCompleteTextView spCategoria, spGenero, spTipoReplica;
    private Button btnGuardar;
    private View progreso;
    private ImageView imgPreview;
    private View layoutSubirImagen;

    private String productoId;
    private String imagenUrlActual;
    private Uri imagenNueva;
    private String estadoInicial = "";
    private boolean conectado = true;

    private final ValueEventListener escuchaConexion = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            conectado = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    private final ActivityResultLauncher<PickVisualMediaRequest> selectorImagen =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onImagenElegida);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!verificarSesion()) {
            return;
        }
        configurarPantalla(R.layout.activity_product_form);

        tilNombre = findViewById(R.id.tilNombre);
        tilMarca = findViewById(R.id.tilMarca);
        tilPrecio = findViewById(R.id.tilPrecio);
        tilStock = findViewById(R.id.tilStock);
        tilTamano = findViewById(R.id.tilTamano);
        spCategoria = findViewById(R.id.spCategoria);
        spGenero = findViewById(R.id.spGenero);
        spTipoReplica = findViewById(R.id.spTipoReplica);
        btnGuardar = findViewById(R.id.btnGuardar);
        progreso = findViewById(R.id.progreso);
        imgPreview = findViewById(R.id.imgPreview);
        layoutSubirImagen = findViewById(R.id.layoutSubirImagen);

        valoresPorDefecto();
        Campos.limpiarErrorAlEscribir(tilNombre, tilPrecio, tilStock);

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.areaImagen).setOnClickListener(v -> selectorImagen.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        btnGuardar.setOnClickListener(v -> guardar());

        // Si hay cambios sin guardar, se pide confirmación antes de salir.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hayCambios()) {
                    new MaterialAlertDialogBuilder(ProductFormActivity.this)
                            .setTitle(R.string.descartar_cambios_titulo)
                            .setMessage(R.string.descartar_cambios_mensaje)
                            .setPositiveButton(R.string.descartar, (d, w) -> finish())
                            .setNegativeButton(R.string.seguir_editando, null)
                            .show();
                } else {
                    finish();
                }
            }
        });

        productoId = getIntent().getStringExtra(EXTRA_PRODUCTO_ID);
        if (productoId != null) {
            ((TextView) findViewById(R.id.tvTitulo)).setText(R.string.editar_producto);
            cargarProducto();
        } else {
            estadoInicial = estadoActual();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        estadoConexion.addValueEventListener(escuchaConexion);
    }

    @Override
    protected void onStop() {
        estadoConexion.removeEventListener(escuchaConexion);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        segundoPlano.shutdown();
        super.onDestroy();
    }

    private void valoresPorDefecto() {
        spGenero.setText(getString(R.string.genero_unisex), false);
        spCategoria.setText(getResources().getStringArray(R.array.categorias)[0], false);
        spTipoReplica.setText(getResources().getStringArray(R.array.tipos_replica)[0], false);
    }

    private void poner(TextInputLayout til, String valor) {
        if (til.getEditText() != null) {
            til.getEditText().setText(valor);
        }
    }

    private String estadoActual() {
        return Campos.texto(tilNombre) + "|" + Campos.texto(tilMarca) + "|" + Campos.texto(tilPrecio) + "|"
                + Campos.texto(tilStock) + "|" + Campos.texto(tilTamano) + "|" + spGenero.getText() + "|"
                + spCategoria.getText() + "|" + spTipoReplica.getText() + "|" + imagenNueva;
    }

    private boolean hayCambios() {
        return !estadoActual().equals(estadoInicial);
    }

    @SuppressWarnings("unchecked")
    private void cargarProducto() {
        bloquear(true);
        Productos.referencia().child(productoId).get()
                .addOnSuccessListener(snapshot -> {
                    bloquear(false);
                    Object datos = snapshot.getValue();
                    if (!(datos instanceof Map)) {
                        aviso(R.string.producto_no_existe);
                        finish();
                        return;
                    }
                    Producto p = Producto.desdeMapa(productoId, (Map<String, Object>) datos);
                    poner(tilNombre, p.getNombre());
                    poner(tilMarca, p.getMarca());
                    poner(tilPrecio, p.getPrecio() > 0 ? String.valueOf(p.getPrecio()) : "");
                    poner(tilStock, String.valueOf(p.getStock()));
                    poner(tilTamano, p.getTamano() == null ? "" : p.getTamano().replaceAll("[^0-9]", ""));
                    if (!TextUtils.isEmpty(p.getCategoria())) {
                        spCategoria.setText(p.getCategoria(), false);
                    }
                    if (!TextUtils.isEmpty(p.getGenero())) {
                        spGenero.setText(p.getGenero(), false);
                    }
                    if (!TextUtils.isEmpty(p.getTipoReplica())) {
                        spTipoReplica.setText(p.getTipoReplica(), false);
                    }
                    imagenUrlActual = p.getImagenUrl();
                    if (!TextUtils.isEmpty(imagenUrlActual)) {
                        mostrarPreview();
                        Glide.with(this).load(imagenUrlActual).centerCrop().into(imgPreview);
                    }
                    estadoInicial = estadoActual();
                })
                .addOnFailureListener(e -> {
                    bloquear(false);
                    aviso(getString(R.string.error_cargar_productos, e.getMessage()));
                });
    }

    private void onImagenElegida(Uri uri) {
        if (uri == null) {
            return;
        }
        imagenNueva = uri;
        mostrarPreview();
        Glide.with(this).load(uri).centerCrop().into(imgPreview);
    }

    private void mostrarPreview() {
        imgPreview.setVisibility(View.VISIBLE);
        layoutSubirImagen.setVisibility(View.GONE);
    }

    private void guardar() {
        String nombre = Campos.texto(tilNombre);
        long precio = Producto.numero(Campos.texto(tilPrecio));

        // Errores junto a cada campo; los demás datos se conservan.
        boolean valido = true;
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError(getString(R.string.error_nombre_requerido));
            valido = false;
        }
        if (precio <= 0) {
            tilPrecio.setError(getString(R.string.error_precio_requerido));
            valido = false;
        }
        if (!valido) {
            (TextUtils.isEmpty(nombre) ? tilNombre : tilPrecio).requestFocus();
            return;
        }
        if (!conectado) {
            aviso(R.string.error_guardar_sin_conexion);
            return;
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setMarca(Campos.texto(tilMarca));
        producto.setCategoria(spCategoria.getText().toString());
        producto.setGenero(spGenero.getText().toString());
        producto.setPrecio(precio);
        producto.setStock((int) Producto.numero(Campos.texto(tilStock)));
        producto.setTamano(Campos.texto(tilTamano));
        producto.setTipoReplica(spTipoReplica.getText().toString());

        DatabaseReference ref = productoId != null
                ? Productos.referencia().child(productoId)
                : Productos.referencia().push();

        bloquear(true);
        if (imagenNueva == null) {
            producto.setImagenUrl(imagenUrlActual);
            escribir(ref, producto);
            return;
        }

        // La compresión puede tardar un poco: se hace fuera del hilo principal.
        Uri imagen = imagenNueva;
        segundoPlano.execute(() -> {
            try {
                String dataUri = Imagenes.comprimirComoDataUri(getContentResolver(), imagen);
                runOnUiThread(() -> {
                    producto.setImagenUrl(dataUri);
                    escribir(ref, producto);
                });
            } catch (IOException | RuntimeException e) {
                runOnUiThread(() -> {
                    bloquear(false);
                    aviso(getString(R.string.error_procesar_imagen, e.getMessage()));
                });
            }
        });
    }

    private void escribir(DatabaseReference ref, Producto producto) {
        ref.setValue(producto)
                .addOnSuccessListener(v -> {
                    if (productoId != null) {
                        estadoInicial = estadoActual();
                        finish();
                    } else {
                        bloquear(false);
                        aviso(R.string.producto_guardado);
                        limpiarCampos();
                    }
                })
                .addOnFailureListener(e -> {
                    bloquear(false);
                    aviso(getString(R.string.error_guardar, e.getMessage()));
                });
    }

    private void aviso(@StringRes int texto) {
        aviso(getString(texto));
    }

    private void aviso(String texto) {
        Snackbar.make(findViewById(R.id.main), texto, Snackbar.LENGTH_LONG).show();
    }

    private void bloquear(boolean ocupado) {
        btnGuardar.setEnabled(!ocupado);
        btnGuardar.setText(ocupado ? R.string.guardando : R.string.guardar);
        progreso.setVisibility(ocupado ? View.VISIBLE : View.INVISIBLE);
    }

    private void limpiarCampos() {
        for (TextInputLayout til : new TextInputLayout[]{tilNombre, tilMarca, tilPrecio, tilStock, tilTamano}) {
            poner(til, "");
            til.setError(null);
        }
        valoresPorDefecto();
        imagenNueva = null;
        imagenUrlActual = null;
        imgPreview.setImageDrawable(null);
        imgPreview.setVisibility(View.GONE);
        layoutSubirImagen.setVisibility(View.VISIBLE);
        tilNombre.requestFocus();
        estadoInicial = estadoActual();
    }
}
