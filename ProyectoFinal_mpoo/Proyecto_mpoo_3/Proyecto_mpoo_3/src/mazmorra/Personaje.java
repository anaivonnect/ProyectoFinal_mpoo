package mazmorra;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Personaje implements Dibujable {
    private final String nombre;
    private final int vidaMaxima;
    private int vida;
    private Herramienta herramienta;
    private Personaje companero;

    protected Personaje(String nombre, int vidaMaxima) {
        this.nombre = (nombre == null || nombre.trim().isEmpty()) ? "Aventurero" : nombre.trim();
        this.vidaMaxima = Math.max(1, vidaMaxima);
        this.vida = this.vidaMaxima;
    }

    public abstract String getTipo();

    protected abstract int calcularDanio(int base);

    protected Color getColorClase() {
        return new Color(78, 161, 226);
    }

    protected void dibujarDetalleClase(Graphics2D g, int x, int y, Color color) {
        g.setColor(color.darker());
        g.fillRoundRect(x + 8, y - 8, 44, 12, 8, 8);
    }

    @Override
    public void dibujar(Graphics2D g, int x, int y) {
        Color color = estaVivo() ? getColorClase() : new Color(110, 118, 130);

        // Sombra
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(x - 18, y + 120, 96, 24);

        // Cabeza
        g.setColor(estaVivo() ? new Color(230, 198, 158) : new Color(165, 165, 165));
        g.fillOval(x + 13, y, 34, 38);

        // Detalle distintivo polimórfico por raza
        dibujarDetalleClase(g, x, y, color);

        // Ojos (o X si cayó en combate)
        g.setColor(new Color(30, 34, 44));
        if (estaVivo()) {
            g.fillOval(x + 22, y + 15, 4, 4);
            g.fillOval(x + 34, y + 15, 4, 4);
        } else {
            g.drawLine(x + 21, y + 14, x + 26, y + 19);
            g.drawLine(x + 26, y + 14, x + 21, y + 19);
            g.drawLine(x + 33, y + 14, x + 38, y + 19);
            g.drawLine(x + 38, y + 14, x + 33, y + 19);
        }

        // Torso
        g.setColor(color);
        g.fillRoundRect(x, y + 38, 60, 82, 12, 12);

        // Escudo / emblema del brazo
        g.setColor(color.brighter());
        g.fillOval(x + 43, y + 48, 18, 18);

        // Indicador simple del arma equipada
        if (herramienta != null && !herramienta.estaRota()) {
            g.setColor(new Color(220, 226, 238));
            if (herramienta instanceof Baston) {
                g.setColor(new Color(145, 98, 62));
                g.fillRect(x + 58, y + 25, 5, 90);
                g.setColor(new Color(130, 220, 255));
                g.fillOval(x + 54, y + 18, 13, 13);
            } else if (herramienta instanceof Arco) {
                g.setColor(new Color(180, 132, 74));
                g.drawArc(x + 48, y + 32, 22, 60, -85, 170);
            } else {
                g.fillRect(x + 58, y + 24, 5, 46);
                g.setColor(new Color(231, 176, 74));
                g.fillRect(x + 53, y + 66, 15, 4);
            }
        }
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public Herramienta getHerramienta() {
        return herramienta;
    }

    public void equipar(Herramienta h) {
        this.herramienta = h;
    }

    public int atacar(Personaje objetivo) {
        if (!estaVivo() || herramienta == null || objetivo == null || objetivo == this || !objetivo.estaVivo()) {
            return 0;
        }
        int base = herramienta.golpear();
        if (base <= 0) {
            return 0;
        }
        int danioFinal = Math.max(1, calcularDanio(base));
        return objetivo.recibirDanio(danioFinal);
    }

    public Personaje getCompanero() {
        return companero;
    }

    public void setCompanero(Personaje companero) {
        if (companero == this) {
            return;
        }
        this.companero = companero;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public int recibirDanio(int puntos) {
        if (!estaVivo() || puntos <= 0) {
            return 0;
        }
        int vidaAnterior = this.vida;
        this.vida = limitar(this.vida - puntos);
        return vidaAnterior - this.vida;
    }

    public int curar(int puntos) {
        if (!estaVivo() || puntos <= 0) {
            return 0;
        }
        int vidaAnterior = this.vida;
        this.vida = limitar(this.vida + puntos);
        return this.vida - vidaAnterior;
    }

    public void restaurarCompleto() {
        this.vida = this.vidaMaxima;
        if (herramienta != null) {
            herramienta.reparar();
        }
    }

    public int ayudarCompanero() {
        if (!estaVivo() || companero == null || !companero.estaVivo()) {
            return 0;
        }
        return companero.curar(10);
    }

    @Override
    public String toString() {
        return nombre + " (" + getTipo() + ") " + vida + "/" + vidaMaxima;
    }

    private int limitar(int valor) {
        return Math.max(0, Math.min(valor, vidaMaxima));
    }
}