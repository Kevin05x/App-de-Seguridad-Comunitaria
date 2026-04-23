package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio.callback;

import ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.modelos.Contacto;

public interface OnContactoClickListener {
    void onEditarClick(Contacto contacto);
    void onEliminarClick(Contacto contacto);
}