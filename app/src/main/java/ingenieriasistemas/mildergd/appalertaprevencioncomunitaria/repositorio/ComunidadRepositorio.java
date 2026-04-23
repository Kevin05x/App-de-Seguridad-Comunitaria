package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ComunidadRepositorio {
    private static final String TAG = "ComunidadRepositorio";
    private final FirebaseFirestore db;
    private final BaseDeDatosApp roomDb;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ComunidadRepositorio(Context context) {
        this.roomDb = BaseDeDatosApp.obtenerInstancia(context);
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void crearComunidad(Comunidad comunidad, Membresia membresia, SimpleCallback callback) {
        comunidad.sincronizado = 0;
        executor.execute(() -> {
            roomDb.daoComunidad().insertar(comunidad);
            roomDb.daoMembresia().insertar(membresia);
            
            mainHandler.post(() -> callback.exito("Comunidad creada localmente. Sincronizando..."));

            WriteBatch batch = db.batch();
            DocumentReference refComunidad = db.collection("comunidades").document(comunidad.comunidadId);
            String membresiaDocId = membresia.usuarioId + "_" + membresia.comunidadId;
            DocumentReference refMembresia = db.collection("membresias").document(membresiaDocId);
            
            batch.set(refComunidad, comunidad);
            batch.set(refMembresia, membresia);

            batch.commit().addOnSuccessListener(aVoid -> {
                executor.execute(() -> roomDb.daoComunidad().marcarSincronizacion(comunidad.comunidadId, System.currentTimeMillis()));
            }).addOnFailureListener(e -> {
                mainHandler.post(() -> callback.error("Error al sincronizar con la nube: " + e.getMessage()));
            });
        });
    }

    public void unirsePorCodigo(String codigo, String usuarioId, SimpleCallback callback) {
        db.collection("comunidades").whereEqualTo("codigoInvitacion", codigo).get()
                .addOnSuccessListener(snaps -> procesarCodigoParaUnirse(snaps, usuarioId, callback))
                .addOnFailureListener(e -> callback.error("Error al buscar código: " + e.getMessage()));
    }

    private void procesarCodigoParaUnirse(QuerySnapshot snaps, String usuarioId, SimpleCallback callback) {
        if (snaps.isEmpty()) {
            callback.error("El código de invitación no es válido o no existe.");
            return;
        }

        Comunidad comNube = snaps.getDocuments().get(0).toObject(Comunidad.class);
        if (comNube == null) {
            callback.error("No se pudo obtener la información de la comunidad.");
            return;
        }

        executor.execute(() -> {
            try {
                if (roomDb.daoUsuario().obtenerPorId(comNube.usuarioCreadorId) == null) {
                    DocumentSnapshot userDoc = Tasks.await(db.collection("usuarios").document(comNube.usuarioCreadorId).get());
                    Usuario creador = userDoc.toObject(Usuario.class);
                    if (creador != null) roomDb.daoUsuario().insertar(creador);
                }

                String membresiaDocId = usuarioId + "_" + comNube.comunidadId;
                DocumentSnapshot memDoc = Tasks.await(db.collection("membresias").document(membresiaDocId).get());
                
                Membresia m;
                if (memDoc.exists()) {
                    m = memDoc.toObject(Membresia.class);
                } else {
                    m = new Membresia();
                    m.comunidadId = comNube.comunidadId;
                    m.usuarioId = usuarioId;
                    m.rol = "miembro";
                    m.fechaIngreso = System.currentTimeMillis();
                    m.notificacionesActivas = 1;
                    m.silenciado = 0;
                    Tasks.await(db.collection("membresias").document(membresiaDocId).set(m));
                }


                comNube.sincronizado = 1;
                roomDb.daoComunidad().insertar(comNube);
                if (m != null) roomDb.daoMembresia().insertar(m);

                mainHandler.post(() -> callback.exito("¡Te has unido con éxito a " + comNube.nombre + "!"));

            } catch (Exception e) {
                Log.e(TAG, "Error al unirse: " + e.getMessage());
                mainHandler.post(() -> callback.error("Error al procesar la unión: " + e.getMessage()));
            }
        });
    }
}
