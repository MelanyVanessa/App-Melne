package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/** Inicio del cliente: catálogo de productos leído desde Firebase. */
public class HomeActivity extends BaseActivity {

    private static final int MAX_NUEVOS = 10;

    private final List<Producto> todos = new ArrayList<>();
    private ProductoAdapter adapter;
    private TextInputLayout tilBuscar;
    private EditText etBuscar;
    private ChipGroup chipsGenero;
    private TextView tvConteo;
    private CircularProgressIndicator progreso;
    private EstadoVacio estadoVacio;
    private NestedScrollView scroll;
    private BottomNavigationView nav;
    private Conexion conexion;
    private boolean datosCargados;

    private final ValueEventListener escuchaProductos = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            datosCargados = true;
            todos.clear();
            todos.addAll(Productos.desdeSnapshot(snapshot));
            progreso.setVisibility(View.GONE);
            aplicarFiltros();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            progreso.setVisibility(View.GONE);
            adapter.mostrar(new ArrayList<>());
            estadoVacio.mostrar(R.drawable.ic_info, R.string.error_catalogo_titulo,
                    getString(R.string.error_cargar_productos, error.getMessage()),
                    R.string.reintentar, v -> reiniciarEscucha());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_home);

        tilBuscar = findViewById(R.id.tilBuscar);
        etBuscar = findViewById(R.id.etBuscar);
        chipsGenero = findViewById(R.id.chipsGenero);
        tvConteo = findViewById(R.id.tvConteo);
        progreso = findViewById(R.id.progreso);
        estadoVacio = new EstadoVacio(findViewById(R.id.estadoVacio));
        scroll = findViewById(R.id.scroll);
        nav = findViewById(R.id.bottomNavigation);

        adapter = new ProductoAdapter(new ProductoAdapter.Acciones() {
            @Override
            public void onAgregar(Producto producto) {
                agregarAlCarrito(producto);
            }

            @Override
            public void onAbrir(Producto producto) {
                startActivity(new Intent(HomeActivity.this, ProductDetailActivity.class)
                        .putExtra(ProductDetailActivity.EXTRA_PRODUCTO_ID, producto.getId()));
            }
        });
        RecyclerView rv = findViewById(R.id.rvProductos);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBag).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.btnSearch).setOnClickListener(v -> alternarBusqueda());
        findViewById(R.id.tvVerTodos).setOnClickListener(v -> mostrarTodo());

        chipsGenero.setOnCheckedStateChangeListener((group, ids) -> aplicarFiltros());
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

        conexion = new Conexion(findViewById(R.id.main), nav, () -> {
            // Sin conexión y sin catálogo guardado: explicamos qué pasa en vez de girar sin fin.
            if (!datosCargados) {
                progreso.setVisibility(View.GONE);
                estadoVacio.mostrar(R.drawable.ic_info, R.string.sin_conexion_titulo,
                        getString(R.string.sin_conexion_mensaje), R.string.reintentar, v -> reiniciarEscucha());
            }
        });

        Navegacion.cliente(this, nav, R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Navegacion.marcar(nav, R.id.nav_home);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Productos.referencia().addValueEventListener(escuchaProductos);
        conexion.iniciar();
    }

    @Override
    protected void onStop() {
        Productos.referencia().removeEventListener(escuchaProductos);
        conexion.detener();
        super.onStop();
    }

    private void reiniciarEscucha() {
        estadoVacio.ocultar();
        progreso.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().goOnline();
        Productos.referencia().removeEventListener(escuchaProductos);
        Productos.referencia().addValueEventListener(escuchaProductos);
    }

    private void agregarAlCarrito(Producto producto) {
        if (Carrito.obtener(this).agregar(producto)) {
            Navegacion.marcar(nav, R.id.nav_home);
            Snackbar.make(findViewById(R.id.main), getString(R.string.agregado_al_carrito, producto.getNombre()),
                            Snackbar.LENGTH_SHORT)
                    .setAnchorView(nav)
                    .setAction(R.string.ver_carrito, v -> startActivity(new Intent(this, CartActivity.class)))
                    .show();
        } else {
            Snackbar.make(findViewById(R.id.main), R.string.sin_stock_suficiente, Snackbar.LENGTH_SHORT)
                    .setAnchorView(nav).show();
        }
    }

    private void mostrarTodo() {
        chipsGenero.check(R.id.chipTodos);
        etBuscar.setText("");
        scroll.post(() -> scroll.smoothScrollTo(0, findViewById(R.id.seccionCatalogo).getTop()));
    }

    private void alternarBusqueda() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (tilBuscar.getVisibility() == View.VISIBLE) {
            etBuscar.setText("");
            tilBuscar.setVisibility(View.GONE);
            imm.hideSoftInputFromWindow(etBuscar.getWindowToken(), 0);
        } else {
            tilBuscar.setVisibility(View.VISIBLE);
            etBuscar.requestFocus();
            imm.showSoftInput(etBuscar, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void aplicarFiltros() {
        List<Producto> lista;
        int chip = chipsGenero.getCheckedChipId();
        if (chip == R.id.chipHombre) {
            lista = Productos.porGenero(todos, getString(R.string.genero_hombre));
        } else if (chip == R.id.chipMujer) {
            lista = Productos.porGenero(todos, getString(R.string.genero_mujer));
        } else if (chip == R.id.chipUnisex) {
            lista = Productos.porGenero(todos, getString(R.string.genero_unisex));
        } else if (chip == R.id.chipNuevos) {
            lista = Productos.nuevos(todos, MAX_NUEVOS);
        } else {
            lista = new ArrayList<>(todos);
        }
        lista = Productos.buscar(lista, etBuscar.getText().toString());
        adapter.mostrar(lista);
        tvConteo.setText(getResources().getQuantityString(R.plurals.fragancias_conteo, lista.size(), lista.size()));

        if (!datosCargados) {
            return;
        }
        if (!lista.isEmpty()) {
            estadoVacio.ocultar();
        } else if (todos.isEmpty()) {
            estadoVacio.mostrar(R.drawable.ic_shopping_bag, R.string.catalogo_vacio_titulo,
                    getString(R.string.catalogo_vacio), R.string.escribenos_whatsapp,
                    v -> Contacto.whatsApp(this, getString(R.string.whatsapp_consulta_catalogo)));
        } else {
            estadoVacio.mostrar(R.drawable.ic_search, R.string.sin_resultados,
                    getString(R.string.sin_resultados_mensaje), R.string.limpiar_filtros, v -> mostrarTodo());
        }
    }
}
