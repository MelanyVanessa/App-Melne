package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/** "Mis Productos": lista, búsqueda, filtros, edición y eliminación de productos. */
public class AdminProductsActivity extends AdminActivity implements AdminProductoAdapter.Acciones {

    private final List<Producto> todos = new ArrayList<>();
    private AdminProductoAdapter adapter;
    private EditText etBuscar;
    private ChipGroup chipsEstado;
    private EstadoVacio estadoVacio;
    private View progreso;
    private BottomNavigationView nav;

    private final ValueEventListener escuchaProductos = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            todos.clear();
            todos.addAll(Productos.desdeSnapshot(snapshot));
            progreso.setVisibility(View.GONE);
            aplicarFiltros();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            progreso.setVisibility(View.GONE);
            estadoVacio.mostrar(R.drawable.ic_info, R.string.error_catalogo_titulo,
                    getString(R.string.error_cargar_productos, error.getMessage()), 0, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!verificarSesion()) {
            return;
        }
        configurarPantalla(R.layout.activity_admin_products);

        etBuscar = findViewById(R.id.etBuscar);
        chipsEstado = findViewById(R.id.chipsEstado);
        estadoVacio = new EstadoVacio(findViewById(R.id.estadoVacio));
        progreso = findViewById(R.id.progreso);
        nav = findViewById(R.id.adminBottomNavigation);

        adapter = new AdminProductoAdapter(this);
        RecyclerView rv = findViewById(R.id.rvProductos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        findViewById(R.id.btnAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, ProductFormActivity.class)));
        chipsEstado.setOnCheckedStateChangeListener((group, ids) -> aplicarFiltros());
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                aplicarFiltros();
            }
        });

        Navegacion.admin(this, nav, R.id.nav_products);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nav != null) {
            Navegacion.marcar(nav, R.id.nav_products);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            Productos.referencia().addValueEventListener(escuchaProductos);
        }
    }

    @Override
    protected void onStop() {
        Productos.referencia().removeEventListener(escuchaProductos);
        super.onStop();
    }

    @Override
    public void onEditar(Producto producto) {
        startActivity(new Intent(this, ProductFormActivity.class)
                .putExtra(ProductFormActivity.EXTRA_PRODUCTO_ID, producto.getId()));
    }

    @Override
    public void onEliminar(Producto producto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.eliminar_producto)
                .setMessage(getString(R.string.confirmar_eliminar, producto.getNombre()))
                .setPositiveButton(R.string.eliminar, (d, w) -> eliminar(producto))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void eliminar(Producto producto) {
        Productos.referencia().child(producto.getId()).removeValue()
                .addOnSuccessListener(v -> Snackbar.make(findViewById(R.id.main), R.string.producto_eliminado,
                                Snackbar.LENGTH_LONG)
                        .setAnchorView(nav)
                        // Deshacer: se vuelve a guardar el mismo producto con su misma clave.
                        .setAction(R.string.deshacer, a -> Productos.referencia().child(producto.getId())
                                .setValue(producto))
                        .show())
                .addOnFailureListener(e -> Snackbar.make(findViewById(R.id.main),
                        getString(R.string.error_eliminar, e.getMessage()), Snackbar.LENGTH_LONG)
                        .setAnchorView(nav).show());
    }

    private void aplicarFiltros() {
        List<Producto> lista;
        int chip = chipsEstado.getCheckedChipId();
        if (chip == R.id.chipActivos) {
            lista = Productos.porStock(todos, false);
        } else if (chip == R.id.chipAgotados) {
            lista = Productos.porStock(todos, true);
        } else {
            lista = new ArrayList<>(todos);
        }
        lista = Productos.buscar(lista, etBuscar.getText().toString());
        adapter.mostrar(lista);
        if (!lista.isEmpty()) {
            estadoVacio.ocultar();
        } else if (todos.isEmpty()) {
            estadoVacio.mostrar(R.drawable.ic_catalog, R.string.admin_sin_productos_titulo,
                    getString(R.string.admin_sin_productos), R.string.agregar_producto,
                    v -> startActivity(new Intent(this, ProductFormActivity.class)));
        } else {
            estadoVacio.mostrar(R.drawable.ic_search, R.string.sin_resultados,
                    getString(R.string.sin_resultados_mensaje), R.string.limpiar_filtros, v -> {
                        chipsEstado.check(R.id.chipTodos);
                        etBuscar.setText("");
                    });
        }
    }
}
