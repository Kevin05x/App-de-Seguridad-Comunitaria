package ingenieriasistemas.mildergd.appalertaprevencioncomunitaria.repositorio;

import org.mindrot.jbcrypt.BCrypt;

public class HashContra {

    public static String hashear(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verificar(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
