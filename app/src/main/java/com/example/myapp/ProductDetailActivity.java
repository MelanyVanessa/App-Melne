package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/** Detalle de un producto: foto grande, ficha técnica, disponibilidad y acciones. */
public class ProductDetailActivity extends BaseActivity {

    public static final String EXTRA_PRODUCTO_ID = "producto_id";

    private DatabaseReference ref;
    private Producto producto;
    private ImageView imagen;
    private TextView tvMarca, tvNombre, tvPrecio, tvDisponibilidad;
    private LinearLayout listaDetalles;
    private MaterialButton btnAgregar;

    private final ValueEventListener escucha = new ValueEventListener() {
        @Override
        @SuppressWarnings("unchecked")
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Object datos = snapshot.getValue();
            if (!(datos instanceof Map)) {
                Toast.makeText(ProductDetailActivity.this, R.string.producto_no_existe, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            mostrar(Producto.desdeMapa(snapshot.getKey(), (Map<String, Object>) datos));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(ProductDetailActivity.this,
                    getString(R.string.error_cargar_productos, error.getMessage()), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_product_detail);

        String id = getIntent().getStringExtra(EXTRA_PRODUCTO_ID);
        if (id == null) {
            finish();
            return;
        }
        ref = Productos.referencia().child(id);
        imagen = findViewById(R.id.imgProducto);
        tvMarca = findViewById(R.id.tvMarca);
        tvNombre = findViewById(R.id.tvNombre);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvDisponibilidad = findViewById(R.id.tvDisponibilidad);
        listaDetalles = findViewById(R.id.listaDetalles);
        btnAgregar = findViewById(R.id.btnAgregar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAgregar.setOnClickListener(v -> agregar());
        findViewById(R.id.btnPreguntar).setOnClickListener(v -> {
            if (producto != null) {
                Contacto.whatsApp(this, getString(R.string.whatsapp_consulta_producto,
                        producto.getNombre(), Formato.precio(producto.getPrecio())));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ref != null) {
            ref.addValueEventListener(escucha);
        }
    }

    @Override
    protected void onStop() {
        if (ref != null) {
            ref.removeEventListener(escucha);
        }
        super.onStop();
    }

    private void mostrar(Producto p) {
        producto = p;
        tvMarca.setText(p.getMarca());
        tvNombre.setText(p.getNombre());
        tvPrecio.setText(Formato.precio(p.getPrecio()));
        imagen.setContentDescription(getString(R.string.foto_de, p.getNombre()));
        Glide.with(this).load(p.getImagenUrl())
                .placeholder(R.drawable.placeholder_producto)
                .error(R.drawable.placeholder_producto)
                .centerCrop()
                .into(imagen);

        boolean agotado = p.isAgotado();
        int color = ContextCompat.getColor(this, agotado ? R.color.status_pending_text : R.color.status_delivered_text);
        tvDisponibilidad.setText(agotado ? getString(R.string.no_disponible)
                : getResources().getQuantityString(R.plurals.disponible_unidades, p.getStock(), p.getStock()));
        tvDisponibilidad.setTextColor(color);
        tvDisponibilidad.setCompoundDrawablesRelativeWithIntrinsicBounds(
                agotado ? R.drawable.ic_cancel : R.drawable.ic_check_circle, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(tvDisponibilidad, android.content.res.ColorStateList.valueOf(color));

        btnAgregar.setEnabled(!agotado);
        btnAgregar.setText(agotado ? R.string.agotado : R.string.anadir_al_carrito);

        listaDetalles.removeAllViews();
        agregarDetalle(R.string.tamano, Formato.tamano(p.getTamano()));
        agregarDetalle(R.string.genero, p.getGenero());
        agregarDetalle(R.string.categoria, p.getCategoria());
        agregarDetalle(R.string.tipo_replica, p.getTipoReplica());
    }

    private void agregarDetalle(@StringRes int etiqueta, String valor) {
        if (valor == null || valor.isEmpty()) {
            return;
        }
        View fila = LayoutInflater.from(this).inflate(R.layout.item_detalle, listaDetalles, false);
        ((TextView) fila.findViewById(R.id.tvEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.tvValor)).setText(valor);
        listaDetalles.addView(fila);
    }

    private void agregar() {
        if (producto == null) {
            return;
        }
        if (Carrito.obtener(this).agregar(producto)) {
            Snackbar.make(findViewById(R.id.main), getString(R.string.agregado_al_carrito, producto.getNombre()),
                            Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.barraAcciones)
                    .setAction(R.string.ver_carrito, v -> startActivity(
                            new android.content.Intent(this, CartActivity.class)))
                    .show();
        } else {
            Snackbar.make(findViewById(R.id.main), R.string.sin_stock_suficiente, Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.barraAcciones).show();
        }
    }
}
