package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI.PublicacionAdapter;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.dto.PublicacionDTO;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.PublicacionRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.CallbackComunidades;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.CallbackListadoPublicaciones;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.OnPublicacionClickListener;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityMuroSeguridad extends AppCompatActivity implements OnPublicacionClickListener {
    private RecyclerView rvFeed;
    private PublicacionAdapter adapter;
    private PublicacionRepositorio repositorio;
    private TextInputEditText etPromptReporte;
    private Button btnPublicar;
    private ImageView btnAbrirCamara, btnAdjuntarUbicacion;
    private ImageView imgVistaPrevia;
    private TextView tvUbicacionAdjunta;
    private DrawerLayout drawerLayout;
    private String nombreUsuarioActual = "Usuario";

    private Uri imagenUri = null;
    private double latitudAdjunta = 0, longitudAdjunta = 0;
    private String direccionAdjunta = null;
    private List<String> misComunidades = new ArrayList<>();
    
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<Intent> picker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                MainActivity.isExternalIntentActive = false;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUri = result.getData().getData();
                    mostrarPreview(true);
                }
            }
    );

    private final ActivityResultLauncher<String> requestLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) obtenerUbicacionActual();
                else Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_muro_seguridad);
        configurarStatusBar();

        repositorio = new PublicacionRepositorio(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        vincularVistas();
        configurarRecycler();
        configurarInteracciones();
        configurarMenuLateral();
        configurarMenuInferior();

        if (savedInstanceState != null) {
            imagenUri = savedInstanceState.getParcelable("imagenUri");
            latitudAdjunta = savedInstanceState.getDouble("latitudAdjunta");
            longitudAdjunta = savedInstanceState.getDouble("longitudAdjunta");
            direccionAdjunta = savedInstanceState.getString("direccionAdjunta");
            if (imagenUri != null) mostrarPreview(true);
            if (direccionAdjunta != null) {
                tvUbicacionAdjunta.setVisibility(View.VISIBLE);
                tvUbicacionAdjunta.setText("Ubicación: " + direccionAdjunta);
                btnAdjuntarUbicacion.setColorFilter(ContextCompat.getColor(this, R.color.brand_primary));
            }
        }
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg_dark));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imagenUri != null) outState.putParcelable("imagenUri", imagenUri);
        outState.putDouble("latitudAdjunta", latitudAdjunta);
        outState.putDouble("longitudAdjunta", longitudAdjunta);
        outState.putString("direccionAdjunta", direccionAdjunta);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuarioDesdeDB();
        verificarPermisoYComunidades();
    }

    private void vincularVistas() {
        drawerLayout = findViewById(R.id.drawer_layout);
        rvFeed = findViewById(R.id.rvFeedPublicaciones);
        etPromptReporte = findViewById(R.id.etPromptReporte);
        btnPublicar = findViewById(R.id.btnPublicar);
        btnAbrirCamara = findViewById(R.id.btnAbrirCamara);
        btnAdjuntarUbicacion = findViewById(R.id.btnAdjuntarUbicacion);
        imgVistaPrevia = findViewById(R.id.imgVistaPrevia);
        tvUbicacionAdjunta = findViewById(R.id.tvUbicacionAdjunta);
    }

    private void configurarRecycler() {
        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicacionAdapter();
        adapter.setListener(this);
        rvFeed.setAdapter(adapter);
    }

    private void configurarInteracciones() {
        btnAbrirCamara.setOnClickListener(v -> abrirGaleria());
        btnAdjuntarUbicacion.setOnClickListener(v -> solicitarUbicacion());
        btnPublicar.setOnClickListener(v -> lanzarPostOfflineFirst());
        
        View fabSOS = findViewById(R.id.fabSOS);
        if (fabSOS != null) {
            fabSOS.setOnClickListener(v -> navegar(ActivityPanico.class, true));
        }
    }

    private void abrirGaleria() {
        MainActivity.isExternalIntentActive = true;
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        picker.launch(i);
    }

    private void solicitarUbicacion() {
        if (direccionAdjunta != null) {
            limpiarUbicacion();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void limpiarUbicacion() {
        latitudAdjunta = 0;
        longitudAdjunta = 0;
        direccionAdjunta = null;
        tvUbicacionAdjunta.setVisibility(View.GONE);
        btnAdjuntarUbicacion.setColorFilter(ContextCompat.getColor(this, R.color.gris_slate_400));
        Toast.makeText(this, "Ubicación quitada", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitudAdjunta = location.getLatitude();
                longitudAdjunta = location.getLongitude();
                traducirCoordenadasADireccion(latitudAdjunta, longitudAdjunta);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void traducirCoordenadasADireccion(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    direccionAdjunta = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> {
                        tvUbicacionAdjunta.setVisibility(View.VISIBLE);
                        tvUbicacionAdjunta.setText("Ubicación: " + direccionAdjunta);
                        btnAdjuntarUbicacion.setColorFilter(ContextCompat.getColor(this, R.color.brand_primary));
                    });
                }
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void mostrarPreview(boolean mostrar) {
        if (mostrar && imagenUri != null) {
            imgVistaPrevia.setVisibility(View.VISIBLE);
            imgVistaPrevia.setImageURI(imagenUri);
            btnAbrirCamara.setColorFilter(ContextCompat.getColor(this, R.color.brand_primary));
        } else {
            imgVistaPrevia.setVisibility(View.GONE);
            btnAbrirCamara.setColorFilter(ContextCompat.getColor(this, R.color.gris_slate_400));
        }
    }

    private void cargarDatosUsuarioDesdeDB() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            var db = BaseDeDatosApp.obtenerInstancia(this);
            var user = db.daoUsuario().obtenerPorId(uid);
            if (user != null) {
                nombreUsuarioActual = user.nombres + " " + (user.apellidos != null ? user.apellidos : "");
                runOnUiThread(() -> actualizarUInombreMenu(nombreUsuarioActual));
            }
        });
    }

    private void actualizarUInombreMenu(String nombre) {
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView != null) {
            View header = navView.getHeaderView(0);
            TextView tvNombre = header.findViewById(R.id.tvNombreUsuarioMenu);
            if (tvNombre != null) tvNombre.setText(nombre);
        }
    }

    private void configurarMenuLateral() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        NavigationView navView = findViewById(R.id.nav_view);

        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_comunidad) navegar(ActivityPerfilUsuario.class, false);
                else if (id == R.id.nav_historial) navegar(ActivityHistorialAlertas.class, false);
                else if (id == R.id.nav_contactos) navegar(ActivityContactosEmergencia.class, true);
                else if (id == R.id.nav_logout) mostrarDialogoCerrarSesion();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cerrar_sesion, null);
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
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_muro);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {  }
                else if (id == R.id.nav_profile) navegar(ActivityPerfilUsuario.class, true);
                return true;
            });
        }
    }

    private void navegar(Class<?> destino, boolean finishCurrent) {
        Intent intent = new Intent(this, destino);
        if (finishCurrent) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (finishCurrent) finish();
    }

    private void verificarPermisoYComunidades() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null && !MainActivity.isExternalIntentActive) {
            finish();
            return;
        }
        
        if (uid != null) {
            repositorio.obtenerMisComunidades(uid, new CallbackComunidades() {
                @Override
                public void exito(List<String> ids) {
                    misComunidades = ids;
                    if (!misComunidades.isEmpty()) {
                        etPromptReporte.setEnabled(true);
                        btnPublicar.setEnabled(true);
                        btnAbrirCamara.setEnabled(true);
                        btnAdjuntarUbicacion.setEnabled(true);
                        repositorio.cargarFeed(misComunidades, new CallbackListadoPublicaciones() {
                            @Override
                            public void exito(List<PublicacionDTO> pubs) {
                                adapter.setPublicaciones(pubs);
                            }

                            @Override
                            public void error(String msj) {
                                Toast.makeText(ActivityMuroSeguridad.this, msj, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        disablePublishing();
                    }
                }

                @Override
                public void error(String msj) {
                    disablePublishing();
                    Toast.makeText(ActivityMuroSeguridad.this, msj, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void disablePublishing() {
        etPromptReporte.setHint("Únete a un grupo para publicar");
        etPromptReporte.setEnabled(false);
        btnPublicar.setEnabled(false);
        btnAbrirCamara.setEnabled(false);
        btnAdjuntarUbicacion.setEnabled(false);
    }

    private void lanzarPostOfflineFirst() {
        if (misComunidades.isEmpty()) {
            Toast.makeText(this, "No estás en ninguna comunidad", Toast.LENGTH_SHORT).show();
            return;
        }
        String txt = etPromptReporte.getText().toString().trim();
        if (txt.isEmpty() && imagenUri == null) {
            Toast.makeText(this, "Escribe algo o sube una foto", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPublicar.setEnabled(false);
        Publicacion pub = new Publicacion();
        pub.publicacionId = UUID.randomUUID().toString();
        pub.comunidadId = misComunidades.get(0);
        pub.usuarioId = FirebaseAuth.getInstance().getUid();

        pub.categoria = "Aviso"; 
        
        pub.contenido = txt;
        pub.latitud = latitudAdjunta;
        pub.longitud = longitudAdjunta;
        pub.direccion = direccionAdjunta;
        pub.fechaCreacion = System.currentTimeMillis();
        pub.fechaActualizacion = pub.fechaCreacion;

        repositorio.crearPostOfflineFirst(pub, imagenUri, new SimpleCallback() {
            @Override
            public void exito(String msj) {
                Toast.makeText(ActivityMuroSeguridad.this, "¡Publicado!", Toast.LENGTH_SHORT).show();
                limpiarPost();
                verificarPermisoYComunidades();
            }

            @Override
            public void error(String msj) {
                btnPublicar.setEnabled(true);
                Toast.makeText(ActivityMuroSeguridad.this, "Error: " + msj, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limpiarPost() {
        etPromptReporte.setText("");
        imagenUri = null;
        latitudAdjunta = 0;
        longitudAdjunta = 0;
        direccionAdjunta = null;
        tvUbicacionAdjunta.setVisibility(View.GONE);
        mostrarPreview(false);
        btnAdjuntarUbicacion.setColorFilter(ContextCompat.getColor(this, R.color.gris_slate_400));
        btnPublicar.setEnabled(true);
    }

    @Override
    public void onUtilClick(PublicacionDTO p) {
        repositorio.marcarUtil(p.publicacion.publicacionId, new SimpleCallback() {
            @Override
            public void exito(String msj) {
                p.publicacion.contadorUtil += 1;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void error(String msj) {
                Toast.makeText(ActivityMuroSeguridad.this, msj, Toast.LENGTH_SHORT).show();
            }
        });
    }
}