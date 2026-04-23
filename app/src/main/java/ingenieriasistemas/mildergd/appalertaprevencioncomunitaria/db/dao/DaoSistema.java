package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.google.gson.Gson;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.ColaSincronizacion;

@Dao
public interface DaoSistema {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarCola(ColaSincronizacion cola);

    @Update
    void actualizarCola(ColaSincronizacion cola);

    @Delete
    void eliminarCola(ColaSincronizacion cola);

    @Query("SELECT * FROM cola_sincronizacion WHERE cola_id = :id")
    ColaSincronizacion obtenerColaPorId(int id);

    @Query("SELECT * FROM cola_sincronizacion WHERE tipo_entidad = :tipo AND intentos < 5 ORDER BY fecha_creacion ASC")
    List<ColaSincronizacion> obtenerNoSincronizadasPorTipo(String tipo);

    @Query("SELECT * FROM cola_sincronizacion WHERE intentos < 5 ORDER BY fecha_creacion ASC")
    List<ColaSincronizacion> obtenerTodasNoSincronizadas();

    @Query("UPDATE cola_sincronizacion SET intentos = intentos + 1 WHERE cola_id = :id")
    void incrementarIntentosCola(int id);

    @Query("DELETE FROM cola_sincronizacion WHERE cola_id = :id")
    void marcarColaCompletado(int id);

    default String ponerEnColaConNuevoId(String tipoEntidad, String operacion, Object entidad, Gson gson){
        String nuevaId = java.util.UUID.randomUUID().toString();

        ColaSincronizacion cola = new ColaSincronizacion();
        cola.tipoEntidad = tipoEntidad;
        cola.entidadId = nuevaId;
        cola.operacion = operacion;
        cola.datosJson = gson.toJson(entidad);
        cola.fechaCreacion = System.currentTimeMillis();
        cola.intentos = 0;

        insertarCola(cola);
        return nuevaId;
    }
}