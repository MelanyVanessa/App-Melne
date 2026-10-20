package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/** Panel principal del administrador con el resumen real del inventario. */
public class AdminDashboardActivity extends AdminActivity {

    private static final int STOCK_BAJO = 3;

    private BottomNavigationView nav;
    private TextView tvProductos, tvActivos, tvAgotados, tvInventario;
    private LinearLayout listaStockBajo;

    private final ValueEventListener escuchaProductos = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            mostrarResumen(Productos.desdeSnapshot(snapshot));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(AdminDashboardActivity.this,
                    getString(R.string.error_cargar_productos, error.getMessage()), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!verificarSesion()) {
            return;
        }
        configurarPantalla(R.layout.activity_admin_dashboard);

        tvProductos = estadistica(R.id.statProductos, R.string.stat_productos);
        tvActivos = estadistica(R.id.statActivos, R.string.stat_activos);
        tvAgotados = estadistica(R.id.statAgotados, R.string.stat_agotados);
        tvInventario = estadistica(R.id.statInventario, R.string.stat_inventario);
        listaStockBajo = findViewById(R.id.listaStockBajo);
        nav = findViewById(R.id.adminBottomNavigation);
        Navegacion.admin(this, nav, R.id.nav_home);

        findViewById(R.id.btnAgregarProducto).setOnClickListener(v ->
                startActivity(new Intent(this, ProductFormActivity.class)));
        findViewById(R.id.btnVerProductos).setOnClickListener(v ->
                startActivity(new Intent(this, AdminProductsActivity.class)));
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.confirmar_cerrar_sesion)
                .setPositiveButton(R.string.cerrar_sesion, (d, w) -> cerrarSesion())
                .setNegativeButton(R.string.cancelar, null)
                .show());

        if (CatalogoEjemplo.disponible(this)) {
            findViewById(R.id.cardDatosPrueba).setVisibility(View.VISIBLE);
            findViewById(R.id.btnCargarEjemplo).setOnClickListener(v -> confirmarCatalogoEjemplo());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nav != null) {
            Navegacion.marcar(nav, R.id.nav_home);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (tvProductos != null) {
            Productos.referencia().addValueEventListener(escuchaProductos);
        }
    }

    @Override
    protected void onStop() {
        Productos.referencia().removeEventListener(escuchaProductos);
        super.onStop();
    }

    private TextView estadistica(int idTarjeta, @StringRes int etiqueta) {
        View tarjeta = findViewById(idTarjeta);
        ((TextView) tarjeta.findViewById(R.id.tvEtiqueta)).setText(etiqueta);
        return tarjeta.findViewById(R.id.tvValor);
    }

    private void mostrarResumen(List<Producto> productos) {
        int agotados = Productos.porStock(productos, true).size();
        long inventario = 0;
        for (Producto p : productos) {
            inventario += p.getPrecio() * Math.max(p.getStock(), 0);
        }
        tvProductos.setText(String.valueOf(productos.size()));
        tvActivos.setText(String.valueOf(productos.size() - agotados));
        tvAgotados.setText(String.valueOf(agotados));
        tvInventario.setText(Formato.precioCorto(inventario));
        tvInventario.setContentDescription(Formato.precio(inventario));

        listaStockBajo.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Producto p : productos) {
            if (p.getStock() > 0 && p.getStock() <= STOCK_BAJO) {
                TextView fila = (TextView) inflater.inflate(R.layout.item_stock_bajo, listaStockBajo, false);
                fila.setText(getResources().getQuantityString(R.plurals.stock_bajo_fila, p.getStock(),
                        p.getNombre(), p.getStock()));
                listaStockBajo.addView(fila);
            }
        }
        if (listaStockBajo.getChildCount() == 0) {
            TextView vacio = (TextView) inflater.inflate(R.layout.item_stock_bajo, listaStockBajo, false);
            vacio.setText(R.string.stock_bajo_vacio);
            listaStockBajo.addView(vacio);
        }
    }

    private void confirmarCatalogoEjemplo() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.datos_prueba_titulo)
                .setMessage(R.string.datos_prueba_confirmar)
                .setPositiveButton(R.string.datos_prueba_reemplazar, (d, w) -> cargarCatalogoEjemplo())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void cargarCatalogoEjemplo() {
        MaterialButton boton = findViewById(R.id.btnCargarEjemplo);
        boton.setEnabled(false);
        boton.setText(R.string.guardando);
        try {
            CatalogoEjemplo.reemplazarCatalogo(this)
                    .addOnSuccessListener(v ->
                            Toast.makeText(this, R.string.datos_prueba_listo, Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, getString(R.string.error_guardar, e.getMessage()), Toast.LENGTH_LONG).show())
                    .addOnCompleteListener(t -> {
                        boton.setEnabled(true);
                        boton.setText(R.string.datos_prueba_boton);
                    });
        } catch (Exception e) {
            boton.setEnabled(true);
            boton.setText(R.string.datos_prueba_boton);
            Toast.makeText(this, getString(R.string.error_guardar, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
