package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cola_sincronizacion")
public class ColaSincronizacion {

 @PrimaryKey(autoGenerate = true)
 @ColumnInfo(name = "cola_id")
 public int colaId;

 @NonNull
 @ColumnInfo(name = "tipo_entidad")
 public String tipoEntidad;

 @NonNull
 @ColumnInfo(name = "entidad_id")
 public String entidadId;

 @NonNull
 @ColumnInfo(name = "operacion")
 public String operacion;

 @ColumnInfo(name = "datos_json")
 public String datosJson;

 @ColumnInfo(name = "fecha_creacion")
 public long fechaCreacion;

 @ColumnInfo(name = "intentos", defaultValue = "0")
 public int intentos;

 public ColaSincronizacion() {}
}