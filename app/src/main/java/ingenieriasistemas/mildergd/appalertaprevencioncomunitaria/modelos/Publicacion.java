package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "publicacion",
        foreignKeys = {
                @ForeignKey(entity = Comunidad.class, parentColumns = "comunidad_id", childColumns = "comunidad_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class, parentColumns = "usuario_id", childColumns = "usuario_id", onDelete = ForeignKey.CASCADE)
        })
public class Publicacion {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "publicacion_id")
    public String publicacionId;

    @NonNull
    @ColumnInfo(name = "comunidad_id")
    public String comunidadId;

    @NonNull
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "categoria")
    public String categoria;

    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "contenido")
    public String contenido;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_actualizacion")
    public long fechaActualizacion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    @ColumnInfo(name = "contador_util", defaultValue = "0")
    public int contadorUtil;

    @ColumnInfo(name = "url_imagen")
    public String urlImagen;

    @ColumnInfo(name = "latitud")
    public double latitud;

    @ColumnInfo(name = "longitud")
    public double longitud;

    @ColumnInfo(name = "direccion")
    public String direccion;

    public Publicacion() {}
}
