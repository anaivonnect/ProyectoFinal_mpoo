package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Mago extends Personaje {
    public Mago(String nombre) {
        super(nombre, 80); // Alto daño mágico con Bastón
    }

    @Override
    public String getTipo() {
        return "Mago";
    }

    @Override
    protected int calcularDanio(int base) {
        if (getHerramienta() instanceof Baston) {
            return (int) Math.round(base * 1.8); // Sinergia real con Bastón
        }
        return base + 2; // Evita el exploit de Mago + Arco
    }

    @Override
    protected Color getColorClase() {
        return new Color(138, 108, 224);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        // Sombrero puntiagudo de mago
        g.setColor(color.darker());
        g.fillRect(x + 4, y - 2, 52, 6);
        int[] xs = {x + 12, x + 30, x + 48};
        int[] ys = {y - 2, y - 28, y - 2};
        g.fillPolygon(xs, ys, 3);
    }
}