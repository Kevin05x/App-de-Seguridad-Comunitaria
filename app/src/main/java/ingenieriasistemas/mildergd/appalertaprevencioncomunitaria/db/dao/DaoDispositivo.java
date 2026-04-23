package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Dispositivo;

@Dao
public interface DaoDispositivo {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Dispositivo dispositivo);

    @Update
    void actualizar(Dispositivo dispositivo);

    @Delete
    void eliminar(Dispositivo dispositivo);

    @Query("SELECT * FROM dispositivo WHERE dispositivo_id = :id")
    Dispositivo obtenerPorId(String id);

    @Query("SELECT * FROM dispositivo WHERE usuario_id = :usuarioId")
    List<Dispositivo> obtenerPorUsuario(String usuarioId);

    @Query("SELECT * FROM dispositivo WHERE sincronizado = 0")
    List<Dispositivo> obtenerNoSincronizados();

    @Query("UPDATE dispositivo SET fecha_actualizacion = :timestamp, sincronizado = 1 WHERE dispositivo_id = :id")
    void marcarSincronizado(String id, long timestamp);

    @Query("SELECT d.* FROM dispositivo d INNER JOIN membresia m ON d.usuario_id = m.usuario_id WHERE m.comunidad_id = :comunidadId AND m.notificaciones_activas = 1")
    List<Dispositivo> obtenerTokensPorComunidad(String comunidadId);
}