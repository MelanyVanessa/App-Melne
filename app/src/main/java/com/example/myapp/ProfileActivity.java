package com.example.myapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Perfil de la tienda: contacto, información y acceso al panel de administración. */
public class ProfileActivity extends BaseActivity {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarPantalla(R.layout.activity_profile);

        nav = findViewById(R.id.bottomNavigation);
        Navegacion.cliente(this, nav, R.id.nav_profile);

        findViewById(R.id.btnContactoRapido).setOnClickListener(v -> Contacto.whatsApp(this, null));
        opcion(R.id.itemWhatsApp, R.drawable.ic_phone, R.string.contactar_whatsapp,
                getString(R.string.whatsapp_mostrar), v -> Contacto.whatsApp(this, null));
        opcion(R.id.itemInstagram, R.drawable.ic_instagram, R.string.seguir_instagram,
                getString(R.string.instagram_mostrar), v -> Contacto.instagram(this));
        opcion(R.id.itemDireccion, R.drawable.ic_location, R.string.direccion_envio,
                getString(R.string.direccion_tienda), v -> Contacto.mapa(this, getString(R.string.direccion_tienda)));
        String[] temas = getResources().getStringArray(R.array.temas);
        opcion(R.id.itemApariencia, R.drawable.ic_tema, R.string.apariencia,
                temas[Tema.actual(this)], v -> new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.apariencia)
                        .setSingleChoiceItems(temas, Tema.actual(this), (d, cual) -> {
                            d.dismiss();
                            Tema.guardarYAplicar(this, cual);
                        })
                        .setNegativeButton(R.string.cancelar, null)
                        .show());
        opcion(R.id.itemAcerca, R.drawable.ic_info, R.string.acerca_titulo,
                getString(R.string.acerca_resumen), v -> new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.acerca_titulo)
                        .setMessage(R.string.acerca_mensaje)
                        .setPositiveButton(R.string.aceptar, null)
                        .show());
        opcion(R.id.itemAdmin, R.drawable.ic_settings, R.string.acceso_admin,
                getString(R.string.acceso_admin_detalle),
                v -> AdminActivity.abrir(this, AdminDashboardActivity.class));

        ((TextView) findViewById(R.id.tvVersion)).setText(getString(R.string.version_formato, versionApp()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Navegacion.marcar(nav, R.id.nav_profile);
    }

    private void opcion(@IdRes int id, @DrawableRes int icono, @StringRes int titulo, String detalle,
                        View.OnClickListener alPulsar) {
        View fila = findViewById(id);
        ((ImageView) fila.findViewById(R.id.icono)).setImageResource(icono);
        ((TextView) fila.findViewById(R.id.titulo)).setText(titulo);
        ((TextView) fila.findViewById(R.id.detalle)).setText(detalle);
        fila.setOnClickListener(alPulsar);
    }

    private String versionApp() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
