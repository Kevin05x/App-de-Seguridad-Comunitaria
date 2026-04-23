package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback;

import java.util.List;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.dto.PublicacionDTO;

public interface CallbackListadoPublicaciones {
    void exito(List<PublicacionDTO> publicaciones);
    void error (String mensaje);
}
