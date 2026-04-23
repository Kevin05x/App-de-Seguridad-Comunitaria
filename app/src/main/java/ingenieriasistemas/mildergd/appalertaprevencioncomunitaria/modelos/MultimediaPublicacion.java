package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "multimedia_publicacion",
        foreignKeys = @ForeignKey(entity = Publicacion.class, parentColumns = "publicacion_id", childColumns = "publicacion_id", onDelete = ForeignKey.CASCADE))
public class MultimediaPublicacion {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "multimedia_id")
    public String multimediaId;

    @NonNull
    @ColumnInfo(name = "publicacion_id")
    public String publicacionId;

    @ColumnInfo(name = "ruta_local")
    public String rutaLocal;

    @ColumnInfo(name = "ruta_storage")
    public String rutaStorage;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "tipo_mime")
    public String tipoMime;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    public int sincronizado;

    public MultimediaPublicacion() {}
}