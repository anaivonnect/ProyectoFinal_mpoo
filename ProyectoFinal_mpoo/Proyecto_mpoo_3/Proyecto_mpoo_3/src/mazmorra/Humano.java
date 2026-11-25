package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Humano extends Personaje {
    public Humano(String nombre) {
        super(nombre, 100); // Equilibrado, recibe mejor curación
    }

    @Override
    public String getTipo() {
        return "Humano";
    }

    @Override
    protected int calcularDanio(int base) {
        return base + 5;
    }

    @Override
    public int curar(int puntos) {
        if (!estaVivo() || puntos <= 0) {
            return 0;
        }
        return super.curar(puntos + 3); // Resiliencia humana
    }

    @Override
    protected Color getColorClase() {
        return new Color(78, 161, 226);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        g.setColor(new Color(180, 192, 210));
        g.fillRoundRect(x + 10, y - 6, 40, 12, 8, 8);
    }
}