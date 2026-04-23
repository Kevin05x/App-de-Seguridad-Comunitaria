package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;

@Dao
public interface DaoComunidad {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Comunidad comunidad);

    @Update
    void actualizar(Comunidad comunidad);

    @Delete
    void eliminar(Comunidad comunidad);

    @Query("SELECT * FROM comunidad WHERE comunidad_id = :id")
    Comunidad obtenerPorId(String id);

    @Query("SELECT * FROM comunidad WHERE codigo_invitacion = :codigo LIMIT 1")
    Comunidad obtenerPorCodigoInvitacion(String codigo);

    @Query("SELECT * FROM comunidad ORDER BY nombre ASC")
    List<Comunidad> obtenerTodas();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarMembresia(Membresia membresia);

    @Query("SELECT c.* FROM comunidad c INNER JOIN membresia m ON c.comunidad_id = m.comunidad_id WHERE m.usuario_id = :idUsuario")
    List<Comunidad> obtenerComunidadesDelUsuario(String idUsuario);

    @Query("SELECT COUNT(*) FROM membresia WHERE comunidad_id = :idComunidad AND usuario_id = :idUsuario")
    int esMiembro(String idComunidad, String idUsuario);

    @Query("UPDATE comunidad SET fecha_actualizacion = :marcaTiempo, sincronizado = 1 WHERE comunidad_id = :id")
    void marcarSincronizacion(String id, long marcaTiempo);
}