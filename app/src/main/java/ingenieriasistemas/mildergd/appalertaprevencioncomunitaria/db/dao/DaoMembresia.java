package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;

@Dao
public interface DaoMembresia {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Membresia membresia);

    @Query("SELECT * FROM membresia WHERE usuario_id = :usuarioId")
    List<Membresia> obtenerPorUsuario(String usuarioId);

    @Query("SELECT * FROM membresia WHERE comunidad_id = :comunidadId")
    List<Membresia> obtenerPorComunidad(String comunidadId);

}
