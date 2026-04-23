package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.dao.DaoUsuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Dispositivo;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.AuthCallback;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SincronizacionCompletaCallback;

public class UsuarioRepositorio {
    private static final String TAG = "UsuarioRepositorio";
    private final BaseDeDatosApp roomDb;
    private final DaoUsuario daoUsuario;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public UsuarioRepositorio(Context context) {
        this.roomDb = BaseDeDatosApp.obtenerInstancia(context);
        this.daoUsuario = roomDb.daoUsuario();
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sincronizarDatosCompletos(String uid, SincronizacionCompletaCallback callback) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Usuario usuario = doc.toObject(Usuario.class);
                    if (usuario != null) {
                        executor.execute(() -> {
                            daoUsuario.insertar(usuario);
                            descargarComunidadesYMembresias(uid, callback);
                        });
                    } else {
                        if (callback != null) mainHandler.post(() -> callback.onSincronizacionError("Perfil no encontrado."));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) mainHandler.post(() -> callback.onSincronizacionError(e.getMessage()));
                });
    }

    private void descargarComunidadesYMembresias(String uid, SincronizacionCompletaCallback callback) {
        db.collection("membresias").whereEqualTo("usuarioId", uid).get()
                .addOnSuccessListener(snaps -> {
                    if (snaps == null || snaps.isEmpty()) {
                        if (callback != null) mainHandler.post(() -> callback.onSincronizacionExitosa(false));
                        return;
                    }

                    List<Membresia> listaMems = new ArrayList<>();
                    List<String> idsGrupo = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snaps) {
                        Membresia m = doc.toObject(Membresia.class);
                        if (m != null) {
                            listaMems.add(m);
                            idsGrupo.add(m.comunidadId);
                        }
                    }

                    if (idsGrupo.isEmpty()) {
                        if (callback != null) mainHandler.post(() -> callback.onSincronizacionExitosa(false));
                        return;
                    }

                    db.collection("comunidades").whereIn("comunidadId", idsGrupo).get()
                            .addOnSuccessListener(comSnaps -> {
                                executor.execute(() -> {
                                    try {
                                        for (QueryDocumentSnapshot cDoc : comSnaps) {
                                            Comunidad c = cDoc.toObject(Comunidad.class);
                                            if (c != null) {
                                                if (roomDb.daoUsuario().obtenerPorId(c.usuarioCreadorId) == null) {
                                                    DocumentSnapshot creatorDoc = Tasks.await(db.collection("usuarios").document(c.usuarioCreadorId).get());
                                                    Usuario creator = creatorDoc.toObject(Usuario.class);
                                                    if (creator != null) roomDb.daoUsuario().insertar(creator);
                                                }
                                                roomDb.daoComunidad().insertar(c);
                                            }
                                        }
                                        for (Membresia m : listaMems) {
                                            roomDb.daoMembresia().insertar(m);
                                        }
                                        if (callback != null) mainHandler.post(() -> callback.onSincronizacionExitosa(true));
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error en sync: " + e.getMessage());
                                        if (callback != null) mainHandler.post(() -> callback.onSincronizacionError(e.getMessage()));
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) mainHandler.post(() -> callback.onSincronizacionError(e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) mainHandler.post(() -> callback.onSincronizacionError(e.getMessage()));
                });
    }

    public void iniciarSesion(String correo, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();
                    db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                Usuario u = doc.toObject(Usuario.class);
                                if (u != null) {
                                    u.password = HashContra.hashear(password);
                                    sincronizarDatosCompletos(uid, new SincronizacionCompletaCallback() {
                                        @Override
                                        public void onSincronizacionExitosa(boolean tieneComunidad) {
                                            generarTokenDispositivo(u, callback);
                                        }

                                        @Override
                                        public void onSincronizacionError(String mensaje) {
                                            generarTokenDispositivo(u, callback);
                                        }
                                    });
                                } else {
                                    mainHandler.post(() -> callback.error("Perfil no encontrado."));
                                }
                            });
                })
                .addOnFailureListener(e -> intentarLoginLocal(correo, password, callback));
    }

    private void generarTokenDispositivo(Usuario usuario, AuthCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Dispositivo disp = new Dispositivo();
                    disp.dispositivoId = UUID.randomUUID().toString();
                    disp.usuarioId = usuario.usuarioId;
                    disp.tokenFcm = token;
                    disp.ultimaActividad = System.currentTimeMillis();
                    disp.sincronizado = 1;

                    db.collection("dispositivos").document(disp.dispositivoId).set(disp)
                            .addOnSuccessListener(v -> {
                                executor.execute(() -> {
                                    daoUsuario.insertar(usuario);
                                    roomDb.daoDispositivo().insertar(disp);
                                    mainHandler.post(() -> callback.exito(usuario));
                                });
                            })
                            .addOnFailureListener(e -> {
                                executor.execute(() -> {
                                    daoUsuario.insertar(usuario);
                                    mainHandler.post(() -> callback.exito(usuario));
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    executor.execute(() -> {
                        daoUsuario.insertar(usuario);
                        mainHandler.post(() -> callback.exito(usuario));
                    });
                });
    }

    private void intentarLoginLocal(String correo, String password, AuthCallback callback) {
        executor.execute(() -> {
            Usuario uLocal = daoUsuario.obtenerPorCorreo(correo);
            mainHandler.post(() -> {
                if (uLocal != null && HashContra.verificar(password, uLocal.password)) {
                    callback.exito(uLocal);
                } else {
                    callback.error("Credenciales incorrectas offline.");
                }
            });
        });
    }

    public void registrarUsuario(Usuario usuario, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(usuario.correo, password)
                .addOnSuccessListener(res -> {
                    usuario.usuarioId = res.getUser().getUid();
                    usuario.password = HashContra.hashear(password);
                    usuario.fechaCreacion = System.currentTimeMillis();
                    usuario.sincronizado = 1;
                    guardarPerfilNube(usuario, callback);
                })
                .addOnFailureListener(e -> mainHandler.post(() -> callback.error(e.getMessage())));
    }

    private void guardarPerfilNube(Usuario usuario, AuthCallback callback) {
        db.collection("usuarios").document(usuario.usuarioId).set(usuario)
                .addOnSuccessListener(v -> generarTokenDispositivo(usuario, callback))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.error("Error al guardar perfil: " + e.getMessage())));
    }

    public void verificarEstadoComunidad(String usuarioId, SimpleCallback callback) {
        executor.execute(() -> {
            List<Membresia> mems = roomDb.daoMembresia().obtenerPorUsuario(usuarioId);
            if (mems != null && !mems.isEmpty()) {
                mainHandler.post(() -> callback.exito("CON_COMUNIDAD"));
                return;
            }
            sincronizarDatosCompletos(usuarioId, new SincronizacionCompletaCallback() {
                @Override
                public void onSincronizacionExitosa(boolean tieneComunidad) {
                    callback.exito(tieneComunidad ? "CON_COMUNIDAD" : "SIN_COMUNIDAD");
                }

                @Override
                public void onSincronizacionError(String mensaje) {
                    callback.error(mensaje);
                }
            });
        });
    }

    public boolean estaLogueado() { return mAuth.getCurrentUser() != null; }

    public void cerrarSesion(SimpleCallback callback) {
        executor.execute(() -> {
            roomDb.clearAllTables();
            mainHandler.post(() -> {
                mAuth.signOut();
                callback.exito("Cerrado");
            });
        });
    }

    public void recuperarClave(String correo, SimpleCallback callback) {
        mAuth.sendPasswordResetEmail(correo)
                .addOnSuccessListener(r -> mainHandler.post(() -> callback.exito("Revisa tu correo electrónico")))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.error(e.getMessage())));
    }
}
