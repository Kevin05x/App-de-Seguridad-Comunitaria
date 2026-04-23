package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoAlerta;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoComunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoContacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoDispositivo;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoMembresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoMultimediaPublicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoPublicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoSistema;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoUsuario;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.ColaSincronizacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Dispositivo;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.MultimediaPublicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;

@Database(
        entities = {
                Usuario.class,
                Comunidad.class,
                Membresia.class,
                Alerta.class,
                Publicacion.class,
                MultimediaPublicacion.class,
                Contacto.class,
                Dispositivo.class,
                ColaSincronizacion.class
        },
        version = 4,
        exportSchema = false
)
public abstract class BaseDeDatosApp extends RoomDatabase {

    public abstract DaoUsuario daoUsuario();
    public abstract DaoComunidad daoComunidad();
    public abstract DaoMembresia daoMembresia();
    public abstract DaoAlerta daoAlerta();
    public abstract DaoPublicacion daoPublicacion();
    public abstract DaoMultimediaPublicacion daoMultimediaPublicacion();
    public abstract DaoContacto daoContacto();
    public abstract DaoDispositivo daoDispositivo();
    public abstract DaoSistema daoSistema();

    private static volatile BaseDeDatosApp INSTANCE;

    public static BaseDeDatosApp obtenerInstancia(final Context context) {
        if (INSTANCE == null) {
            synchronized (BaseDeDatosApp.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    BaseDeDatosApp.class,
                                    "alerta_comunitaria_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
