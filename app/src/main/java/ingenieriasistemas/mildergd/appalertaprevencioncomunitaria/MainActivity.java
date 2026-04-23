package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.UsuarioRepositorio;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback.SimpleCallback;

public class MainActivity extends AppCompatActivity {
    private Button btnIniciarSesion, btnRegistro;
    private static boolean monitorRegistrado = false;
    
    public static boolean isExternalIntentActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        registrarMonitorSalidaGlobal();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ActivityMuroSeguridad.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistro = findViewById(R.id.btnRegistro);

        btnIniciarSesion.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ActivityIniciarSesion.class)));
        btnRegistro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ActivityRegistrarse.class)));
    }

    private void registrarMonitorSalidaGlobal() {
        if (monitorRegistrado) return;
        
        getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable logoutRunnable;
            private int startedActivities = 0;
            
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                startedActivities++;
                if (logoutRunnable != null) {
                    handler.removeCallbacks(logoutRunnable);
                    logoutRunnable = null;
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                startedActivities--;
                if (startedActivities == 0 && !activity.isChangingConfigurations()) {
                    logoutRunnable = () -> {
                        if (isExternalIntentActive) return;

                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            UsuarioRepositorio repo = new UsuarioRepositorio(getApplicationContext());
                            repo.cerrarSesion(new SimpleCallback() {
                                @Override 
                                public void exito(String msj) {
                                    irAlInicio();
                                }
                                @Override 
                                public void error(String msj) {
                                    irAlInicio();
                                }
                            });
                        }
                    };
                    handler.postDelayed(logoutRunnable, 1000);
                }
            }

            private void irAlInicio() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplicationContext().startActivity(intent);
            }

            @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
            @Override public void onActivityResumed(@NonNull Activity activity) {}
            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
        
        monitorRegistrado = true;
    }
}