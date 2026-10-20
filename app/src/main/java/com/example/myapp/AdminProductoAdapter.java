package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/** Filas de la lista "Mis Productos" del administrador, con editar y eliminar. */
public class AdminProductoAdapter extends RecyclerView.Adapter<AdminProductoAdapter.ViewHolder> {

    public interface Acciones {
        void onEditar(Producto producto);

        void onEliminar(Producto producto);
    }

    private final List<Producto> productos = new ArrayList<>();
    private final Acciones acciones;

    public AdminProductoAdapter(Acciones acciones) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = productos.get(position);
        h.marca.setText(p.getMarca());
        h.nombre.setText(p.getNombre());
        h.precio.setText(Formato.precio(p.getPrecio()));

        boolean agotado = p.isAgotado();
        h.stock.setText(h.itemView.getContext().getString(
                agotado ? R.string.stock_agotado : R.string.stock_activo, p.getStock()));
        int color = ContextCompat.getColor(h.itemView.getContext(),
                agotado ? R.color.status_pending_text : R.color.status_delivered_text);
        h.stock.setTextColor(color);
        h.stock.setCompoundDrawablesRelativeWithIntrinsicBounds(
                agotado ? R.drawable.ic_cancel : R.drawable.ic_check_circle, 0, 0, 0);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(h.stock,
                android.content.res.ColorStateList.valueOf(color));
        h.btnEditar.setContentDescription(h.itemView.getContext().getString(R.string.editar_producto_de, p.getNombre()));
        h.btnEliminar.setContentDescription(h.itemView.getContext().getString(R.string.eliminar_producto_de, p.getNombre()));

        Glide.with(h.imagen).load(p.getImagenUrl())
                .placeholder(R.drawable.placeholder_producto)
                .error(R.drawable.placeholder_producto)
                .centerCrop()
                .into(h.imagen);

        h.btnEditar.setOnClickListener(v -> acciones.onEditar(p));
        h.btnEliminar.setOnClickListener(v -> acciones.onEliminar(p));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imagen;
        final TextView marca, nombre, precio, stock;
        final View btnEditar, btnEliminar;

        ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.imgProducto);
            marca = v.findViewById(R.id.tvMarca);
            nombre = v.findViewById(R.id.tvNombre);
            precio = v.findViewById(R.id.tvPrecio);
            stock = v.findViewById(R.id.tvStock);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnEliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
