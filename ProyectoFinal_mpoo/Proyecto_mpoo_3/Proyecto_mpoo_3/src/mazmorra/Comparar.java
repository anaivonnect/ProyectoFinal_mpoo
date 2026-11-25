package mazmorra;

public class Comparar {

    public static void compararPersonajes(Personaje p1, Personaje p2) {
        if (p1 == null || p2 == null) {
            System.out.println("  [!] Se necesitan dos personajes válidos para realizar la comparación.");
            return;
        }

        System.out.println("\n--- ANÁLISIS COMPARATIVO DE PERSONAJES ---");
        System.out.println("Combatiente 1: " + p1);
        System.out.println("Combatiente 2: " + p2);

        if (p1.getVida() > p2.getVida()) {
            int diferencia = p1.getVida() - p2.getVida();
            System.out.println("  -> Ventaja de vitalidad: " + p1.getNombre() + " supera por " + diferencia + " HP a " + p2.getNombre() + ".");
        } else if (p2.getVida() > p1.getVida()) {
            int diferencia = p2.getVida() - p1.getVida();
            System.out.println("  -> Ventaja de vitalidad: " + p2.getNombre() + " supera por " + diferencia + " HP a " + p1.getNombre() + ".");
        } else {
            System.out.println("  -> Empate técnico: Ambos personajes tienen exactamente " + p1.getVida() + " HP.");
        }

        if (p1.getHerramienta() != null && p2.getHerramienta() != null) {
            compararArmas(p1.getHerramienta(), p2.getHerramienta());
        }
    }

    public static void compararArmas(Herramienta h1, Herramienta h2) {
        if (h1 == null || h2 == null) {
            return;
        }
        System.out.println("--- COMPARACIÓN DE ARMAMENTO ---");
        System.out.println("Arma 1: " + h1);
        System.out.println("Arma 2: " + h2);

        if (h1.getDanio() > h2.getDanio()) {
            System.out.println("  -> Mayor daño base: " + h1.getNombre() + " (" + h1.getDanio() + " vs " + h2.getDanio() + ").");
        } else if (h2.getDanio() > h1.getDanio()) {
            System.out.println("  -> Mayor daño base: " + h2.getNombre() + " (" + h2.getDanio() + " vs " + h1.getDanio() + ").");
        } else {
            System.out.println("  -> Empate en daño base (" + h1.getDanio() + ").");
        }

        if (h1.getDurabilidad() > h2.getDurabilidad()) {
            System.out.println("  -> Mejor estado de arma: " + h1.getNombre() + " tiene mayor durabilidad.");
        } else if (h2.getDurabilidad() > h1.getDurabilidad()) {
            System.out.println("  -> Mejor estado de arma: " + h2.getNombre() + " tiene mayor durabilidad.");
        } else {
            System.out.println("  -> Ambas armas tienen la misma durabilidad (" + h1.getDurabilidad() + ").");
        }
    }
}