package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback;

import java.util.List;

public interface CallbackComunidades {
    void exito(List<String> comunidadesIds);
    void error(String mensaje);
}