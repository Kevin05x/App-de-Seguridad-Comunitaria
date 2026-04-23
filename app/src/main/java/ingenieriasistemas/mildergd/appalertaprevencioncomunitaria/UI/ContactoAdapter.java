package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.R;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.OnContactoClickListener;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoVH> {

    private List<Contacto> listaContactos = new ArrayList<>();
    private List<Contacto> listaFiltrada = new ArrayList<>();
    private OnContactoClickListener listener;

    public void setListener(OnContactoClickListener listener) {
        this.listener = listener;
    }

    public void setContactos(List<Contacto> contactos) {
        this.listaContactos = contactos;
        this.listaFiltrada = new ArrayList<>(contactos);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaContactos);
        } else {
            String query = texto.toLowerCase().trim();
            for (Contacto c : listaContactos) {
                if (c.nombre.toLowerCase().contains(query) || 
                    (c.telefono != null && c.telefono.contains(query))) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
        return new ContactoVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoVH holder, int position) {
        Contacto contacto = listaFiltrada.get(position);

        holder.txtContacto.setText(contacto.nombre);
        holder.txtDescripcionContacto.setText(contacto.telefono);
        
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(contacto);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(contacto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public static class ContactoVH extends RecyclerView.ViewHolder {
        public TextView txtContacto, txtDescripcionContacto;
        public ImageButton btnEditar, btnEliminar;

        public ContactoVH(@NonNull View itemView) {
            super(itemView);
            txtContacto = itemView.findViewById(R.id.txtContacto);
            txtDescripcionContacto = itemView.findViewById(R.id.txtDescripcionContacto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}