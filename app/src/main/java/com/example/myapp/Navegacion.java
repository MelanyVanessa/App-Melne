package com.example.myapp;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Conecta las barras de navegación inferiores (cliente y admin) con sus pantallas. */
public final class Navegacion {

    private Navegacion() {
    }

    /** Barra del cliente: Inicio, Carrito, Perfil. */
    public static void cliente(Activity activity, BottomNavigationView nav, @IdRes int seleccionado) {
        marcar(nav, seleccionado);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == seleccionado) {
                return true;
            }
            if (id == R.id.nav_home) {
                abrir(activity, HomeActivity.class);
            } else if (id == R.id.nav_cart) {
                abrir(activity, CartActivity.class);
            } else if (id == R.id.nav_profile) {
                abrir(activity, ProfileActivity.class);
            }
            return true;
        });
    }

    /** Barra del administrador: Panel, Productos, Tienda. */
    public static void admin(Activity activity, BottomNavigationView nav, @IdRes int seleccionado) {
        marcar(nav, seleccionado);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == seleccionado) {
                return true;
            }
            if (id == R.id.nav_home) {
                abrir(activity, AdminDashboardActivity.class);
            } else if (id == R.id.nav_products) {
                abrir(activity, AdminProductsActivity.class);
            } else if (id == R.id.nav_store) {
                abrir(activity, HomeActivity.class);
            }
            return true;
        });
    }

    /** Vuelve a marcar la pestaña correcta y actualiza el contador del carrito. */
    public static void marcar(BottomNavigationView nav, @IdRes int seleccionado) {
        if (nav.getMenu().findItem(seleccionado) != null) {
            nav.getMenu().findItem(seleccionado).setChecked(true);
        }
        if (nav.getMenu().findItem(R.id.nav_cart) != null) {
            int cantidad = Carrito.obtener(nav.getContext()).cantidadTotal();
            BadgeDrawable badge = nav.getOrCreateBadge(R.id.nav_cart);
            badge.setVisible(cantidad > 0);
            badge.setNumber(cantidad);
            badge.setBackgroundColor(ContextCompat.getColor(nav.getContext(), R.color.melne_price));
            badge.setBadgeTextColor(ContextCompat.getColor(nav.getContext(), R.color.melne_on_primary));
            badge.setContentDescriptionQuantityStringsResource(R.plurals.carrito_badge);
        }
    }

    private static void abrir(Activity desde, Class<? extends Activity> destino) {
        Intent intent = new Intent(desde, destino)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        desde.startActivity(intent);
        desde.overridePendingTransition(0, 0);
        // Las pantallas raíz (Inicio cliente y Panel admin) se quedan en la pila.
        if (!(desde instanceof HomeActivity) && !(desde instanceof AdminDashboardActivity)) {
            desde.finish();
        }
    }
}
