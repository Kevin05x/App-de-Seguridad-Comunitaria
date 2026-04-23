package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivityRecuperarClave extends AppCompatActivity {

    private TextInputEditText txtRecuperar;
    private Button btnEnviarEnlace;
    private TextView txtVolver;
    private ProgressBar progressBar;
    private UsuarioRepositorio repositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_clave);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        repositorio = new UsuarioRepositorio(this);

        txtRecuperar = findViewById(R.id.txtRecuperar);
        btnEnviarEnlace = findViewById(R.id.btnEnviarEnlace);
        txtVolver = findViewById(R.id.txtVolver);
        progressBar = findViewById(R.id.progressRecuperar);

        txtVolver.setOnClickListener(v -> finish());
        btnEnviarEnlace.setOnClickListener(v -> enviarCorreo());
    }

    private void enviarCorreo() {
        String correo = txtRecuperar.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            txtRecuperar.setError("Ingresa tu correo");
            return;
        }

        btnEnviarEnlace.setEnabled(false);
        btnEnviarEnlace.setText("Enviando...");
        progressBar.setVisibility(View.VISIBLE);

        repositorio.recuperarClave(correo, new SimpleCallback() {
            @Override
            public void exito(String mensaje) {
                progressBar.setVisibility(View.GONE);
                btnEnviarEnlace.setText("Enlace Enviado");
                Toast.makeText(ActivityRecuperarClave.this, mensaje, Toast.LENGTH_LONG).show();
                btnEnviarEnlace.postDelayed(() -> finish(), 2000);
            }

            @Override
            public void error(String mensaje) {
                progressBar.setVisibility(View.GONE);
                btnEnviarEnlace.setEnabled(true);
                btnEnviarEnlace.setText("Enviar enlace seguro");
                Toast.makeText(ActivityRecuperarClave.this, "Error: " + mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}