package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.MultimediaPublicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.dto.PublicacionDTO;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.CallbackComunidades;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.CallbackListadoPublicaciones;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class PublicacionRepositorio {
    private static final String TAG = "PublicacionRepositorio";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final BaseDeDatosApp roomDb;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public PublicacionRepositorio(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.roomDb = BaseDeDatosApp.obtenerInstancia(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void obtenerMisComunidades(String usuarioId, CallbackComunidades callback) {
        db.collection("membresias").whereEqualTo("usuarioId", usuarioId).get()
                .addOnSuccessListener(snaps -> {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snaps) {
                        String cid = doc.getString("comunidadId");
                        if (cid != null) ids.add(cid);
                    }
                    callback.exito(ids);
                })
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    public void cargarFeed(List<String> comunidadesIds, CallbackListadoPublicaciones callback) {
        if (comunidadesIds == null || comunidadesIds.isEmpty()) {
            cargarDeLocal(null, callback);
            return;
        }

        db.collection("publicaciones")
                .whereIn("comunidadId", comunidadesIds)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error en tiempo real, cargando local: ", e);
                        cargarDeLocal(comunidadesIds, callback);
                        return;
                    }
                    if (snapshots != null) {
                        procesarSnapshots(snapshots, callback);
                    }
                });
    }

    private void procesarSnapshots(Iterable<QueryDocumentSnapshot> snapshots, CallbackListadoPublicaciones callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        executor.execute(() -> {
            List<Publicacion> pubsNube = new ArrayList<>();
            Set<String> usuarioIds = new HashSet<>();
            Set<String> comunidadIds = new HashSet<>();
            
            for (QueryDocumentSnapshot doc : snapshots) {
                Publicacion p = doc.toObject(Publicacion.class);
                if (p != null) {
                    pubsNube.add(p);
                    usuarioIds.add(p.usuarioId);
                    comunidadIds.add(p.comunidadId);
                }
            }

            for (String cid : comunidadIds) {
                if (roomDb.daoComunidad().obtenerPorId(cid) == null) {
                    try {
                        DocumentSnapshot comDoc = Tasks.await(db.collection("comunidades").document(cid).get());
                        Comunidad c = comDoc.toObject(Comunidad.class);
                        if (c != null) {
                            if (roomDb.daoUsuario().obtenerPorId(c.usuarioCreadorId) == null) {
                                DocumentSnapshot creatorDoc = Tasks.await(db.collection("usuarios").document(c.usuarioCreadorId).get());
                                Usuario creator = creatorDoc.toObject(Usuario.class);
                                if (creator != null) roomDb.daoUsuario().insertar(creator);
                            }
                            roomDb.daoComunidad().insertar(c);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error sync comunidad: " + cid, e);
                    }
                }
            }

            for (String autorId : usuarioIds) {
                if (roomDb.daoUsuario().obtenerPorId(autorId) == null) {
                    try {
                        DocumentSnapshot userDoc = Tasks.await(db.collection("usuarios").document(autorId).get());
                        Usuario u = userDoc.toObject(Usuario.class);
                        if (u != null) roomDb.daoUsuario().insertar(u);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sync autor: " + autorId, e);
                    }
                }
            }

            List<PublicacionDTO> listaDTOs = new ArrayList<>();
            List<String> idsPublicaciones = new ArrayList<>();

            for (Publicacion p : pubsNube) {
                p.sincronizado = 1;
                roomDb.daoPublicacion().insertar(p);
                idsPublicaciones.add(p.publicacionId);

                MultimediaPublicacion media = roomDb.daoMultimediaPublicacion().obtenerPorPublicacionId(p.publicacionId);
                Usuario autor = roomDb.daoUsuario().obtenerPorId(p.usuarioId);

                PublicacionDTO dto = new PublicacionDTO(p, media, autor != null ? autor.nombres : "Vecino");
                listaDTOs.add(dto);
            }

            if (uid != null && !idsPublicaciones.isEmpty()) {
                db.collection("utiles_usuarios")
                        .whereEqualTo("usuarioId", uid)
                        .whereIn("publicacionId", idsPublicaciones)
                        .get()
                        .addOnSuccessListener(snaps -> {
                            Map<String, Boolean> utilesMap = new HashMap<>();
                            for (QueryDocumentSnapshot d : snaps) {
                                utilesMap.put(d.getString("publicacionId"), true);
                            }
                            for (PublicacionDTO dto : listaDTOs) {
                                if (utilesMap.containsKey(dto.publicacion.publicacionId)) {
                                    dto.esUtilParaUsuarioActual = true;
                                }
                            }
                            mainHandler.post(() -> callback.exito(listaDTOs));
                        })
                        .addOnFailureListener(e -> mainHandler.post(() -> callback.exito(listaDTOs)));
            } else {
                mainHandler.post(() -> callback.exito(listaDTOs));
            }
        });
    }

    private void cargarDeLocal(List<String> comunidadesIds, CallbackListadoPublicaciones callback) {
        executor.execute(() -> {
            List<Publicacion> locales;
            if (comunidadesIds == null || comunidadesIds.isEmpty()) {
                locales = roomDb.daoPublicacion().obtenerMuroComunidad("", 50);
            } else {
                locales = roomDb.daoPublicacion().obtenerMuroComunidad(comunidadesIds.get(0), 50);
            }
            
            List<PublicacionDTO> dtos = new ArrayList<>();
            for (Publicacion p : locales) {
                MultimediaPublicacion media = roomDb.daoMultimediaPublicacion().obtenerPorPublicacionId(p.publicacionId);
                Usuario autor = roomDb.daoUsuario().obtenerPorId(p.usuarioId);
                dtos.add(new PublicacionDTO(p, media, autor != null ? autor.nombres : "Offline"));
            }
            mainHandler.post(() -> callback.exito(dtos));
        });
    }

    public void crearPostOfflineFirst(Publicacion pub, Uri imagenUri, SimpleCallback callback) {
        pub.sincronizado = 0;

        Alerta alertaHistorial = new Alerta();
        alertaHistorial.alertaId = pub.publicacionId;
        alertaHistorial.usuarioId = pub.usuarioId;
        alertaHistorial.comunidadId = pub.comunidadId;
        alertaHistorial.latitud = pub.latitud;
        alertaHistorial.longitud = pub.longitud;
        alertaHistorial.ubicacion = pub.direccion;
        alertaHistorial.estado = "reporte";
        alertaHistorial.fechaCreacion = pub.fechaCreacion;
        alertaHistorial.sincronizado = 0;

        executor.execute(() -> {
            roomDb.daoPublicacion().insertar(pub);
            roomDb.daoAlerta().insertar(alertaHistorial);
        });

        if (imagenUri != null) {
            MultimediaPublicacion media = new MultimediaPublicacion();
            media.multimediaId = UUID.randomUUID().toString();
            media.publicacionId = pub.publicacionId;
            media.rutaLocal = imagenUri.toString();
            media.sincronizado = 0;
            media.fechaCreacion = pub.fechaCreacion;
            executor.execute(() -> roomDb.daoMultimediaPublicacion().insertar(media));

            subirImagenYPublicar(pub, media, alertaHistorial, callback);
        } else {
            publicarEnFirestore(pub, null, alertaHistorial, callback);
        }
    }

    private void subirImagenYPublicar(Publicacion pub, MultimediaPublicacion media, Alerta alerta, SimpleCallback callback) {
        String path = "publicaciones/" + pub.publicacionId + "/" + media.multimediaId + ".jpg";
        StorageReference ref = storage.getReference().child(path);

        ref.putFile(Uri.parse(media.rutaLocal))
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
                    String downloadUrl = url.toString();
                    media.url = downloadUrl;
                    media.rutaStorage = path;
                    pub.urlImagen = downloadUrl; 
                    publicarEnFirestore(pub, media, alerta, callback);
                }))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.error("Error al subir imagen: " + e.getMessage())));
    }

    private void publicarEnFirestore(Publicacion pub, @Nullable MultimediaPublicacion media, Alerta alerta, SimpleCallback callback) {
        db.collection("publicaciones").document(pub.publicacionId).set(pub)
                .addOnSuccessListener(v -> {
                    pub.sincronizado = 1;
                    alerta.sincronizado = 1;
                    db.collection("alertas").document(alerta.alertaId).set(alerta);

                    executor.execute(() -> {
                        roomDb.daoPublicacion().insertar(pub);
                        roomDb.daoAlerta().insertar(alerta);
                    });

                    if (media != null) {
                        db.collection("multimedia_publicacion").document(media.multimediaId).set(media)
                            .addOnSuccessListener(v2 -> {
                                media.sincronizado = 1;
                                executor.execute(() -> roomDb.daoMultimediaPublicacion().insertar(media));
                                mainHandler.post(() -> callback.exito("¡Publicado!"));
                            })
                            .addOnFailureListener(e -> mainHandler.post(() -> callback.error("Post guardado, media falló.")));
                    } else {
                        mainHandler.post(() -> callback.exito("¡Publicado!"));
                    }
                })
                .addOnFailureListener(e -> mainHandler.post(() -> callback.error("Error al publicar.")));
    }

    public void marcarUtil(String pubId, SimpleCallback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.error("Sesión no válida");
            return;
        }

        DocumentReference utilRef = db.collection("utiles_usuarios").document(pubId + "_" + uid);
        DocumentReference pubRef = db.collection("publicaciones").document(pubId);

        db.runTransaction(transaction -> {
            if (transaction.get(utilRef).exists()) {
                throw new RuntimeException("Ya marcaste esta publicación como útil");
            }

            Map<String, Object> utilData = new HashMap<>();
            utilData.put("usuarioId", uid);
            utilData.put("publicacionId", pubId);
            utilData.put("fecha", FieldValue.serverTimestamp());
            
            transaction.set(utilRef, utilData);
            transaction.update(pubRef, "contadorUtil", FieldValue.increment(1));

            return null;
        }).addOnSuccessListener(result -> {
            mainHandler.post(() -> callback.exito("¡Gracias por tu aporte!"));
        }).addOnFailureListener(e -> {
            mainHandler.post(() -> callback.error(e.getMessage()));
        });
    }
}
