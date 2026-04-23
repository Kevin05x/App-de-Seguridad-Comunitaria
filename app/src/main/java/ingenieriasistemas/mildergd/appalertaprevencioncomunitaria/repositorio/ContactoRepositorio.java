package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoContacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ContactoRepositorio {

    private final DaoContacto daoContacto;
    private final FirebaseFirestore db;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ContactoRepositorio(Context context) {
        BaseDeDatosApp database = BaseDeDatosApp.obtenerInstancia(context);
        this.daoContacto = database.daoContacto();
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sincronizarContactos(String usuarioId, SimpleCallback callback) {
        db.collection("contactos")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executor.execute(() -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Contacto contacto = doc.toObject(Contacto.class);
                            daoContacto.insertar(contacto);
                        }
                        mainHandler.post(() -> callback.exito("Contactos sincronizados"));
                    });
                })
                .addOnFailureListener(e -> callback.error("Error sincronización: " + e.getMessage()));
    }

    public void insertar(Contacto contacto, SimpleCallback callback) {
        executor.execute(() -> {
            daoContacto.insertar(contacto);
            mainHandler.post(() -> {
                db.collection("contactos")
                        .document(String.valueOf(contacto.contactoId))
                        .set(contacto)
                        .addOnSuccessListener(aVoid -> callback.exito("Contacto guardado en nube"))
                        .addOnFailureListener(e -> callback.error("Guardado localmente. Error nube: " + e.getMessage()));
            });
        });
    }

    public void actualizar(Contacto contacto, SimpleCallback callback) {
        executor.execute(() -> {
            daoContacto.actualizar(contacto);
            mainHandler.post(() -> {
                db.collection("contactos")
                        .document(String.valueOf(contacto.contactoId))
                        .set(contacto)
                        .addOnSuccessListener(aVoid -> callback.exito("Contacto actualizado"))
                        .addOnFailureListener(e -> callback.error("Error actualización nube: " + e.getMessage()));
            });
        });
    }

    public void eliminar(Contacto contacto, SimpleCallback callback) {
        executor.execute(() -> {
            daoContacto.eliminar(contacto);
            mainHandler.post(() -> {
                db.collection("contactos")
                        .document(String.valueOf(contacto.contactoId))
                        .delete()
                        .addOnSuccessListener(aVoid -> callback.exito("Contacto eliminado"))
                        .addOnFailureListener(e -> callback.error("Error eliminación nube: " + e.getMessage()));
            });
        });
    }

    public List<Contacto> obtenerContactosLocal(String usuarioId) {
        return daoContacto.obtenerPorUsuario(usuarioId);
    }
}