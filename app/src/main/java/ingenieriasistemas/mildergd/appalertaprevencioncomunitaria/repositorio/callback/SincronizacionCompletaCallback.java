package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback;

public interface SincronizacionCompletaCallback {
    void onSincronizacionExitosa(boolean tieneComunidad);
    void onSincronizacionError(String mensaje);
}
