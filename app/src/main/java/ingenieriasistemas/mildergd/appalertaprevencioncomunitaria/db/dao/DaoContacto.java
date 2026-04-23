package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;

@Dao
public interface DaoContacto {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Contacto contacto);

    @Update
    void actualizar(Contacto contacto);

    @Delete
    void eliminar(Contacto contacto);

    @Query("SELECT * FROM contacto WHERE contacto_id = :id")
    Contacto obtenePorId(int id);

    @Query("SELECT * FROM contacto WHERE usuario_id = :usuarioId")
    List<Contacto> obtenerPorUsuario(String usuarioId);

    @Query("SELECT * FROM contacto WHERE usuario_id = :usuarioId ORDER BY nombre ASC")
    List<Contacto> obtenerTodosParaAlerta(String usuarioId);

    @Query("SELECT * FROM contacto WHERE sincronizado = 0")
    List<Contacto> obtenerNoSincronizados();

    @Query("UPDATE contacto SET fecha_actualizacion = :marcaTiempo, sincronizado = 1 WHERE contacto_id = :id")
    void marcarSincronizado(int id, long marcaTiempo);
}