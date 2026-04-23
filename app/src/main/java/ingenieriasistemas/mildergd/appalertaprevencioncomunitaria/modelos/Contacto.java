package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacto",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "usuario_id",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE))
public class Contacto {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contacto_id")
    public int contactoId;

    @NonNull
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @NonNull
    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "telefono")
    public String telefono;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_actualizacion")
    public long fechaActualizacion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    public Contacto() {}
}