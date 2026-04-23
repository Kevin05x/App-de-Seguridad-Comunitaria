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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.Executors;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI.AdapterComunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityPerfilUsuario extends AppCompatActivity {
    private TextView nombreUsuario, numeroUsuario, txtMiComunidad;
    private Chip chipVerificado;
    private MaterialCardView cardSalir, cardCrearComunidad;
    private RecyclerView rvComunidades;
    private BaseDeDatosApp baseDeDatos;
    private UsuarioRepositorio usuarioRepo;
    private DrawerLayout drawerLayout;
    private String nombreUsuarioActual = "Usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);
        configurarStatusBar();

        baseDeDatos = BaseDeDatosApp.obtenerInstancia(this);
        usuarioRepo = new UsuarioRepositorio(this);

        vincularVistas();
        cargarDatosUsuarioLocal();
        cargarComunidadesLocal();
        configurarMenuLateral();
        configurarMenuInferior();

        cardSalir.setOnClickListener(v -> mostrarDialogoCerrarSesion());
        cardCrearComunidad.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivitySeleccionComunidad.class);
            startActivity(intent);
        });
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
        nombreUsuario = findViewById(R.id.nombreUsuario);
        numeroUsuario = findViewById(R.id.numeroUsuario);
        chipVerificado = findViewById(R.id.verificado);
        cardSalir = findViewById(R.id.cardSalir);
        cardCrearComunidad = findViewById(R.id.cardCrearComunidad);
        rvComunidades = findViewById(R.id.rvComunidades);
        txtMiComunidad = findViewById(R.id.txtMiComunidad);
    }

    private void configurarMenuLateral() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        NavigationView navView = findViewById(R.id.nav_view);

        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });

        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_comunidad) {
                } else if (id == R.id.nav_historial) {
                    navegar(ActivityHistorialAlertas.class, false);
                } else if (id == R.id.nav_contactos) {
                    navegar(ActivityContactosEmergencia.class, false);
                } else if (id == R.id.nav_logout) {
                    mostrarDialogoCerrarSesion();
                }
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void configurarMenuInferior() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_contactos);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    navegar(ActivityMuroSeguridad.class, true);
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }

        View fabSos = findViewById(R.id.fab_sos_nav);
        if (fabSos != null) {
            fabSos.setOnClickListener(v -> navegar(ActivityPanico.class, true));
        }
    }

    private void navegar(Class<?> destino, boolean finishCurrent) {
        if (this.getClass().equals(destino)) return;
        Intent intent = new Intent(this, destino);
        if (finishCurrent) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (finishCurrent) finish();
    }

    private void cargarDatosUsuarioLocal() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            Usuario usuario = baseDeDatos.daoUsuario().obtenerPorId(uid);
            runOnUiThread(() -> {
                if (usuario != null) {
                    nombreUsuarioActual = usuario.nombres + " " + (usuario.apellidos != null ? usuario.apellidos : "");
                    nombreUsuario.setText(nombreUsuarioActual);
                    numeroUsuario.setText(usuario.correo);
                    actualizarUInombreMenu(nombreUsuarioActual);
                } else {
                    nombreUsuario.setText("Usuario no encontrado");
                }
            });
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

    private void cargarComunidadesLocal() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Comunidad> comunidades = baseDeDatos.daoComunidad().obtenerComunidadesDelUsuario(uid);
            runOnUiThread(() -> {
                if (comunidades == null || comunidades.isEmpty()) {
                    rvComunidades.setVisibility(View.GONE);
                    txtMiComunidad.setText("Aún no pertenece a una comunidad");
                    cardCrearComunidad.setVisibility(View.VISIBLE);
                } else {
                    rvComunidades.setVisibility(View.VISIBLE);
                    cardCrearComunidad.setVisibility(View.GONE);
                    rvComunidades.setLayoutManager(new LinearLayoutManager(this));
                    rvComunidades.setAdapter(new AdapterComunidad(this, comunidades));
                }
            });
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
                usuarioRepo.cerrarSesion(new SimpleCallback() {
                    @Override
                    public void exito(String msj) {
                        Intent intent = new Intent(ActivityPerfilUsuario.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void error(String msj) {
                        Toast.makeText(ActivityPerfilUsuario.this, msj, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}