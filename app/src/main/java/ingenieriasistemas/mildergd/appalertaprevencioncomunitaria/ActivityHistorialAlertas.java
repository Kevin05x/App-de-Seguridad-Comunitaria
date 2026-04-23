package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI.AlertaHistorialAdapter;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Alerta;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityHistorialAlertas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AlertaHistorialAdapter adapter;
    private List<Alerta> listaAlertas = new ArrayList<>();

    private BaseDeDatosApp database;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private UsuarioRepositorio usuarioRepositorio;

    private TextView txtSectionHoy;
    private DrawerLayout drawerLayout;
    private String usuarioIdActual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_alertas);
        configurarStatusBar();

        database = BaseDeDatosApp.obtenerInstancia(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        usuarioRepositorio = new UsuarioRepositorio(this);

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) {
            finish();
            return;
        }

        usuarioIdActual = usuario.getUid();
        vincularVistas();
        configurarMenuLateral();
        configurarMenuInferior();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlertaHistorialAdapter(this, listaAlertas);
        recyclerView.setAdapter(adapter);

        cargarDesdeRoom();
        sincronizarDesdeFirebase();
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg_dark));
        }
    }

    private void vincularVistas() {
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.rv_alertas_historial_alertas);
        txtSectionHoy = findViewById(R.id.txt_section_hoy_historial_alertas);
    }

    private void configurarMenuLateral() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        NavigationView navView = findViewById(R.id.nav_view);

        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_comunidad) navegar(ActivityMuroSeguridad.class, true);
                else if (id == R.id.nav_historial) { /* Ya estamos aquí */ }
                else if (id == R.id.nav_contactos) navegar(ActivityContactosEmergencia.class, false);
                else if (id == R.id.nav_logout) mostrarDialogoCerrarSesion();
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
            actualizarNombreMenuLateral(navView);
        }
    }

    private void actualizarNombreMenuLateral(NavigationView navView) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            var user = database.daoUsuario().obtenerPorId(usuarioIdActual);
            if (user != null) {
                String nombre = user.nombres + " " + (user.apellidos != null ? user.apellidos : "");
                runOnUiThread(() -> {
                    View header = navView.getHeaderView(0);
                    TextView tvNombre = header.findViewById(R.id.tvNombreUsuarioMenu);
                    if (tvNombre != null) tvNombre.setText(nombre);
                });
            }
        });
    }

    private void mostrarDialogoCerrarSesion() {
        View view = getLayoutInflater().inflate(R.layout.dialog_cerrar_sesion, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnConfirmar = view.findViewById(R.id.btnConfirmarCerrar);
        Button btnCancelar = view.findViewById(R.id.btnCancelarCerrar);

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                dialog.dismiss();
                usuarioRepositorio.cerrarSesion(new SimpleCallback() {
                    @Override
                    public void exito(String msj) {
                        Intent intent = new Intent(ActivityHistorialAlertas.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void error(String msj) {
                        Toast.makeText(ActivityHistorialAlertas.this, msj, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarMenuInferior() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_contactos);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) navegar(ActivityMuroSeguridad.class, true);
                else if (id == R.id.nav_profile) navegar(ActivityPerfilUsuario.class, true);
                return true;
            });
        }
        View fabSos = findViewById(R.id.fab_sos_nav);
        if (fabSos != null) fabSos.setOnClickListener(v -> navegar(ActivityPanico.class, true));
    }

    private void navegar(Class<?> destino, boolean finishCurrent) {
        Intent intent = new Intent(this, destino);
        if (finishCurrent) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (finishCurrent) finish();
    }

    private void sincronizarDesdeFirebase() {
        firestore.collection("alertas")
                .whereEqualTo("usuarioId", usuarioIdActual)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    new Thread(() -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Alerta alerta = doc.toObject(Alerta.class);
                            alerta.alertaId = doc.getId();
                            alerta.sincronizado = 1;
                            database.daoAlerta().insertar(alerta);
                        }
                        runOnUiThread(this::cargarDesdeRoom);
                    }).start();
                });
    }

    private void cargarDesdeRoom() {
        new Thread(() -> {
            List<Alerta> alertas = database.daoAlerta().obtenerPorUsuario(usuarioIdActual);
            runOnUiThread(() -> {
                listaAlertas.clear();
                listaAlertas.addAll(alertas);
                adapter.notifyDataSetChanged();
                if (listaAlertas.isEmpty()) {
                    txtSectionHoy.setText("No hay alertas registradas");
                } else {
                    txtSectionHoy.setText("Historial de alertas");
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDesdeRoom();
    }
}
