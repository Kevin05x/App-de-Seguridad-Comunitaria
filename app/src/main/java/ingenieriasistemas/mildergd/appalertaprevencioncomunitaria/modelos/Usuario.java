package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuario",
        indices = {@Index(value = "correo", unique = true)})
public class Usuario {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "correo")
    public String correo;

    @ColumnInfo(name = "telefono")
    public String telefono;

    @ColumnInfo(name = "nombres")
    public String nombres;

    @ColumnInfo(name = "apellidos")
    public String apellidos;

    @ColumnInfo(name = "ruta_foto_local")
    public String rutaFotoLocal;

    @ColumnInfo(name = "url_foto")
    public String urlFoto;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_actualizacion")
    public long fechaActualizacion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    public Usuario() {}
}