package mazmorra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import trampas.TrampasDeAcceso;
import trampas.Tramposo;

public class PersonajeTest {

    @BeforeEach
    public void prepararEntorno() {
        Tramposo.establecerUsuario(null);
        Tramposo.reiniciarAdvertencias();
    }

    @AfterEach
    public void limpiarEntorno() {
        Tramposo.establecerUsuario(null);
        Tramposo.reiniciarAdvertencias();
    }

    @Test
    public void testCreacionYEncapsulamiento() {
        Personaje humano = new Humano("Hengel");
        Herramienta espada = new Espada(20);
        humano.equipar(espada);

        assertEquals("Hengel", humano.getNombre());
        assertEquals(100, humano.getVida());
        assertTrue(humano.estaVivo());
        assertNotNull(humano.getHerramienta());
        assertEquals("Espada", humano.getHerramienta().getNombre());
        assertTrue(humano instanceof Dibujable);
    }

    @Test
    public void testCombatePolimorficoYBalanceArmas() {
        Personaje heroe = new Humano("Hengel");
        heroe.equipar(new Espada(20));

        Personaje enemigo = new Orco("Goblin Salvaje");
        enemigo.equipar(new Espada(12));

        int vidaInicialEnemigo = enemigo.getVida();
        int danio = heroe.atacar(enemigo);
        assertTrue(danio > 0);
        assertEquals(vidaInicialEnemigo - danio, enemigo.getVida());
        assertEquals(99, heroe.getHerramienta().getDurabilidad());

        // El Bastón también debe desgastarse y escalar con Mago
        Personaje mago = new Mago("Gandalf");
        mago.equipar(new Baston(14));
        int danioMagico = mago.atacar(enemigo);
        assertTrue(danioMagico >= 25);
        assertEquals(99, mago.getHerramienta().getDurabilidad());

        // Personalizado debe mitigar daño con su defensa real
        Personaje tanque = new Personalizado("Arturo", "Paladin", 120, 5);
        int recibido = tanque.recibirDanio(20);
        assertEquals(15, recibido);
        assertEquals(105, tanque.getVida());
    }

    @Test
    public void testArmaRotaYReparacion() {
        Herramienta baston = new Baston(10);
        for (int i = 0; i < Herramienta.DURABILIDAD_MAXIMA; i++) {
            assertEquals(10, baston.golpear());
        }
        assertTrue(baston.estaRota());
        assertEquals(0, baston.golpear());

        baston.reparar();
        assertFalse(baston.estaRota());
        assertEquals(100, baston.getDurabilidad());
        assertEquals(10, baston.golpear());
    }

    @Test
    public void testProteccionContraNulosAutoAtaqueYRevivir() {
        Personaje elfo = new Elfo("Legolas");
        elfo.equipar(new Arco(11));

        // No debe fallar ni permitirse auto-ataque ni auto-compañero
        assertEquals(0, elfo.atacar(null));
        assertEquals(0, elfo.atacar(elfo));
        elfo.setCompanero(elfo);
        assertNull(elfo.getCompanero());

        // Un personaje derrotado no puede curarse normalmente, pero sí restaurarse completo
        elfo.recibirDanio(500);
        assertFalse(elfo.estaVivo());
        assertEquals(0, elfo.curar(20));

        elfo.restaurarCompleto();
        assertTrue(elfo.estaVivo());
        assertEquals(elfo.getVidaMaxima(), elfo.getVida());
    }

    @Test
    public void testSistemaDeCompanerosYCuracion() {
        Personaje lider = new Elfo("Rodo");
        lider.equipar(new Espada(20));

        Personaje aliado = new Enano("Thorin");
        aliado.equipar(new Espada(18));

        lider.setCompanero(aliado);
        aliado.setCompanero(lider);

        assertNotNull(lider.getCompanero());
        assertEquals("Thorin", lider.getCompanero().getNombre());
        assertFalse(Main.esUsuarioValido("ab"));
        Personaje orco = new Orco("Orco");
        orco.equipar(new Espada(25));
        orco.atacar(aliado);

        int vidaHeridoAliado = aliado.getVida();
        int curado = lider.ayudarCompanero();
        assertTrue(curado > 0);
        assertEquals(vidaHeridoAliado + curado, aliado.getVida());
    }

    @Test
    public void testMoldesYComparar() {
        Herramienta arco = Moldes.crearArmaBasica(2);
        assertEquals("Arco", arco.getNombre());

        Personaje aliadoMago = Moldes.crearAliado(1);
        assertNotNull(aliadoMago);
        assertTrue(aliadoMago instanceof Mago);

        Personaje jefe = Moldes.crearEnemigoMazmorra(3);
        assertEquals("Senor de las Sombras", jefe.getNombre());
    }

    @Test
    public void testDeteccionDeTrampasYBaneo() {
        assertEquals(0, Tramposo.getAdvertencias());
        assertFalse(Tramposo.estaBaneado());

        assertFalse(Tramposo.detectarCheats(50, 50, 200, "Vida"));
        assertTrue(Tramposo.detectarCheats(9999, "Vida"));
        assertTrue(Tramposo.detectarCheats(-50, "Defensa"));
        assertTrue(Tramposo.detectarCheats(5000, "Daño"));
        assertTrue(Tramposo.detectarCheats(8888, "Extra"));

        // Debe quedarse topado en 3/3
        assertEquals(3, Tramposo.getAdvertencias());
        assertTrue(Tramposo.estaBaneado());

        Personaje mago = new Mago("Gandalf");
        assertFalse(TrampasDeAcceso.validarAccesoSalaOculta(mago));
        assertFalse(TrampasDeAcceso.validarAccesoSalaOculta(null));
    }

    @Test
    public void testValidacionDeNicknamesYNombres() {
        assertTrue(Main.esUsuarioValido("Rodo_99"));
        assertFalse(Main.esUsuarioValido("12345"));
        assertFalse(Main.esUsuarioValido("Invitado"));
        assertFalse(Main.esUsuarioValido("a:b"));
        assertFalse(Main.esUsuarioValido("ab"));

        assertTrue(Main.esNombrePersonajeValido("Sir Hengel"));
        assertFalse(Main.esNombrePersonajeValido(""));
        assertFalse(Main.esNombrePersonajeValido("   "));
        assertFalse(Main.esNombrePersonajeValido("9999"));
        assertFalse(Main.esNombrePersonajeValido("<html>HACK</html>"));
    }
}