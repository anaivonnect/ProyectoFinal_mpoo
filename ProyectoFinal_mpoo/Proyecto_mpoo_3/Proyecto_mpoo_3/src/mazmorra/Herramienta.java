package mazmorra;

public abstract class Herramienta {
    public static final int DURABILIDAD_MAXIMA = 100;

    private final String nombre;
    private final int danio;
    private int durabilidad;

    protected Herramienta(String nombre, int danio) {
        this.nombre = (nombre == null || nombre.trim().isEmpty()) ? "Arma" : nombre.trim();
        this.danio = entre(danio, 1, 50);
        this.durabilidad = DURABILIDAD_MAXIMA;
    }

    public abstract String grito();

    public abstract int golpear();

    public String getNombre() {
        return nombre;
    }

    public int getDanio() {
        return danio;
    }

    public int getDurabilidad() {
        return durabilidad;
    }

    public boolean estaRota() {
        return durabilidad <= 0;
    }

    public int usar() {
        if (estaRota()) {
            return 0;
        }
        durabilidad--;
        return danio;
    }

    public void reparar() {
        durabilidad = DURABILIDAD_MAXIMA;
    }

    @Override
    public String toString() {
        return nombre + " (Daño: " + danio + ", Durabilidad: " + durabilidad + "/" + DURABILIDAD_MAXIMA + ")";
    }

    private int entre(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }
}