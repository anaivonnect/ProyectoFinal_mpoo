package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Elfo extends Personaje {
    public Elfo(String nombre) {
        super(nombre, 90); // Alta precisión y afinidad con Arco
    }

    @Override
    public String getTipo() {
        return "Elfo";
    }

    @Override
    protected int calcularDanio(int base) {
        int bonoArco = (getHerramienta() instanceof Arco) ? 3 : 0;
        return base + 7 + bonoArco;
    }

    @Override
    protected Color getColorClase() {
        return new Color(78, 201, 153);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        // Capucha élfica
        g.setColor(color.darker());
        int[] xs = {x + 8, x + 30, x + 52};
        int[] ys = {y + 8, y - 14, y + 8};
        g.fillPolygon(xs, ys, 3);
    }
}