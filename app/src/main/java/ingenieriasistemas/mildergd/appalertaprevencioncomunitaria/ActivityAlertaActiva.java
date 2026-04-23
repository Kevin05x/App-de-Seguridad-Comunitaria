package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


public class ActivityAlertaActiva extends AppCompatActivity {

    private View vistaEmisor, vistaReceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerta_activa);

        ajustarSistemaUI();
        inicializarVistas();
        configurarBotonesYNav();
        cargarInformacionAlerta();
    }

    private void ajustarSistemaUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarVistas() {
        vistaEmisor = findViewById(R.id.vistaEmisor);
        vistaReceptor = findViewById(R.id.vistaReceptor);
    }

    private void configurarBotonesYNav() {
        ImageView btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabSos = findViewById(R.id.fab_sos);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_alert_progress);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (fabSos != null) fabSos.setOnClickListener(v -> navegarA(ActivityPanico.class));

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) navegarA(ActivityMuroSeguridad.class);
                else if (id == R.id.nav_profile) navegarA(ActivityPerfilUsuario.class);
                return true;
            });
        }
    }

    private void navegarA(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void cargarInformacionAlerta() {
        Intent intent = getIntent();
        boolean esEmisor = intent.getBooleanExtra("ES_EMISOR", true);
        String direccionRecibida = intent.getStringExtra("DIRECCION");
        String nombreRecibido = intent.getStringExtra("NOMBRE_USUARIO");

        TextView tvLoc = findViewById(R.id.tvLocation);
        TextView tvUser = findViewById(R.id.tvUserName);
        TextView tvDesc = findViewById(R.id.tvAlertDesc);

        if (tvLoc != null) {
            if (direccionRecibida != null && !direccionRecibida.isEmpty()) {
                tvLoc.setText(direccionRecibida);
                tvLoc.setTextColor(ContextCompat.getColor(this, R.color.red_alert));
            } else {
                tvLoc.setText("Ubicación no disponible");
            }

            View layoutLoc = findViewById(R.id.layoutLocation);
            if (layoutLoc instanceof android.widget.LinearLayout) {
                View iconLoc = ((android.widget.LinearLayout) layoutLoc).getChildAt(0);
                if (iconLoc instanceof ImageView) {
                    ((ImageView) iconLoc).setColorFilter(ContextCompat.getColor(this, R.color.red_alert));
                }
            }
        }

        if (esEmisor) {
            if (vistaEmisor != null) vistaEmisor.setVisibility(View.VISIBLE);
            if (vistaReceptor != null) vistaReceptor.setVisibility(View.GONE);
            cargarMiPerfil();
            if (tvDesc != null) tvDesc.setText("Has activado una alerta de pánico. Tu ubicación se actualiza en tiempo real.");
        } else {
            if (vistaEmisor != null) vistaEmisor.setVisibility(View.GONE);
            if (vistaReceptor != null) vistaReceptor.setVisibility(View.VISIBLE);

            if (tvUser != null && nombreRecibido != null) {
                tvUser.setText(nombreRecibido);
                if (tvDesc != null) tvDesc.setText(nombreRecibido + " ha activado una alerta de pánico.");
            }
        }
    }

    private void cargarMiPerfil() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            var user = ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.db.BaseDeDatosApp
                    .obtenerInstancia(this).daoUsuario().obtenerPorId(uid);
            if (user != null) {
                String miNombre = user.nombres + " " + (user.apellidos != null ? user.apellidos : "");
                runOnUiThread(() -> {
                    TextView tvName = findViewById(R.id.tvUserName);
                    if (tvName != null) tvName.setText(miNombre);
                });
            }
        });
    }
}