package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;

public class ActivityPanico extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ActivityPanico";
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private double latitudActual = 0.0;
    private double longitudActual = 0.0;
    private String direccionActual = "Obteniendo ubicación...";
    private String nombreUsuarioActual = "Usuario";

    private final Handler handlerPanico = new Handler(Looper.getMainLooper());
    private Runnable runnablePanico;
    private boolean isBotonPresionado = false;

    private TextView tvUbicacionActualPanico;
    private DrawerLayout drawerLayout;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false))) {
                    obtenerUbicacionActual();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panico);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvUbicacionActualPanico = findViewById(R.id.tvUbicacionActualPanico);
        drawerLayout = findViewById(R.id.drawer_layout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        inicializarComponentesUI();
        verificarPermisosUbicacion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarEstadoCampanita();
        cargarDatosUsuarioDesdeDB();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (latitudActual != 0.0) {
            actualizarPosicionEnMapa(latitudActual, longitudActual);
        }
    }

    private void actualizarPosicionEnMapa(double lat, double lng) {
        if (googleMap != null) {
            LatLng miPosicion = new LatLng(lat, lng);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(miPosicion).title("Estás aquí"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 16f));
        }
    }

    private void verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitudActual = location.getLatitude();
                longitudActual = location.getLongitude();
                traducirCoordenadasADireccion(latitudActual, longitudActual);
            }
        });
    }

    private void traducirCoordenadasADireccion(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    direccionActual = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> {
                        if (tvUbicacionActualPanico != null) tvUbicacionActualPanico.setText(direccionActual);
                        actualizarPosicionEnMapa(lat, lng);
                    });
                }
            } catch (IOException e) { Log.e(TAG, "Error de Geocodificación", e); }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configurarBotonSOS() {
        View btnSOS = findViewById(R.id.btnSOS);
        runnablePanico = () -> { if (isBotonPresionado) enviarAlertaDePanico(); };

        if (btnSOS != null) {
            btnSOS.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isBotonPresionado = true;
                    handlerPanico.postDelayed(runnablePanico, 5000);
                    Toast.makeText(this, "Mantén presionado 5 segundos...", Toast.LENGTH_SHORT).show();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    isBotonPresionado = false;
                    handlerPanico.removeCallbacks(runnablePanico);
                }
                return true;
            });
        }
    }

    private void enviarAlertaDePanico() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        guardarEstadoAlerta(true);
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BaseDeDatosApp localDb = BaseDeDatosApp.obtenerInstancia(this);
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                List<Membresia> membresias = localDb.daoMembresia().obtenerPorUsuario(uid);
                String comunidadId = (membresias != null && !membresias.isEmpty()) ? membresias.get(0).comunidadId : null;

                if (comunidadId == null) {
                    runOnUiThread(() -> Toast.makeText(this, "No perteneces a ninguna comunidad", Toast.LENGTH_SHORT).show());
                    return;
                }

                if (localDb.daoComunidad().obtenerPorId(comunidadId) == null) {
                    DocumentSnapshot comDoc = Tasks.await(firestore.collection("comunidades").document(comunidadId).get());
                    Comunidad c = comDoc.toObject(Comunidad.class);
                    if (c != null) {
                        if (localDb.daoUsuario().obtenerPorId(c.usuarioCreadorId) == null) {
                            DocumentSnapshot creatorDoc = Tasks.await(firestore.collection("usuarios").document(c.usuarioCreadorId).get());
                            Usuario creator = creatorDoc.toObject(Usuario.class);
                            if (creator != null) localDb.daoUsuario().insertar(creator);
                        }
                        localDb.daoComunidad().insertar(c);
                    }
                }

                Alerta alerta = new Alerta();
                alerta.alertaId = UUID.randomUUID().toString();
                alerta.usuarioId = uid;
                alerta.comunidadId = comunidadId;
                alerta.latitud = latitudActual;
                alerta.longitud = longitudActual;
                alerta.ubicacion = direccionActual;
                alerta.estado = "activa";
                alerta.fechaCreacion = System.currentTimeMillis();
                alerta.fechaActualizacion = alerta.fechaCreacion;
                alerta.sincronizado = 0;

                Publicacion postAlerta = new Publicacion();
                postAlerta.publicacionId = UUID.randomUUID().toString();
                postAlerta.usuarioId = uid;
                postAlerta.comunidadId = comunidadId;
                postAlerta.categoria = "¡EMERGENCIA!";
                postAlerta.contenido = nombreUsuarioActual + " necesita ayuda en: " + direccionActual;
                postAlerta.latitud = latitudActual;
                postAlerta.longitud = longitudActual;
                postAlerta.direccion = direccionActual;
                postAlerta.fechaCreacion = alerta.fechaCreacion;
                postAlerta.sincronizado = 0;

                localDb.daoAlerta().insertar(alerta);
                localDb.daoPublicacion().insertar(postAlerta);

                firestore.collection("alertas").document(alerta.alertaId).set(alerta);
                firestore.collection("publicaciones").document(postAlerta.publicacionId).set(postAlerta);

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ActivityAlertaActiva.class);
                    intent.putExtra("ES_EMISOR", true);
                    intent.putExtra("DIRECCION", direccionActual);
                    intent.putExtra("NOMBRE_USUARIO", nombreUsuarioActual);
                    intent.putExtra("LATITUD", latitudActual);
                    intent.putExtra("LONGITUD", longitudActual);
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error en enviarAlertaDePanico", e);
                runOnUiThread(() -> Toast.makeText(this, "Error al activar alerta", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void inicializarComponentesUI() {
        configurarMenuLateral();
        configurarMenuInferior();
        configurarBotonSOS();
    }

    private void configurarMenuLateral() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        NavigationView navView = findViewById(R.id.nav_view);

        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_comunidad) navegarHacia(ActivityPerfilUsuario.class, false);
                else if (id == R.id.nav_historial) navegarHacia(ActivityHistorialAlertas.class, false);
                else if (id == R.id.nav_contactos) navegarHacia(ActivityContactosEmergencia.class, false);
                else if (id == R.id.nav_logout) mostrarDialogoCerrarSesion();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_cerrar_sesion, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        view.findViewById(R.id.btnConfirmarCerrar).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, ActivityIniciarSesion.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });
        view.findViewById(R.id.btnCancelarCerrar).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarMenuInferior() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_alerta_panico);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) navegarHacia(ActivityMuroSeguridad.class, true);
                else if (id == R.id.nav_profile) navegarHacia(ActivityPerfilUsuario.class, true);
                return true;
            });
        }
    }

    private void navegarHacia(Class<?> destino, boolean limpiarPila) {
        Intent intent = new Intent(this, destino);
        if (limpiarPila) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (limpiarPila) finish();
    }

    private void cargarDatosUsuarioDesdeDB() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            var user = BaseDeDatosApp.obtenerInstancia(this).daoUsuario().obtenerPorId(uid);
            if (user != null) {
                nombreUsuarioActual = user.nombres + " " + (user.apellidos != null ? user.apellidos : "");
                runOnUiThread(() -> {
                    NavigationView navView = findViewById(R.id.nav_view);
                    if (navView != null) {
                        View header = navView.getHeaderView(0);
                        TextView tvNombre = header.findViewById(R.id.tvNombreUsuarioMenu);
                        if (tvNombre != null) tvNombre.setText(nombreUsuarioActual);
                    }
                });
            }
        });
    }

    private void guardarEstadoAlerta(boolean activa) {
        getSharedPreferences("SafeApp", MODE_PRIVATE).edit().putBoolean("ALERTA_ACTIVA", activa).apply();
    }

    private void actualizarEstadoCampanita() {
        ImageButton btnNotif = findViewById(R.id.btnNotificaciones);
        boolean activa = getSharedPreferences("SafeApp", MODE_PRIVATE).getBoolean("ALERTA_ACTIVA", false);
        if (btnNotif == null) return;
        if (activa) {
            btnNotif.setColorFilter(ContextCompat.getColor(this, R.color.red_sos));
            btnNotif.setOnClickListener(v -> mostrarDialogoCancelacion());
        } else {
            btnNotif.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            btnNotif.setOnClickListener(v -> navegarHacia(ActivityHistorialAlertas.class, false));
        }
    }

    private void mostrarDialogoCancelacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Desactivar alerta?")
                .setMessage("Se notificará a tus contactos que estás a salvo.")
                .setPositiveButton("Sí, desactivar", (d, w) -> {
                    desactivarAlertasEnNube();
                    guardarEstadoAlerta(false);
                    actualizarEstadoCampanita();
                    Toast.makeText(this, "Alerta desactivada", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancelar", null).show();
    }

    private void desactivarAlertasEnNube() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("alertas")
                .whereEqualTo("usuarioId", uid)
                .whereEqualTo("estado", "activa")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (DocumentSnapshot doc : snaps) {
                        doc.getReference().update("estado", "resuelta", "fechaResolucion", System.currentTimeMillis());
                    }
                });
    }
}