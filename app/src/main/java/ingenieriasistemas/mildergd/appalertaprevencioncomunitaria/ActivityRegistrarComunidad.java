package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.UUID;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Comunidad;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Membresia;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.ComunidadRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityRegistrarComunidad extends AppCompatActivity {
    private TextInputEditText txtNombre, txtCodigo, txtDireccion, txtDescripcion;
    private Button btnCrear;
    private ComunidadRepositorio comunidadRepo;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_comunidad);
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        txtNombre = findViewById(R.id.txtNombre);
        txtCodigo = findViewById(R.id.txtCodigo);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnCrear = findViewById(R.id.btnCrearComunidad);

        comunidadRepo = new ComunidadRepositorio(this);
        auth = FirebaseAuth.getInstance();

        generarCodigoAutomatico();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCrear.setOnClickListener(v -> registrarComunidadEnNube());
    }

    private void registrarComunidadEnNube() {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().toString().trim() : "";
        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().toString().trim() : "";
        String direccion = txtDireccion.getText() != null ? txtDireccion.getText().toString().trim() : "";
        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "Completa Nombre y Dirección", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesión no activa", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCrear.setEnabled(false);
        btnCrear.setText("Creando comunidad...");

        String idUsuario = auth.getCurrentUser().getUid();
        long tiempo = System.currentTimeMillis();

        Comunidad comunidad = new Comunidad();
        comunidad.comunidadId = UUID.randomUUID().toString();
        comunidad.nombre = nombre;
        comunidad.codigoInvitacion = codigo;
        comunidad.direccion = direccion;
        comunidad.descripcion = descripcion;
        comunidad.usuarioCreadorId = idUsuario;
        comunidad.fechaCreacion = tiempo;
        comunidad.fechaActualizacion = tiempo;

        Membresia membresia = new Membresia();
        membresia.comunidadId = comunidad.comunidadId;
        membresia.usuarioId = idUsuario;
        membresia.rol = "admin";
        membresia.fechaIngreso = tiempo;
        membresia.notificacionesActivas = 1;
        membresia.silenciado = 0;

        comunidadRepo.crearComunidad(comunidad, membresia, new SimpleCallback() {
            @Override
            public void exito(String mensaje) {
                Intent intent = new Intent(ActivityRegistrarComunidad.this, ActivityMuroSeguridad.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(String mensaje) {
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Comunidad");
                Toast.makeText(ActivityRegistrarComunidad.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generarCodigoAutomatico() {
        if (txtCodigo == null) return;
        String codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        txtCodigo.setText(codigo);
    }
}