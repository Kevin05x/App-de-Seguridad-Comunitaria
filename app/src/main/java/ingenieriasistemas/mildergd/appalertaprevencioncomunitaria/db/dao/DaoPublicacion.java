package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;

@Dao
public interface DaoPublicacion {
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    void insertar (Publicacion publicacion);

    @Update
    void actualizar(Publicacion publicacion);

    @Delete
    void eliminar (Publicacion publicacion);

    @Query("SELECT * FROM publicacion WHERE publicacion_id = :id")
    Publicacion obtenerPorId(String id);

    @Query("SELECT * FROM publicacion WHERE sincronizado = 0")
    List<Publicacion> obtenerPublicacionesNoSincronizadas();

    @Query("SELECT * FROM publicacion WHERE comunidad_id = :comunidadId ORDER BY fecha_creacion DESC LIMIT :limite")
    List<Publicacion> obtenerMuroComunidad(String comunidadId, int limite);

    @Query("SELECT * FROM publicacion WHERE comunidad_id = :comunidadId AND categoria = :categoria ORDER BY fecha_creacion DESC")
    List<Publicacion> obtenerPorCategoriaComunidad(String comunidadId, String categoria);

    @Query("UPDATE publicacion SET contador_util = :util WHERE publicacion_id =:id")
    void actualizarContadores(String id, int util);

    @Query("UPDATE publicacion SET fecha_actualizacion = :marcaTiempo, sincronizado = 1 WHERE publicacion_id = :idPublicacion")
    void marcarPublicacionSincronizada(String idPublicacion, long marcaTiempo);
}