package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** Carrito de compras: cantidades, totales y envío del pedido por WhatsApp. */
public class CartActivity extends BaseActivity {

    private Carrito carrito;
    private CarritoAdapter adapter;
    private EstadoVacio estadoVacio;
    private TextView tvSubtotal, tvEnvio, tvTotal;
    private View tvVaciar, btnWhatsApp, cardEnvio, resumen;
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_cart);

        carrito = Carrito.obtener(this);
        estadoVacio = new EstadoVacio(findViewById(R.id.estadoVacio));
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvEnvio = findViewById(R.id.tvEnvio);
        tvTotal = findViewById(R.id.tvTotal);
        tvVaciar = findViewById(R.id.tvVaciar);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        cardEnvio = findViewById(R.id.cardEnvio);
        resumen = findViewById(R.id.bottomSummary);
        nav = findViewById(R.id.bottomNavigation);

        adapter = new CarritoAdapter((item, delta) -> {
            if (!carrito.cambiarCantidad(item.productoId, delta)) {
                aviso(getString(R.string.sin_stock_suficiente));
            } else if (delta < 0 && item.cantidad == 1) {
                // Se quitó la última unidad: se puede deshacer.
                Carrito.Item copia = item;
                Snackbar.make(findViewById(R.id.main), getString(R.string.quitado_del_carrito, item.nombre),
                                Snackbar.LENGTH_LONG)
                        .setAnchorView(nav)
                        .setAction(R.string.deshacer, v -> {
                            carrito.restaurar(copia);
                            actualizar();
                        })
                        .show();
            }
            actualizar();
        });
        RecyclerView rv = findViewById(R.id.rvCarrito);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        tvVaciar.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.vaciar_carrito)
                .setMessage(R.string.confirmar_vaciar)
                .setPositiveButton(R.string.vaciar, (d, w) -> {
                    carrito.vaciar();
                    actualizar();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show());
        btnWhatsApp.setOnClickListener(v -> Contacto.whatsApp(this, carrito.mensajePedido()));

        Navegacion.cliente(this, nav, R.id.nav_cart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizar();
    }

    private void aviso(String texto) {
        Snackbar.make(findViewById(R.id.main), texto, Snackbar.LENGTH_SHORT).setAnchorView(nav).show();
    }

    private void actualizar() {
        List<Carrito.Item> items = carrito.getItems();
        adapter.mostrar(items);
        boolean vacio = items.isEmpty();
        if (vacio) {
            estadoVacio.mostrar(R.drawable.ic_shopping_bag, R.string.carrito_vacio,
                    getString(R.string.carrito_vacio_mensaje), R.string.ver_catalogo,
                    v -> startActivity(new Intent(this, HomeActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        } else {
            estadoVacio.ocultar();
        }
        tvVaciar.setVisibility(vacio ? View.INVISIBLE : View.VISIBLE);
        cardEnvio.setVisibility(vacio ? View.GONE : View.VISIBLE);
        resumen.setVisibility(vacio ? View.GONE : View.VISIBLE);
        tvSubtotal.setText(Formato.precio(carrito.subtotal()));
        tvEnvio.setText(Formato.precio(carrito.envio()));
        tvTotal.setText(Formato.precio(carrito.total()));
        Navegacion.marcar(nav, R.id.nav_cart);
    }
}
