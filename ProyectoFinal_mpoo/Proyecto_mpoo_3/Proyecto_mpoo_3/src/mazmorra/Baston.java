package mazmorra;

public class Baston extends Herramienta {

    public Baston(int danio) {
        super("Baston", danio);
    }

    @Override
    public String grito() {
        return "lanza un hechizo arcano";
    }

    @Override
    public int golpear() {
        return usar(); // Ahora sí se desgasta al usarse
    }
}