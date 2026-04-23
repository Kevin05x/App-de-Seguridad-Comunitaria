package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.ActivityRegistrarComunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.R;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;

public class AdapterComunidad extends RecyclerView.Adapter<AdapterComunidad.ComunidadVH> {

    private Context contexto;
    private List<Comunidad> lista;

    public AdapterComunidad(Context contexto, List<Comunidad> lista) {
        this.contexto = contexto;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ComunidadVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contexto).inflate(R.layout.item_comunidad, parent, false);
        return new ComunidadVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ComunidadVH holder, int position) {
        Comunidad comunidad = lista.get(position);

        holder.txtNombre.setText(comunidad.nombre);

        holder.txtUbicacion.setText(comunidad.direccion != null ? comunidad.direccion : "Ubicación no disponible");
        holder.txtResidencias.setText("Mi comunidad");

        holder.btnCambiar.setOnClickListener(v ->
                contexto.startActivity(new Intent(contexto, ActivityRegistrarComunidad.class))
        );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ComunidadVH extends RecyclerView.ViewHolder {
        TextView txtNombre, txtUbicacion, txtResidencias;
        MaterialButton btnCambiar;

        public ComunidadVH(@NonNull View itemView) {
            super(itemView);
            txtNombre      = itemView.findViewById(R.id.txtNombreComunidad);
            txtUbicacion   = itemView.findViewById(R.id.txtUbicacion);
            txtResidencias = itemView.findViewById(R.id.txtResidencias);
            btnCambiar     = itemView.findViewById(R.id.btnCambiar);
        }
    }
}