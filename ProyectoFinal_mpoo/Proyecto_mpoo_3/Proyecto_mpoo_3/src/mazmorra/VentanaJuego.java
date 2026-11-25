package mazmorra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import trampas.Tramposo;

public class VentanaJuego extends JFrame {
    private static final Color FONDO = new Color(13, 18, 28);
    private static final Color PANEL = new Color(24, 31, 44);
    private static final Color TEXTO = new Color(235, 241, 248);
    private static final Color AZUL = new Color(78, 161, 226);
    private static final Color VERDE = new Color(78, 201, 153);
    private static final Color ROJO = new Color(230, 92, 92);
    private static final Color DORADO = new Color(231, 176, 74);

    private static final int POCIONES_MAXIMAS = 3;
    private static VentanaJuego instanciaActiva;

    private Personaje jugador;
    private Personaje enemigo;
    private final boolean modoPersonalizado;
    private final String usuarioActivo;

    private int pocionesRestantes = POCIONES_MAXIMAS;
    private int desplazamientoHeroe = 0;
    private int desplazamientoEnemigo = 0;
    private int frameAnimacion = 0;

    private JTextArea consolaLog;
    private JLabel estadoTitulo;
    private JComboBox<String> selectorSala;
    private JPanel panelLienzo;
    private JPanel panelEstados;
    private JButton btnAtacar;
    private JButton btnCurar;
    private JButton btnTrampa;
    private Timer animacionTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaJuego().setVisible(true));
    }

    public static void abrirUnica(Personaje jugadorConsola, String usuario) {
        SwingUtilities.invokeLater(() -> {
            if (instanciaActiva != null && instanciaActiva.isDisplayable()) {
                instanciaActiva.jugador = jugadorConsola;
                instanciaActiva.sincronizarControles();
                instanciaActiva.actualizarTodo();
                instanciaActiva.toFront();
                instanciaActiva.requestFocus();
            } else {
                instanciaActiva = new VentanaJuego(jugadorConsola, usuario);
                instanciaActiva.setVisible(true);
            }
        });
    }

    public static void cerrarSiAbierta() {
        SwingUtilities.invokeLater(() -> {
            if (instanciaActiva != null && instanciaActiva.isDisplayable()) {
                instanciaActiva.dispose();
            }
        });
    }

    // Constructor standalone (ejecución directa sin Main)
    public VentanaJuego() {
        this.modoPersonalizado = false;
        this.usuarioActivo = "Demo";
        Tramposo.establecerUsuario(null);
        configurarVentana();
        inicializarDemo();
        construirInterfaz();
        sincronizarControles();
        iniciarAnimacion();
    }

    // Constructor compatible con 1 parámetro
    public VentanaJuego(Personaje jugadorConsola) {
        this(jugadorConsola, "Jugador");
    }

    // Constructor conectado a la sesión de Main (sin borrar advertencias ni curar gratis)
    public VentanaJuego(Personaje jugadorConsola, String usuario) {
        this.modoPersonalizado = true;
        this.usuarioActivo = (usuario == null || usuario.trim().isEmpty()) ? "Jugador" : usuario.trim();
        this.jugador = jugadorConsola;
        this.enemigo = Moldes.crearEnemigoMazmorra(1);
        configurarVentana();
        construirInterfaz();
        sincronizarControles();
        iniciarAnimacion();
    }

    private void inicializarDemo() {
        jugador = new Humano("Hengel");
        jugador.equipar(new Espada(20));
        Personaje gandalf = new Mago("Gandalf");
        gandalf.equipar(new Baston(14)); // Ahora sí lleva bastón equipado
        jugador.setCompanero(gandalf);
        gandalf.setCompanero(jugador);
        enemigo = Moldes.crearEnemigoMazmorra(1);
        pocionesRestantes = POCIONES_MAXIMAS;
    }

    private void configurarVentana() {
        setTitle("La Orden de las Sombras - " + usuarioActivo);
        setSize(980, 690);
        setMinimumSize(new Dimension(840, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (animacionTimer != null) {
                    animacionTimer.stop();
                }
                if (instanciaActiva == VentanaJuego.this) {
                    instanciaActiva = null;
                }
            }
        });
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(12, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearAcciones(), BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel("LA ORDEN DE LAS SOMBRAS");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);

        selectorSala = new JComboBox<>(new String[]{
                "Sala 1: Goblin Salvaje",
                "Sala 2: Hechicero Sombrio",
                "Sala 3: Senor de las Sombras"
        });
        selectorSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectorSala.addActionListener(e -> cambiarSala());

        estadoTitulo = new JLabel();
        estadoTitulo.setForeground(DORADO);
        estadoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        derecha.add(selectorSala);
        derecha.add(estadoTitulo);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        actualizarEstadoTitulo();
        return encabezado;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);

        panelLienzo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dibujarEscenario(g2d);
                g2d.dispose();
            }
        };
        panelLienzo.setOpaque(false);
        panelLienzo.setPreferredSize(new Dimension(700, 330));
        panelLienzo.setBorder(BorderFactory.createLineBorder(new Color(55, 66, 87), 1));

        panelEstados = new JPanel(new GridLayout(1, 3, 10, 0));
        panelEstados.setOpaque(false);

        JPanel logCard = crearTarjetaLog();
        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setOpaque(false);
        superior.add(panelLienzo, BorderLayout.CENTER);
        superior.add(panelEstados, BorderLayout.SOUTH);

        centro.add(superior, BorderLayout.CENTER);
        centro.add(logCard, BorderLayout.EAST);
        actualizarEstados();
        return centro;
    }

    private JPanel crearTarjetaLog() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(PANEL);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 66, 87)),
                new EmptyBorder(10, 10, 10, 10)));
        contenedor.setPreferredSize(new Dimension(265, 0));

        JLabel titulo = new JLabel("REGISTRO DE COMBATE");
        titulo.setForeground(DORADO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contenedor.add(titulo, BorderLayout.NORTH);

        consolaLog = new JTextArea();
        consolaLog.setEditable(false);
        consolaLog.setOpaque(false);
        consolaLog.setForeground(new Color(186, 220, 200));
        consolaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        consolaLog.setLineWrap(true);
        consolaLog.setWrapStyleWord(true);
        consolaLog.setText("> Interfaz lista para " + usuarioActivo + ".\n> Enfrentando a: " + enemigo.getNombre() + ".");

        JScrollPane scroll = new JScrollPane(consolaLog);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearAcciones() {
        JPanel acciones = new JPanel(new GridLayout(1, 4, 10, 0));
        acciones.setOpaque(false);

        btnAtacar = crearBoton("ATACAR", AZUL);
        btnCurar = crearBoton("POCION (" + pocionesRestantes + ")", VERDE);
        btnTrampa = crearBoton("PROBAR TRAMPA", DORADO);
        JButton btnReiniciar = crearBoton("NUEVO COMBATE", new Color(104, 116, 148));

        btnAtacar.addActionListener(this::atacar);
        btnCurar.addActionListener(this::curarJugador);
        btnTrampa.addActionListener(this::simularTrampa);
        btnReiniciar.addActionListener(e -> reiniciarCombate());

        acciones.add(btnAtacar);
        acciones.add(btnCurar);
        acciones.add(btnTrampa);
        acciones.add(btnReiniciar);
        return acciones;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.brighter(), 1),
                new EmptyBorder(12, 12, 12, 12)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private void dibujarEscenario(Graphics2D g2d) {
        int ancho = panelLienzo.getWidth();
        int alto = panelLienzo.getHeight();

        GradientPaint fondo = new GradientPaint(0, 0, new Color(26, 29, 45),
                0, alto, new Color(12, 15, 23));
        g2d.setPaint(fondo);
        g2d.fillRect(0, 0, ancho, alto);

        g2d.setColor(new Color(48, 54, 72));
        g2d.fillRect(0, alto - 70, ancho, 70);
        g2d.setColor(new Color(67, 74, 94));
        for (int x = 0; x < ancho; x += 64) {
            g2d.drawLine(x, alto - 70, x - 30, alto);
        }

        int respiracion = (frameAnimacion / 4) % 2 == 0 ? 0 : 3;

        dibujarPersonaje(g2d, (int) (ancho * 0.16) + desplazamientoHeroe, 100 + respiracion, jugador, "HEROE", AZUL);

        if (jugador.getCompanero() != null) {
            Color colorAliado = jugador.getCompanero().estaVivo() ? VERDE : new Color(110, 118, 130);
            dibujarPersonaje(g2d, (int) (ancho * 0.36) + (desplazamientoHeroe / 2), 120 + respiracion,
                    jugador.getCompanero(), "ALIADO", colorAliado);
        }

        Color colorEnemigo = (frameAnimacion / 3) % 2 == 0 ? ROJO : new Color(190, 67, 67);
        dibujarPersonaje(g2d, (int) (ancho * 0.72) + desplazamientoEnemigo, 100 - respiracion, enemigo, "ENEMIGO", colorEnemigo);
    }

    private void dibujarPersonaje(Graphics2D g2d, int x, int y, Personaje personaje,
                                  String rol, Color colorEtiqueta) {
        // Uso explícito de la interfaz Dibujable (polimorfismo por interfaz)
        Dibujable sprite = personaje;
        sprite.dibujar(g2d, x, y);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.setColor(TEXTO);
        g2d.drawString(personaje.getNombre(), x - 12, y - 46);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.setColor(colorEtiqueta);
        g2d.drawString(rol + " - " + personaje.getTipo(), x - 12, y - 31);

        dibujarBarraVida(g2d, x - 8, y + 148, 76,
                personaje.getVida(), personaje.getVidaMaxima());
    }

    private void dibujarBarraVida(Graphics2D g2d, int x, int y, int ancho,
                                  int vida, int vidaMaxima) {
        int maxSeguro = Math.max(1, vidaMaxima);
        g2d.setColor(new Color(52, 58, 72));
        g2d.fillRoundRect(x, y, ancho, 14, 7, 7);
        int progreso = Math.max(0, Math.min(ancho,
                (int) ((double) vida / maxSeguro * ancho)));
        g2d.setColor(vida > maxSeguro * 0.3 ? VERDE : ROJO);
        g2d.fillRoundRect(x, y, progreso, 14, 7, 7);
        g2d.setColor(TEXTO);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString(vida + " / " + maxSeguro, x + 12, y + 11);
    }

    private void actualizarEstados() {
        panelEstados.removeAll();
        panelEstados.add(crearTarjetaEstado("HEROE", jugador, AZUL));
        panelEstados.add(crearTarjetaEstado("ALIADO", jugador.getCompanero(), VERDE));
        panelEstados.add(crearTarjetaEstado("ENEMIGO", enemigo, ROJO));
        panelEstados.revalidate();
        panelEstados.repaint();
    }

    private JPanel crearTarjetaEstado(String titulo, Personaje personaje, Color color) {
        JPanel tarjeta = new JPanel(new BorderLayout(5, 2));
        tarjeta.setBackground(PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, color),
                new EmptyBorder(6, 10, 6, 10)));

        JLabel nombre = new JLabel(titulo);
        nombre.setForeground(color);
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tarjeta.add(nombre, BorderLayout.NORTH);

        if (personaje == null) {
            JLabel sinAliado = new JLabel("Sin aliado");
            sinAliado.setForeground(new Color(155, 165, 180));
            tarjeta.add(sinAliado, BorderLayout.CENTER);
        } else {
            String estadoExtra = personaje.estaVivo() ? "" : " (CAIDO)";
            String armaInfo = personaje.getHerramienta() != null
                    ? "<br><small>" + escaparHtml(personaje.getHerramienta().getNombre())
                    + " (" + personaje.getHerramienta().getDurabilidad() + "%)</small>"
                    : "";
            JLabel datos = new JLabel("<html><b>" + escaparHtml(personaje.getNombre()) + estadoExtra
                    + "</b><br>" + personaje.getVida() + " / " + personaje.getVidaMaxima() + " HP"
                    + armaInfo + "</html>");
            datos.setForeground(TEXTO);
            datos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tarjeta.add(datos, BorderLayout.CENTER);
        }
        return tarjeta;
    }

    private void atacar(ActionEvent evento) {
        if (Tramposo.estaBaneado()) {
            agregarLog("[X] Cuenta bloqueada por trampas.");
            sincronizarControles();
            return;
        }
        if (!jugador.estaVivo()) {
            agregarLog("[!] Tu héroe ha caído. Pulsa NUEVO COMBATE para revivir.");
            return;
        }
        if (!enemigo.estaVivo()) {
            agregarLog("[!] El enemigo ya fue derrotado. Cambia de sala o inicia NUEVO COMBATE.");
            return;
        }
        if (jugador.getHerramienta() == null || jugador.getHerramienta().estaRota()) {
            agregarLog("[!] Tu arma está rota. Pulsa NUEVO COMBATE para repararla.");
            return;
        }

        // Animación de embestida
        desplazamientoHeroe = 26;
        desplazamientoEnemigo = -16;

        // 1. Ataque del jugador
        int danioHeroe = jugador.atacar(enemigo);
        agregarLog("> " + jugador.getNombre() + " " + jugador.getHerramienta().grito()
                + " (-" + danioHeroe + " HP).");

        // 2. Ataque del compañero (si existe y sigue con vida)
        Personaje aliado = jugador.getCompanero();
        if (enemigo.estaVivo() && aliado != null && aliado.estaVivo()) {
            int danioAliado = aliado.atacar(enemigo);
            agregarLog("> " + aliado.getNombre() + " apoya (-" + danioAliado + " HP).");
        }

        // 3. Contraataque del enemigo
        if (enemigo.estaVivo()) {
            if (aliado != null && aliado.estaVivo() && Math.random() < 0.35) {
                int danio = enemigo.atacar(aliado);
                agregarLog("> " + enemigo.getNombre() + " golpea a " + aliado.getNombre() + " (-" + danio + " HP).");
                if (!aliado.estaVivo()) {
                    agregarLog("[!] ¡" + aliado.getNombre() + " ha caído en combate!");
                }
            } else {
                int danio = enemigo.atacar(jugador);
                agregarLog("> " + enemigo.getNombre() + " contraataca (-" + danio + " HP).");
                if (!jugador.estaVivo()) {
                    agregarLog("[DERROTA] Tu héroe ha caído.");
                }
            }
        } else {
            agregarLog("[VICTORIA] ¡" + enemigo.getNombre() + " derrotado!");
        }
        sincronizarControles();
        actualizarTodo();
    }

    private void curarJugador(ActionEvent evento) {
        if (Tramposo.estaBaneado()) {
            agregarLog("[X] Cuenta bloqueada por trampas.");
            return;
        }
        if (!jugador.estaVivo() || !enemigo.estaVivo()) {
            agregarLog("[!] El combate no está activo.");
            return;
        }
        if (pocionesRestantes <= 0) {
            agregarLog("[!] Ya no te quedan pociones en este combate.");
            return;
        }

        boolean heroeLleno = jugador.getVida() == jugador.getVidaMaxima();
        Personaje aliado = jugador.getCompanero();
        boolean aliadoLleno = (aliado == null || !aliado.estaVivo() || aliado.getVida() == aliado.getVidaMaxima());
        if (heroeLleno && aliadoLleno) {
            agregarLog("[!] Tu equipo ya está al máximo de vida.");
            return;
        }

        pocionesRestantes--;
        int curadoHeroe = jugador.curar(20);
        int curadoAliado = jugador.ayudarCompanero();
        agregarLog("> Poción usada: héroe +" + curadoHeroe + " HP"
                + (curadoAliado > 0 ? ", aliado +" + curadoAliado + " HP." : "."));

        sincronizarControles();
        actualizarTodo();
    }

    private void simularTrampa(ActionEvent evento) {
        Tramposo.detectarCheats(9999, "Interfaz Grafica");
        agregarLog("[!] Trampa detectada. Advertencias: " + Tramposo.getAdvertencias() + "/3.");
        if (Tramposo.estaBaneado()) {
            agregarLog("[X] Cuenta bloqueada por trampas.");
        }
        sincronizarControles();
        actualizarTodo();
    }

    private void cambiarSala() {
        if (Tramposo.estaBaneado()) {
            return;
        }
        int nivel = selectorSala.getSelectedIndex() + 1;
        enemigo = Moldes.crearEnemigoMazmorra(nivel);
        pocionesRestantes = POCIONES_MAXIMAS;
        agregarLog("--- Cambiaste a " + selectorSala.getSelectedItem() + " ---");
        sincronizarControles();
        actualizarTodo();
    }

    private void reiniciarCombate() {
        if (Tramposo.estaBaneado()) {
            agregarLog("[X] Tu cuenta está BANEADA. No puedes iniciar un nuevo combate.");
            sincronizarControles();
            actualizarTodo();
            return;
        }

        if (!modoPersonalizado) {
            inicializarDemo();
        } else {
            jugador.restaurarCompleto(); // Revive incluso desde 0 HP y repara arma
            if (jugador.getCompanero() != null) {
                jugador.getCompanero().restaurarCompleto();
            }
        }
        int nivel = selectorSala != null ? selectorSala.getSelectedIndex() + 1 : 1;
        enemigo = Moldes.crearEnemigoMazmorra(nivel);
        pocionesRestantes = POCIONES_MAXIMAS;
        consolaLog.setText("> Nuevo combate iniciado contra " + enemigo.getNombre() + ".");
        sincronizarControles();
        actualizarTodo();
    }

    private void sincronizarControles() {
        boolean baneado = Tramposo.estaBaneado();
        boolean combateActivo = !baneado && jugador.estaVivo() && enemigo.estaVivo();
        if (btnAtacar != null) {
            btnAtacar.setEnabled(combateActivo);
        }
        if (btnCurar != null) {
            btnCurar.setText("POCION (" + pocionesRestantes + ")");
            btnCurar.setEnabled(combateActivo && pocionesRestantes > 0);
        }
        if (btnTrampa != null) {
            btnTrampa.setEnabled(!baneado);
        }
    }

    private void actualizarTodo() {
        actualizarEstados();
        actualizarEstadoTitulo();
        panelLienzo.repaint();
    }

    private void actualizarEstadoTitulo() {
        if (estadoTitulo == null) {
            return;
        }
        if (Tramposo.estaBaneado()) {
            estadoTitulo.setText("ESTADO: BLOQUEADO (3/3)");
            estadoTitulo.setForeground(ROJO);
        } else if (!jugador.estaVivo()) {
            estadoTitulo.setText("ESTADO: DERROTA");
            estadoTitulo.setForeground(ROJO);
        } else if (!enemigo.estaVivo()) {
            estadoTitulo.setText("ESTADO: VICTORIA");
            estadoTitulo.setForeground(VERDE);
        } else {
            estadoTitulo.setText("ESTADO: EN COMBATE | ADV: " + Tramposo.getAdvertencias() + "/3");
            estadoTitulo.setForeground(DORADO);
        }
    }

    private void agregarLog(String texto) {
        consolaLog.append("\n" + texto);
        SwingUtilities.invokeLater(() ->
                consolaLog.setCaretPosition(consolaLog.getDocument().getLength()));
    }

    private static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void iniciarAnimacion() {
        animacionTimer = new Timer(120, e -> {
            frameAnimacion++;
            if (desplazamientoHeroe > 0) {
                desplazamientoHeroe = Math.max(0, desplazamientoHeroe - 6);
            }
            if (desplazamientoEnemigo < 0) {
                desplazamientoEnemigo = Math.min(0, desplazamientoEnemigo + 4);
            }
            panelLienzo.repaint();
        });
        animacionTimer.start();
    }
}