package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public class Orco extends Personaje {
    public Orco(String nombre) {
        super(nombre, 140); // Berserker: mucha vida y furia, pero recibe +2 daño
    }

    @Override
    public String getTipo() {
        return "Orco";
    }

    @Override
    protected int calcularDanio(int base) {
        return base + 7;
    }

    @Override
    public int recibirDanio(int puntos) {
        if (!estaVivo() || puntos <= 0) {
            return 0;
        }
        return super.recibirDanio(puntos + 2);
    }

    @Override
    protected Color getColorClase() {
        return new Color(214, 86, 86);
    }

    @Override
    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        // Banda de guerra y colmillos
        g.setColor(new Color(95, 130, 82));
        g.fillOval(x + 13, y, 34, 38);
        g.setColor(Color.WHITE);
        g.fillRect(x + 21, y + 26, 4, 6);
        g.fillRect(x + 35, y + 26, 4, 6);
    }
}