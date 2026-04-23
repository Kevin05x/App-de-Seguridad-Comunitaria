package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.ArrayList;
import java.util.List;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.UI.ContactoAdapter;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.ContactoRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.OnContactoClickListener;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityContactosEmergencia extends AppCompatActivity implements OnContactoClickListener {

    private RecyclerView rvContactos;
    private ContactoAdapter adapter;
    private ContactoRepositorio repositorio;
    private UsuarioRepositorio usuarioRepositorio;
    private String usuarioId;
    private List<Contacto> listaContactos = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private String nombreUsuarioActual = "Usuario";

    private EditText etBuscar;
    private TextView tvTitulo;
    private ImageButton btnBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contactos_emergencia);
        configurarStatusBar();

        usuarioId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        repositorio = new ContactoRepositorio(this);
        usuarioRepositorio = new UsuarioRepositorio(this);

        inicializarComponentes();
        
        if (usuarioId != null) {
            sincronizarDesdeNube();
        }
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg_dark));
        }
    }

    private void sincronizarDesdeNube() {
        repositorio.sincronizarContactos(usuarioId, new SimpleCallback() {
            @Override
            public void exito(String mensaje) {
                cargarContactos();
            }

            @Override
            public void error(String mensaje) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuarioDesdeDB();
        cargarContactos();
    }

    private void inicializarComponentes() {
        drawerLayout = findViewById(R.id.drawer_layout);
        rvContactos = findViewById(R.id.rvContactosEmergencia);
        rvContactos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactoAdapter();
        adapter.setListener(this);
        rvContactos.setAdapter(adapter);

        findViewById(R.id.btnAgregarContacto).setOnClickListener(v -> mostrarDialogoEditar(null));
        findViewById(R.id.fab_sos_nav).setOnClickListener(v -> navegar(ActivityPanico.class, true));

        tvTitulo = findViewById(R.id.tvTituloContactos);
        etBuscar = findViewById(R.id.etBuscarContacto);
        btnBuscar = findViewById(R.id.btnBuscarContacto);

        configurarBusqueda();
        configurarMenuLateral();
        configurarMenuInferior();
    }

    private void configurarBusqueda() {
        if (btnBuscar != null && etBuscar != null && tvTitulo != null) {
            btnBuscar.setOnClickListener(v -> {
                if (etBuscar.getVisibility() == View.GONE) {
                    etBuscar.setVisibility(View.VISIBLE);
                    tvTitulo.setVisibility(View.GONE);
                    etBuscar.requestFocus();
                } else {
                    etBuscar.setVisibility(View.GONE);
                    tvTitulo.setVisibility(View.VISIBLE);
                    etBuscar.setText("");
                }
            });

            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filtrar(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void cargarDatosUsuarioDesdeDB() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            var db = ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp.obtenerInstancia(this);
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

    private void cargarContactos() {
        if (usuarioId == null) return;
        new Thread(() -> {
            listaContactos = repositorio.obtenerContactosLocal(usuarioId);
            runOnUiThread(() -> {
                adapter.setContactos(listaContactos);
                if (etBuscar != null && !etBuscar.getText().toString().isEmpty()) {
                    adapter.filtrar(etBuscar.getText().toString());
                }
            });
        }).start();
    }

    private void configurarMenuLateral() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        NavigationView navView = findViewById(R.id.nav_view);

        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_comunidad) navegar(ActivityMuroSeguridad.class, true);
                else if (id == R.id.nav_historial) navegar(ActivityHistorialAlertas.class, false);
                else if (id == R.id.nav_contactos) navegar(ActivityContactosEmergencia.class, false);
                else if (id == R.id.nav_logout) mostrarDialogoCerrarSesion();
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
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
                        Intent intent = new Intent(ActivityContactosEmergencia.this, ActivityIniciarSesion.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void error(String msj) {
                        Toast.makeText(ActivityContactosEmergencia.this, msj, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

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
    }

    private void navegar(Class<?> destino, boolean finishCurrent) {
        Intent intent = new Intent(this, destino);
        if (finishCurrent) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (finishCurrent) finish();
    }

    private void mostrarDialogoEditar(Contacto contactoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_contacto, null);
        builder.setView(view);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etParentesco = view.findViewById(R.id.etParentesco);
        EditText etTelefono = view.findViewById(R.id.etTelefono);

        if (contactoExistente != null) {
            etNombre.setText(contactoExistente.nombre);
            etTelefono.setText(contactoExistente.telefono);
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btnGuardar).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();

            if (nombre.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Nombre y teléfono son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleCallback callback = new SimpleCallback() {
                @Override
                public void exito(String mensaje) {
                    Toast.makeText(ActivityContactosEmergencia.this, mensaje, Toast.LENGTH_SHORT).show();
                    cargarContactos();
                    dialog.dismiss();
                }

                @Override
                public void error(String mensaje) {
                    Toast.makeText(ActivityContactosEmergencia.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            };

            if (contactoExistente == null) {
                Contacto nuevo = new Contacto();
                nuevo.usuarioId = usuarioId;
                nuevo.nombre = nombre;
                nuevo.telefono = telefono;
                nuevo.fechaCreacion = System.currentTimeMillis();
                repositorio.insertar(nuevo, callback);
            } else {
                contactoExistente.nombre = nombre;
                contactoExistente.telefono = telefono;
                contactoExistente.fechaActualizacion = System.currentTimeMillis();
                repositorio.actualizar(contactoExistente, callback);
            }
        });

        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        if (view.findViewById(R.id.btnCerrarDialog) != null) {
            view.findViewById(R.id.btnCerrarDialog).setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    @Override
    public void onEditarClick(Contacto contacto) {
        mostrarDialogoEditar(contacto);
    }

    @Override
    public void onEliminarClick(Contacto contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_eliminar_contacto, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btnConfirmarEliminar).setOnClickListener(v -> {
            repositorio.eliminar(contacto, new SimpleCallback() {
                @Override
                public void exito(String mensaje) {
                    Toast.makeText(ActivityContactosEmergencia.this, mensaje, Toast.LENGTH_SHORT).show();
                    cargarContactos();
                    dialog.dismiss();
                }

                @Override
                public void error(String mensaje) {
                    Toast.makeText(ActivityContactosEmergencia.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            });
        });

        view.findViewById(R.id.btnCancelarEliminar).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
