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
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.AuthCallback;

public class ActivityRegistrarse extends AppCompatActivity {
    private TextInputEditText txtNombre, txtCorreo, txtPassword;
    private Button btnContinuar;
    private UsuarioRepositorio usuarioRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrarse);
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        usuarioRepo = new UsuarioRepositorio(this);
        txtNombre = findViewById(R.id.txtNombre);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        btnContinuar = findViewById(R.id.btnContinuar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnContinuar.setOnClickListener(v -> iniciarRegistro());
    }

    private void iniciarRegistro() {
        String nombre = txtNombre.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo) || password.length() < 6) {
            Toast.makeText(this, "Completa tus datos (Clave min. 6 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }
        btnContinuar.setEnabled(false);
        btnContinuar.setText("Registrando...");
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.nombres = nombre;
        nuevoUsuario.correo = correo;
        usuarioRepo.registrarUsuario(nuevoUsuario, password, new AuthCallback() {
            @Override
            public void exito(Usuario usuario) {
                Toast.makeText(ActivityRegistrarse.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ActivityRegistrarse.this, ActivitySeleccionComunidad.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(String mensaje) {
                btnContinuar.setEnabled(true);
                btnContinuar.setText("Continuar");
                Toast.makeText(ActivityRegistrarse.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}