package mazmorra;

public class Arco extends Herramienta {

    public Arco(int danio) {
        super("Arco", danio);
    }

    @Override
    public String grito() {
        return "dispara " + getFlechas() + " flechas rápidas";
    }

    public int getFlechas() {
        return 2;
    }

    @Override
    public int golpear() {
        int total = 0;
        for (int i = 0; i < getFlechas(); i++) {
            total += usar();
        }
        return total;
    }
}