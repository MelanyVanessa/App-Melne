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

import java.util.ArrayList;
import java.util.List;

/** Líneas del carrito con selector de cantidad. */
public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.ViewHolder> {

    public interface OnCantidadListener {
        void onCambiarCantidad(Carrito.Item item, int delta);
    }

    private final List<Carrito.Item> items = new ArrayList<>();
    private final OnCantidadListener listener;

    public CarritoAdapter(OnCantidadListener listener) {
        this.listener = listener;
    }

    public void mostrar(List<Carrito.Item> nuevos) {
        items.clear();
        items.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Carrito.Item item = items.get(position);
        Context c = h.itemView.getContext();
        h.marca.setText(item.marca);
        h.nombre.setText(item.nombre);
        String tamano = Formato.tamano(item.tamano);
        h.tamano.setVisibility(tamano.isEmpty() ? View.GONE : View.VISIBLE);
        h.tamano.setText(c.getString(R.string.tamano_formato, tamano));
        h.precio.setText(Formato.precio(item.precio));
        h.cantidad.setText(String.valueOf(item.cantidad));
        h.cantidad.setContentDescription(c.getResources().getQuantityString(R.plurals.cantidad_de, item.cantidad, item.cantidad, item.nombre));
        h.btnMas.setContentDescription(c.getString(R.string.agregar_uno_de, item.nombre));
        h.btnMenos.setContentDescription(c.getString(item.cantidad == 1 ? R.string.quitar_del_carrito
                : R.string.quitar_uno_de, item.nombre));
        Glide.with(h.imagen).load(item.imagenUrl)
                .placeholder(R.drawable.placeholder_producto)
                .error(R.drawable.placeholder_producto)
                .centerCrop()
                .into(h.imagen);

        h.btnMenos.setOnClickListener(v -> listener.onCambiarCantidad(item, -1));
        h.btnMas.setOnClickListener(v -> listener.onCambiarCantidad(item, 1));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imagen;
        final TextView marca, nombre, tamano, precio, cantidad;
        final View btnMenos, btnMas;

        ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.imgProducto);
            marca = v.findViewById(R.id.tvMarca);
            nombre = v.findViewById(R.id.tvNombre);
            tamano = v.findViewById(R.id.tvTamano);
            precio = v.findViewById(R.id.tvPrecio);
            cantidad = v.findViewById(R.id.tvCantidad);
            btnMenos = v.findViewById(R.id.btnMenos);
            btnMas = v.findViewById(R.id.btnMas);
        }
    }
}
