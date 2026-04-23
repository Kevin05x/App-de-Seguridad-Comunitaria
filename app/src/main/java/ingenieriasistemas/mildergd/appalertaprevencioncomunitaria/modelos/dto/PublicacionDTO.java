package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.dto;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.MultimediaPublicacion;
import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Publicacion;

public class PublicacionDTO {
    public Publicacion publicacion;
    public MultimediaPublicacion multimedia;
    public String nombreAutor;
    public boolean esUtilParaUsuarioActual = false;

    public PublicacionDTO() {
    }

    public PublicacionDTO(Publicacion publicacion, MultimediaPublicacion multimedia, String nombreAutor) {
        this.publicacion = publicacion;
        this.multimedia = multimedia;
        this.nombreAutor = nombreAutor;
    }
}
