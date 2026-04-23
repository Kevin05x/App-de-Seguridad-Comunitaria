package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;

@Dao
public interface DaoAlerta {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Alerta alerta);

    @Update
    void actualizar(Alerta alerta);

    @Delete
    void eliminar(Alerta alerta);

    @Query("SELECT * FROM alerta WHERE alerta_id = :id")
    Alerta obtenerPorId(String id);

    @Query("SELECT * FROM alerta WHERE comunidad_id = :comunidadId AND estado = 'activa' ORDER BY fecha_creacion DESC")
    List<Alerta> obtenerActivasPorComunidad(String comunidadId);

    @Query("SELECT * FROM alerta WHERE usuario_id = :usuarioId ORDER BY fecha_creacion DESC")
    List<Alerta> obtenerPorUsuario(String usuarioId);

    @Query("SELECT * FROM alerta WHERE sincronizado = 0")
    List<Alerta> obtenerNoSincronizadas();

    @Query("UPDATE alerta SET estado = :estado, fecha_resolucion = :marcaTiempo WHERE alerta_id = :id")
    void actualizarEstado(String id, String estado, long marcaTiempo);

    @Query("UPDATE alerta SET fecha_actualizacion = :marcaTiempo, sincronizado = 1 WHERE alerta_id = :id")
    void marcarSincronizacion(String id, long marcaTiempo);
}