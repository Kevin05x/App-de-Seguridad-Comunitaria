package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI;

import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.R;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.dto.PublicacionDTO;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.OnPublicacionClickListener;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionVH> {
    private List<PublicacionDTO> listaPosts = new ArrayList<>();
    private OnPublicacionClickListener listener;

    public void setListener(OnPublicacionClickListener listener) {
        this.listener = listener;
    }

    public void setPublicaciones(List<PublicacionDTO> nuevasPublicaciones) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PublicacionDiffCallback(this.listaPosts, nuevasPublicaciones));
        this.listaPosts = new ArrayList<>(nuevasPublicaciones);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public PublicacionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PublicacionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionVH holder, int position) {
        PublicacionDTO dto = listaPosts.get(position);
        
        holder.txtNombreUsuario.setText(dto.nombreAutor != null ? dto.nombreAutor : "Usuario");
        holder.txtContenido.setText(dto.publicacion.contenido);
        holder.txtEtiquetaTipo.setText(dto.publicacion.categoria != null ? dto.publicacion.categoria : "Aviso");
        
        long timestamp = dto.publicacion.fechaCreacion;
        CharSequence tiempoRelativo = DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.txtTiempo.setText("• " + tiempoRelativo);

        holder.btnUtil.setText(dto.publicacion.contadorUtil + " Útil");

        if ("¡EMERGENCIA!".equals(dto.publicacion.categoria)) {
            holder.txtEtiquetaTipo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark)));
            holder.txtEtiquetaTipo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            int colorPrimary = ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_primary);
            holder.txtEtiquetaTipo.setBackgroundTintList(ColorStateList.valueOf(colorPrimary).withAlpha(40));
            holder.txtEtiquetaTipo.setTextColor(colorPrimary);
        }

        if (dto.publicacion.latitud != 0 && dto.publicacion.longitud != 0) {
            holder.cardMapa.setVisibility(View.VISIBLE);
            holder.setMapLocation(dto.publicacion.latitud, dto.publicacion.longitud);
        } else {
            holder.cardMapa.setVisibility(View.GONE);
        }

        if (dto.esUtilParaUsuarioActual) {
            int color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_dark);
            holder.btnUtil.setIconTint(ColorStateList.valueOf(color));
            holder.btnUtil.setTextColor(color);
            holder.btnUtil.setEnabled(false);
        } else {
            holder.btnUtil.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.gris_slate_400)));
            holder.btnUtil.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gris_slate_400));
            holder.btnUtil.setEnabled(true);
            holder.btnUtil.setOnClickListener(v -> {
                if (listener != null) listener.onUtilClick(dto);
            });
        }

        String urlImagen = null;
        if (dto.multimedia != null) {
            urlImagen = (dto.multimedia.url != null && !dto.multimedia.url.isEmpty()) ? dto.multimedia.url : dto.multimedia.rutaLocal;
        } else if (dto.publicacion.urlImagen != null && !dto.publicacion.urlImagen.isEmpty()) {
            urlImagen = dto.publicacion.urlImagen;
        }

        if (urlImagen != null && !urlImagen.isEmpty()) {
            holder.imgContenido.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .centerCrop()
                    .placeholder(R.drawable.ic_logo_user)
                    .error(R.drawable.ic_warning)
                    .into(holder.imgContenido);
        } else {
            holder.imgContenido.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaPosts.size();
    }

    public static class PublicacionVH extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public TextView txtNombreUsuario, txtContenido, txtEtiquetaTipo, txtTiempo;
        public ImageView imgContenido;
        public ShapeableImageView imgAvatar;
        public MaterialButton btnUtil;
        public View cardMapa;
        public MapView mapView;
        public GoogleMap googleMap;
        public LatLng location;

        public PublicacionVH(@NonNull View itemView) {
            super(itemView);
            txtNombreUsuario = itemView.findViewById(R.id.txtNombreUsuario);
            txtContenido = itemView.findViewById(R.id.txtContenidoPost);
            txtEtiquetaTipo = itemView.findViewById(R.id.txtEtiquetaTipo);
            txtTiempo = itemView.findViewById(R.id.txtTiempoPublicacion);
            imgContenido = itemView.findViewById(R.id.imgContenidoMultimedia);
            imgAvatar = itemView.findViewById(R.id.imgAvatarPerfil);
            btnUtil = itemView.findViewById(R.id.btnUtil);
            cardMapa = itemView.findViewById(R.id.cardMapaPost);
            mapView = itemView.findViewById(R.id.mapViewPost);

            if (mapView != null) {
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            }
        }

        public void setMapLocation(double lat, double lng) {
            location = new LatLng(lat, lng);
            if (googleMap != null) {
                updateMapContents();
            }
        }

        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            MapsInitializer.initialize(itemView.getContext().getApplicationContext());
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            if (location != null) {
                updateMapContents();
            }
        }

        private void updateMapContents() {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }

    private static class PublicacionDiffCallback extends DiffUtil.Callback {
        private final List<PublicacionDTO> oldList;
        private final List<PublicacionDTO> newList;

        public PublicacionDiffCallback(List<PublicacionDTO> oldList, List<PublicacionDTO> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return Objects.equals(oldList.get(oldPos).publicacion.publicacionId, newList.get(newPos).publicacion.publicacionId);
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            PublicacionDTO oldItem = oldList.get(oldPos);
            PublicacionDTO newItem = newList.get(newPos);
            return Objects.equals(oldItem.publicacion.contenido, newItem.publicacion.contenido) &&
                   oldItem.publicacion.contadorUtil == newItem.publicacion.contadorUtil &&
                   oldItem.esUtilParaUsuarioActual == newItem.esUtilParaUsuarioActual &&
                   Objects.equals(oldItem.nombreAutor, newItem.nombreAutor);
        }
    }
}
