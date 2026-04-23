package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.R;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;

public class AlertaHistorialAdapter extends RecyclerView.Adapter<AlertaHistorialAdapter.ViewHolder> {
    private List<Alerta> lista;
    private Context context;

    public AlertaHistorialAdapter(Context context, List<Alerta> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alerta, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Alerta alerta = lista.get(position);
        String estado = alerta.estado != null ? alerta.estado : "activa";

        if ("reporte".equalsIgnoreCase(estado)) {
            holder.txtTitulo.setText("Aviso");
        } else {
            holder.txtTitulo.setText("Alerta SOS");
        }

        SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String hora = horaFormat.format(new Date(alerta.fechaCreacion));
        holder.txtHora.setText(hora);

        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fecha = fechaFormat.format(new Date(alerta.fechaCreacion));
        holder.txtFecha.setText(fecha);

        holder.txtEstado.setText(estado.toUpperCase());

        switch (estado.toLowerCase()) {

            case "reporte":
                holder.viewIndicador.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.brand_primary));

                holder.txtEstado.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.brand_primary));

                holder.txtEstado.setTextColor(
                        ContextCompat.getColor(context, R.color.white));

                holder.imgIcono.setImageResource(R.drawable.ic_warning);
                holder.imgIcono.setColorFilter(
                        ContextCompat.getColor(context, R.color.brand_primary),
                        android.graphics.PorterDuff.Mode.SRC_IN
                );
                break;

            case "activa":
                holder.viewIndicador.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.red_sos));

                holder.txtEstado.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.red_sos));

                holder.txtEstado.setTextColor(
                        ContextCompat.getColor(context, R.color.white));

                holder.imgIcono.setImageResource(R.drawable.ic_warning);
                holder.imgIcono.setColorFilter(
                        ContextCompat.getColor(context, R.color.red_sos),
                        android.graphics.PorterDuff.Mode.SRC_IN
                );

                break;

            case "resuelta":
                holder.viewIndicador.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.green_safety_dark));

                holder.txtEstado.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.green_safety_dark));

                holder.txtEstado.setTextColor(
                        ContextCompat.getColor(context, R.color.green_soft_bg));

                holder.imgIcono.setImageResource(R.drawable.ic_check_circulo);
                holder.imgIcono.clearColorFilter();

                break;

            case "cancelada":
                holder.viewIndicador.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.gris_slate_500));

                holder.txtEstado.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.gris_slate_500));

                holder.txtEstado.setTextColor(Color.WHITE);

                holder.imgIcono.setImageResource(R.drawable.ic_close);
                holder.imgIcono.clearColorFilter();

                break;
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarLista(List<Alerta> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        View viewIndicador;
        ImageView imgIcono;

        TextView txtTitulo;
        TextView txtHora;
        TextView txtDescripcion;
        TextView txtEstado;
        TextView txtFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cadrview_item_emergencia);
            viewIndicador = itemView.findViewById(R.id.view_indicador_color);
            imgIcono = itemView.findViewById(R.id.img_icono_tipo_alerta_item);

            txtTitulo = itemView.findViewById(R.id.txt_titulo_alerta_item);
            txtHora = itemView.findViewById(R.id.txt_hora_alerta_item);
            txtDescripcion = itemView.findViewById(R.id.txt_descripcion_alerta_item);
            txtEstado = itemView.findViewById(R.id.txt_estado_alerta_item);
            txtFecha = itemView.findViewById(R.id.txt_fecha_alerta_item);
        }
    }
}