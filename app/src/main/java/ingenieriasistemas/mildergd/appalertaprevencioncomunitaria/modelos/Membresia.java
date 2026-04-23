package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "membresia",
        foreignKeys = {
                @ForeignKey(entity = Comunidad.class, parentColumns = "comunidad_id", childColumns = "comunidad_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class, parentColumns = "usuario_id", childColumns = "usuario_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = {"comunidad_id", "usuario_id"}, unique = true)})
public class Membresia {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "membresia_id")
    public int membresiaId;

    @NonNull
    @ColumnInfo(name = "comunidad_id")
    public String comunidadId;

    @NonNull
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "rol", defaultValue = "miembro")
    public String rol;

    @ColumnInfo(name = "fecha_ingreso")
    public long fechaIngreso;

    @ColumnInfo(name = "silenciado", defaultValue = "0")
    public int silenciado;

    @ColumnInfo(name = "notificaciones_activas", defaultValue = "1")
    public int notificacionesActivas;

    public Membresia() {}
}