package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Bundle;
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
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.ComunidadRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class ActivitySeleccionComunidad extends AppCompatActivity {
    private TextInputEditText txtCodigo;
    private ComunidadRepositorio repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seleccion_comunidad);
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        repo = new ComunidadRepositorio(this);
        txtCodigo = findViewById(R.id.txtCodigoUnion);

        findViewById(R.id.btnCrearGrupo).setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityRegistrarComunidad.class));
        });

        findViewById(R.id.btnUnirseGrupo).setOnClickListener(v -> unirse());
    }

    private void unirse() {
        String codigo = txtCodigo.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getUid();
        if (codigo.isEmpty() || uid == null) {
            Toast.makeText(this, "Ingresa un código válido", Toast.LENGTH_SHORT).show();
            return;
        }
        Button btn = findViewById(R.id.btnUnirseGrupo);
        btn.setEnabled(false);
        btn.setText("Verificando...");
        repo.unirsePorCodigo(codigo, uid, new SimpleCallback() {
            @Override
            public void exito(String mensaje) {
                Intent i = new Intent(ActivitySeleccionComunidad.this, ActivityMuroSeguridad.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void error(String mensaje) {
                btn.setEnabled(true);
                btn.setText("Unirse a Grupo Existente");
                Toast.makeText(ActivitySeleccionComunidad.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }
}