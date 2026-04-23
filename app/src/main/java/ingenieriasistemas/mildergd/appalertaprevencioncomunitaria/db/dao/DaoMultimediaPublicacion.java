package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.MultimediaPublicacion;

@Dao
public interface DaoMultimediaPublicacion {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(MultimediaPublicacion multimedia);

    @Update
    void actualizar(MultimediaPublicacion multimedia);

    @Delete
    void eliminar(MultimediaPublicacion multimedia);

    @Query("SELECT * FROM multimedia_publicacion WHERE multimedia_id = :id")
    MultimediaPublicacion obtenerPorId(String id);

    @Query("SELECT * FROM multimedia_publicacion WHERE publicacion_id = :publicacionId LIMIT 1")
    MultimediaPublicacion obtenerPorPublicacionId(String publicacionId);

    @Query("SELECT * FROM multimedia_publicacion WHERE publicacion_id = :publicacionId AND tipo_mime LIKE 'image/%'")
    List<MultimediaPublicacion> ObtenerFotosPublicacion(String publicacionId);

    @Query("SELECT * FROM multimedia_publicacion WHERE sincronizado = 0")
    List<MultimediaPublicacion> obtenerNoSincronizados();
}
