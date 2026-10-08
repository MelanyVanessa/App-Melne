package com.example.myapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity5 extends AppCompatActivity {

    private EditText etNombre, etMarca, etPrecio, etStock, etTamaño;
    private Spinner etCategoria, etTipoReplica;
    private Button btnGuardar;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);

        // Inicializar vistas
        btnBack = findViewById(R.id.btnBack);
        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etCategoria = findViewById(R.id.etCategoria);
        etPrecio = findViewById(R.id.etPrecio);
        etStock = findViewById(R.id.etStock);
        etTamaño = findViewById(R.id.etTamaño);
        etTipoReplica = findViewById(R.id.etTipoReplica);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Configurar Spinners con opciones por defecto
        String[] categorias = {"Perfumería", "Cuidado Personal", "Cosméticos", "Otros"};
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etCategoria.setAdapter(adapterCategoria);

        String[] tiposReplica = {"Calidad 1:1", "Triple A", "Tester", "Original"};
        ArrayAdapter<String> adapterReplica = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposReplica);
        adapterReplica.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etTipoReplica.setAdapter(adapterReplica);

        // Botón de regresar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myproductsRef = database.getReference("producto");

        // Guardar producto
        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String marca = etMarca.getText().toString().trim();
            String categoria = etCategoria.getSelectedItem() != null ? etCategoria.getSelectedItem().toString() : "";
            String precio = etPrecio.getText().toString().trim();
            String stock = etStock.getText().toString().trim();
            String tamaño = etTamaño.getText().toString().trim();
            String tipoReplica = etTipoReplica.getSelectedItem() != null ? etTipoReplica.getSelectedItem().toString() : "";

            if (TextUtils.isEmpty(nombre)) {
                etNombre.setError("El nombre es requerido");
                return;
            }

            if (TextUtils.isEmpty(precio)) {
                etPrecio.setError("El precio es requerido");
                return;
            }

            Producto producto = new Producto(nombre, marca, categoria, precio, stock, tamaño, tipoReplica);

            myproductsRef.push().setValue(producto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity5.this, "Producto guardado correctamente", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity5.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etMarca.setText("");
        etPrecio.setText("");
        etStock.setText("");
        etTamaño.setText("");
        if (etCategoria.getAdapter() != null && etCategoria.getAdapter().getCount() > 0) {
            etCategoria.setSelection(0);
        }
        if (etTipoReplica.getAdapter() != null && etTipoReplica.getAdapter().getCount() > 0) {
            etTipoReplica.setSelection(0);
        }
    }
}
