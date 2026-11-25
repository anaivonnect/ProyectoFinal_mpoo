package mazmorra;

public class Moldes {

    public static Herramienta crearArmaBasica(int opcion) {
        switch (opcion) {
            case 1:
                return new Espada(20);
            case 2:
                return new Arco(11); // 2 flechas x 11 = 22 daño, gasta 2 durabilidad
            case 3:
            default:
                return new Baston(14); // Con Mago escala x1.8 (~25 daño)
        }
    }

    public static Personaje crearAliado(int opcion) {
        Personaje aliado = null;
        if (opcion == 1) {
            aliado = new Mago("Gandalf el Gris");
            aliado.equipar(new Baston(14));
        } else if (opcion == 2) {
            aliado = new Enano("Thorin");
            aliado.equipar(new Espada(18));
        } else if (opcion == 3) {
            aliado = new Elfo("Legolas");
            aliado.equipar(new Arco(11));
        }
        return aliado;
    }

    public static Personaje crearEnemigoMazmorra(int nivelSala) {
        Personaje enemigo;
        if (nivelSala == 2) {
            enemigo = new Mago("Hechicero Sombrio");
            enemigo.equipar(new Baston(15));
        } else if (nivelSala == 3) {
            enemigo = new Personalizado("Senor de las Sombras", "Jefe Oscuro", 160, 4);
            enemigo.equipar(new Espada(22));
        } else {
            enemigo = new Orco("Goblin Salvaje");
            enemigo.equipar(new Espada(12));
        }
        return enemigo;
    }
}