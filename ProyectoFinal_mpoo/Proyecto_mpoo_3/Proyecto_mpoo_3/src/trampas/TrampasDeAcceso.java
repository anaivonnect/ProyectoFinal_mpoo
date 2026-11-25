package trampas;

import mazmorra.Personaje;

public class TrampasDeAcceso {

    public static boolean validarAccesoSalaOculta(Personaje p) {
        if (p == null) {
            System.out.println("  [!] Acceso denegado: No hay un personaje válido.");
            return false;
        }

        if (Tramposo.estaBaneado()) {
            System.out.println("  [X] Acceso denegado: La cuenta está BANEADA por uso de trampas.");
            return false;
        }

        if (!p.estaVivo()) {
            System.out.println("  [!] Acceso denegado a la Sala Oculta: Tu personaje está derrotado (0 HP).");
            return false;
        }

        if (p.getVida() < 20) {
            System.out.println("  [!] Acceso denegado a la Sala Oculta: Tienes poca vida (" + p.getVida() + "/20 mínimo).");
            return false;
        }

        System.out.println("  [OK] Acceso concedido a la Zona Prohibida para " + p.getNombre() + ".");
        return true;
    }
}