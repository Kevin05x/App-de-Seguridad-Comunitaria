package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Usuario;

public interface AuthCallback {
    void exito(Usuario usuario);
    void error(String mensaje);
}
