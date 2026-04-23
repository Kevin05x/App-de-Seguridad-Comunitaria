package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "comunidad",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "usuario_id",
                childColumns = "usuario_creador_id",
                onDelete = ForeignKey.SET_NULL),
        indices = {@Index(value = "codigo_invitacion", unique = true)})
public class Comunidad {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "comunidad_id")
    public String comunidadId;

    @ColumnInfo(name = "usuario_creador_id")
    public String usuarioCreadorId;

    @NonNull
    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "codigo_invitacion")
    public String codigoInvitacion;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "direccion")
    public String direccion;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_actualizacion")
    public long fechaActualizacion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    public Comunidad() {}
}