package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Fichas de producto del catálogo del cliente (cuadrícula de 2 columnas). */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    public interface Acciones {
        void onAgregar(Producto producto);

        void onAbrir(Producto producto);
    }

    private final List<Producto> productos = new ArrayList<>();
    private final Acciones acciones;

    public ProductoAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void mostrar(List<Producto> nuevos) {
        productos.clear();
        productos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = productos.get(position);
        Context c = h.itemView.getContext();
        String precio = Formato.precio(p.getPrecio());
        h.marca.setText(p.getMarca());
        h.nombre.setText(p.getNombre());
        h.precio.setText(precio);
        Glide.with(h.imagen).load(p.getImagenUrl())
                .placeholder(R.drawable.placeholder_producto)
                .error(R.drawable.placeholder_producto)
                .centerCrop()
                .into(h.imagen);

        boolean agotado = p.isAgotado();
        h.agotado.setVisibility(agotado ? View.VISIBLE : View.GONE);
        h.imagen.setAlpha(agotado ? 0.55f : 1f);
        h.btnAgregar.setEnabled(!agotado);
        h.btnAgregar.setVisibility(agotado ? View.GONE : View.VISIBLE);
        h.btnAgregar.setContentDescription(c.getString(R.string.anadir_producto_al_carrito, p.getNombre()));
        h.btnAgregar.setOnClickListener(v -> acciones.onAgregar(p));

        // TalkBack lee la ficha completa en una sola frase.
        h.itemView.setContentDescription(c.getString(agotado ? R.string.ficha_agotada : R.string.ficha_producto,
                p.getNombre(), p.getMarca(), precio));
        h.itemView.setOnClickListener(v -> acciones.onAbrir(p));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imagen;
        final TextView marca, nombre, precio, agotado;
        final MaterialButton btnAgregar;

        ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.imgProducto);
            marca = v.findViewById(R.id.tvMarca);
            nombre = v.findViewById(R.id.tvNombre);
            precio = v.findViewById(R.id.tvPrecio);
            agotado = v.findViewById(R.id.tvAgotado);
            btnAgregar = v.findViewById(R.id.btnAgregar);
        }
    }
}
