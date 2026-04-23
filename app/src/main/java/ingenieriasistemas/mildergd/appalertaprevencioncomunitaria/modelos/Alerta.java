package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerta",
        foreignKeys = {
                @ForeignKey(entity = Comunidad.class, parentColumns = "comunidad_id", childColumns = "comunidad_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class, parentColumns = "usuario_id", childColumns = "usuario_id", onDelete = ForeignKey.CASCADE)
        })
public class Alerta {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "alerta_id")
    public String alertaId;

    @NonNull
    @ColumnInfo(name = "comunidad_id")
    public String comunidadId;

    @NonNull
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "estado", defaultValue = "activa")
    public String estado;

    @ColumnInfo(name = "latitud")
    public double latitud;

    @ColumnInfo(name = "longitud")
    public double longitud;

    @ColumnInfo(name = "ubicacion")
    public String ubicacion;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_actualizacion")
    public long fechaActualizacion;

    @ColumnInfo(name = "fecha_resolucion")
    public long fechaResolucion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    public Alerta() {}
}