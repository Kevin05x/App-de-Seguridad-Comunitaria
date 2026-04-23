package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.AuthCallback;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SincronizacionCompletaCallback;

public class ActivityIniciarSesion extends AppCompatActivity {
    private TextInputEditText txtCorreo, txtPassword;
    private Button btnIniciarSesion;
    private TextView txtRegistrar, txtOlvidarContrasena;
    private UsuarioRepositorio repositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iniciar_sesion);
        
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        repositorio = new UsuarioRepositorio(this);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        txtRegistrar = findViewById(R.id.txtRegistrar);
        txtOlvidarContrasena = findViewById(R.id.txtOlvidarContraseña);

        if (repositorio.estaLogueado()) {
            iniciarSincronizacionTotal(com.google.firebase.auth.FirebaseAuth.getInstance().getUid());
        }

        btnIniciarSesion.setOnClickListener(v -> intentarLogin());
        txtRegistrar.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegistrarse.class)));
        txtOlvidarContrasena.setOnClickListener(v -> startActivity(new Intent(this, ActivityRecuperarClave.class)));
    }

    private void intentarLogin() {
        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Ingresa tus datos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Verificando...");
        
        repositorio.iniciarSesion(correo, password, new AuthCallback() {
            @Override
            public void exito(Usuario usuario) {
                iniciarSincronizacionTotal(usuario.usuarioId);
            }

            @Override
            public void error(String mensaje) {
                btnIniciarSesion.setEnabled(true);
                btnIniciarSesion.setText("Iniciar Sesión");
                Toast.makeText(ActivityIniciarSesion.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarSincronizacionTotal(String uid) {
        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Sincronizando...");
        
        repositorio.sincronizarDatosCompletos(uid, new SincronizacionCompletaCallback() {
            @Override
            public void onSincronizacionExitosa(boolean tieneComunidad) {
                Class<?> destino = tieneComunidad ? ActivityMuroSeguridad.class : ActivitySeleccionComunidad.class;
                Intent intent = new Intent(ActivityIniciarSesion.this, destino);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onSincronizacionError(String mensaje) {
                btnIniciarSesion.setEnabled(true);
                btnIniciarSesion.setText("Iniciar Sesión");
                Toast.makeText(ActivityIniciarSesion.this, "Error de sincronización: " + mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
