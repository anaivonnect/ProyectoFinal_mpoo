package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Personalizado extends Personaje {
    private final String tituloClase;
    private final int defensa;

    public Personalizado(String nombre, String tituloClase, int vidaMaxima, int defensa) {
        super(nombre, vidaMaxima);
        this.tituloClase = (tituloClase == null || tituloClase.trim().isEmpty())
                ? "Personalizado"
                : tituloClase.trim();
        this.defensa = Math.max(0, Math.min(defensa, 15)); // Defensa real acotada (0-15)
    }

    public int getDefensa() {
        return defensa;
    }

    @Override
    public String getTipo() {
        return tituloClase;
    }

    @Override
    protected int calcularDanio(int base) {
        return base + 4;
    }

    @Override
    public int recibirDanio(int puntos) {
        if (!estaVivo() || puntos <= 0) {
            return 0;
        }
        int mitigado = Math.max(1, puntos - defensa);
        return super.recibirDanio(mitigado);
    }

    @Override
    protected Color getColorClase() {
        return new Color(231, 176, 74);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        // Corona dorada
        g.setColor(color);
        int[] xs = {x + 12, x + 16, x + 23, x + 30, x + 37, x + 44, x + 48};
        int[] ys = {y + 2, y - 12, y - 2, y - 14, y - 2, y - 12, y + 2};
        g.fillPolygon(xs, ys, 7);
    }
}