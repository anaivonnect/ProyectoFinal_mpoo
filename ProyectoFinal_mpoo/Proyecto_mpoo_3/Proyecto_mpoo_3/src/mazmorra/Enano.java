package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Enano extends Personaje {
    public Enano(String nombre) {
        super(nombre, 130); // Tanque con armadura natural (-3 daño recibido)
    }

    @Override
    public String getTipo() {
        return "Enano";
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
        return super.recibirDanio(Math.max(1, puntos - 3));
    }

    @Override
    protected Color getColorClase() {
        return new Color(196, 125, 65);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        // Casco de hierro y barba
        g.setColor(new Color(140, 148, 162));
        g.fillRoundRect(x + 9, y - 6, 42, 13, 8, 8);
        g.setColor(new Color(115, 65, 32));
        g.fillOval(x + 14, y + 26, 32, 18);
    }
}