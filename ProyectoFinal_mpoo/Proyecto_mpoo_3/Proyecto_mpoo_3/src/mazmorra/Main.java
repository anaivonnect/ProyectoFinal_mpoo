package mazmorra;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import trampas.TrampasDeAcceso;
import trampas.Tramposo;

public class Main {
    private static final String ARCHIVO_USUARIOS = "usuarios.txt";
    private static final Pattern PATRON_USUARIO = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,15}$");
    private static final Pattern PATRON_NOMBRE_PERSONAJE = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ]+( [A-Za-zÁÉÍÓÚáéíóúÑñ]+)*$");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, String> usuariosRegistrados = cargarUsuarios();
        String usuarioActual = "Invitado";
        Personaje jugador = null;

        System.out.println("==========================================");
        System.out.println("   BIENVENIDO A LA ORDEN DE LAS SOMBRAS   ");
        System.out.println("==========================================");

        boolean salir = false;
        while (!salir) {
            String estadoCuenta = usuarioActual.equals("Invitado")
                    ? ""
                    : (Tramposo.estaBaneado() ? " [BANEADO]" : " [Adv: " + Tramposo.getAdvertencias() + "/3]");

            System.out.println("\n--- MENÚ PRINCIPAL (Usuario: " + usuarioActual + estadoCuenta + ") ---");
            System.out.println("1. Iniciar sesión / Cambiar cuenta");
            System.out.println("2. Selección de Personaje y Arma");
            System.out.println("3. Ver estado, Comparar y Posada");
            System.out.println("4. Iniciar aventura (Mazmorra por Turnos)");
            System.out.println("5. Gestionar Compañero de Grupo");
            System.out.println("6. Zona de Pruebas, Sala Oculta y Trampas");
            System.out.println("7. Abrir Ventana de Batalla (Interfaz Gráfica)");
            System.out.println("8. Salir");
            System.out.print("Elige una opción (1-8): ");

            int opcion = leerEntero(scanner, 1, 8);

            if (opcion != 1 && opcion != 8 && usuarioActual.equals("Invitado")) {
                System.out.println("  [X] ACCESO DENEGADO: Debes iniciar sesión primero (Opción 1) para poder jugar.");
                continue;
            }

            switch (opcion) {
                case 1:
                    String nuevoUsuario = gestionarAcceso(scanner, usuariosRegistrados);
                    if (!nuevoUsuario.equalsIgnoreCase(usuarioActual)) {
                        VentanaJuego.cerrarSiAbierta();
                        jugador = null; // Evita heredar personaje entre cuentas distintas
                    }
                    usuarioActual = nuevoUsuario;
                    Tramposo.establecerUsuario(usuarioActual.equals("Invitado") ? null : usuarioActual);
                    if (!usuarioActual.equals("Invitado") && Tramposo.estaBaneado()) {
                        System.out.println("  [X] AVISO: Esta cuenta se encuentra BANEADA por trampas (3/3 advertencias).");
                    }
                    break;

                case 2:
                    if (Tramposo.estaBaneado()) {
                        System.out.println("  [X] ACCESO DENEGADO: El usuario está BANEADO por uso de trampas.");
                        break;
                    }

                    System.out.println("\n--- SELECCIÓN DE RAZA ---");
                    System.out.println("1. Enano   (130 HP | +4 daño | Armadura natural -3 daño recibido)");
                    System.out.println("2. Humano  (100 HP | +5 daño | Recibe +3 HP extra al curarse)");
                    System.out.println("3. Elfo    ( 90 HP | +7 daño | +3 daño extra con Arco)");
                    System.out.println("4. Mago    ( 80 HP | x1.8 daño con Bastón)");
                    System.out.println("5. Orco    (140 HP | +7 daño | Furia berserker: recibe +2 daño)");
                    System.out.println("6. Personalizado (Configuras Vida 50-200 y Defensa 0-15)");
                    System.out.print("Elige tu raza (1-6): ");
                    int tipoRaza = leerEntero(scanner, 1, 6);

                    String nombreP = leerNombrePersonaje(scanner, "Nombre de tu personaje (solo letras, 2-20 caracteres): ");

                    System.out.println("\n--- SELECCIÓN DE ARMA ---");
                    System.out.println("1. Espada (20 daño | -1 durabilidad por golpe)");
                    System.out.println("2. Arco   (2 flechas x 11 = 22 daño | -2 durabilidad por golpe)");
                    System.out.println("3. Bastón (14 daño | -1 durabilidad | Sinergia con Mago)");
                    System.out.print("Elige tu arma (1-3): ");
                    int tipoArma = leerEntero(scanner, 1, 3);

                    Herramienta armaElegida = Moldes.crearArmaBasica(tipoArma);
                    Personaje nuevoPersonaje = null;

                    switch (tipoRaza) {
                        case 1: nuevoPersonaje = new Enano(nombreP); break;
                        case 2: nuevoPersonaje = new Humano(nombreP); break;
                        case 3: nuevoPersonaje = new Elfo(nombreP); break;
                        case 4: nuevoPersonaje = new Mago(nombreP); break;
                        case 5: nuevoPersonaje = new Orco(nombreP); break;
                        case 6:
                            String subtitulo = leerNombrePersonaje(scanner, "Título o clase personalizada (2-20 letras): ");
                            System.out.print("Ingresa puntos de Vida Inicial (Rango legal 50 - 200): ");
                            int vidaCustom = leerEntero(scanner);
                            System.out.print("Ingresa puntos de Defensa (Rango legal 0 - 15): ");
                            int defCustom = leerEntero(scanner);

                            boolean trampaVida = Tramposo.detectarCheats(vidaCustom, 50, 200, "Vida Inicial");
                            boolean trampaDef = Tramposo.detectarCheats(defCustom, 0, 15, "Defensa");

                            if (Tramposo.estaBaneado()) {
                                System.out.println("  [X] Creación cancelada: Tu cuenta ha sido BANEADA.");
                                break;
                            }

                            if (trampaVida || trampaDef) {
                                System.out.println("  [!] Se aplicarán valores regulados por el servidor (Vida: 110, Defensa: 5).");
                                vidaCustom = 110;
                                defCustom = 5;
                            }
                            nuevoPersonaje = new Personalizado(nombreP, subtitulo, vidaCustom, defCustom);
                            break;
                        default:
                            nuevoPersonaje = new Humano(nombreP);
                    }

                    if (nuevoPersonaje != null) {
                        nuevoPersonaje.equipar(armaElegida);
                        jugador = nuevoPersonaje;
                        System.out.println("  [OK] ¡Personaje creado con éxito! -> " + jugador);
                    }
                    break;

                case 3:
                    if (jugador == null) {
                        System.out.println("  [!] Primero debes crear tu personaje en la opción 2.");
                    } else {
                        System.out.println("\n--- ESTADO ACTUAL ---");
                        System.out.println("Cuenta: " + usuarioActual + " | Advertencias: " + Tramposo.getAdvertencias() + "/3");
                        System.out.println("Héroe: " + jugador + (jugador.estaVivo() ? "" : " [DERROTADO]"));
                        if (jugador instanceof Personalizado) {
                            System.out.println("Defensa personalizada: " + ((Personalizado) jugador).getDefensa());
                        }
                        if (jugador.getHerramienta() != null) {
                            Herramienta h = jugador.getHerramienta();
                            System.out.println("Arma equipada: " + h + " | Grito: " + h.grito()
                                    + (h.estaRota() ? " [ROTA]" : ""));
                        }
                        if (jugador.getCompanero() != null) {
                            System.out.println("Compañero activo: " + jugador.getCompanero());
                            Comparar.compararPersonajes(jugador, jugador.getCompanero());
                        } else {
                            System.out.println("Comparativa contra enemigo estándar de mazmorra:");
                            Comparar.compararPersonajes(jugador, Moldes.crearEnemigoMazmorra(1));
                        }

                        boolean necesitaDescanso = jugador.getVida() < jugador.getVidaMaxima()
                                || (jugador.getHerramienta() != null
                                && jugador.getHerramienta().getDurabilidad() < Herramienta.DURABILIDAD_MAXIMA)
                                || (jugador.getCompanero() != null
                                && jugador.getCompanero().getVida() < jugador.getCompanero().getVidaMaxima());

                        if (necesitaDescanso && !Tramposo.estaBaneado()) {
                            if (leerConfirmacion(scanner, "¿Deseas descansar en la posada (restaura vida y repara armas)? (S/N): ")) {
                                jugador.restaurarCompleto();
                                if (jugador.getCompanero() != null) {
                                    jugador.getCompanero().restaurarCompleto();
                                }
                                System.out.println("  [OK] Tú y tu equipo han recuperado toda su salud y durabilidad.");
                            }
                        }
                    }
                    break;

                case 4:
                    if (Tramposo.estaBaneado()) {
                        System.out.println("  [X] No puedes entrar a la mazmorra: Cuenta BANEADA.");
                        break;
                    }
                    if (jugador == null) {
                        System.out.println("  [!] Debes crear un personaje antes de iniciar la aventura (Opción 2).");
                        break;
                    }
                    if (!jugador.estaVivo()) {
                        System.out.println("  [!] Tu personaje está derrotado (0 HP).");
                        if (leerConfirmacion(scanner, "¿Deseas descansar en la posada para revivir antes de combatir? (S/N): ")) {
                            jugador.restaurarCompleto();
                            if (jugador.getCompanero() != null) {
                                jugador.getCompanero().restaurarCompleto();
                            }
                            System.out.println("  [OK] ¡Listo para volver al combate!");
                        } else {
                            break;
                        }
                    }

                    System.out.println("\n--- NIVELES DE LA MAZMORRA ---");
                    System.out.println("1. Sala 1: Goblin Salvaje (Orco - Básico)");
                    System.out.println("2. Sala 2: Hechicero Sombrío (Mago - Medio)");
                    System.out.println("3. Sala Final: Señor de las Sombras (Jefe Oscuro)");
                    System.out.print("Elige sala (1-3): ");
                    int sala = leerEntero(scanner, 1, 3);

                    Personaje enemigo = Moldes.crearEnemigoMazmorra(sala);
                    Comparar.compararPersonajes(jugador, enemigo);

                    boolean huir = false;
                    int turno = 1;
                    int pociones = 3;

                    while (jugador.estaVivo() && enemigo.estaVivo() && !huir && !Tramposo.estaBaneado()) {
                        Personaje aliado = jugador.getCompanero();
                        System.out.println("\n=== TURNO " + turno + " ===");
                        System.out.println("Tú: " + jugador + (jugador.getHerramienta() != null
                                ? " | Arma Durabilidad: " + jugador.getHerramienta().getDurabilidad() : ""));
                        if (aliado != null) {
                            System.out.println("Aliado: " + aliado + (aliado.estaVivo() ? "" : " [CAÍDO]"));
                        }
                        System.out.println("Enemigo: " + enemigo);

                        System.out.println("\n1. Atacar");
                        System.out.println("2. Apoyo del compañero (Tu aliado te cura y ataca)");
                        System.out.println("3. Usar poción de salud (+20 HP | Restantes: " + pociones + ")");
                        System.out.println("4. Huir");
                        System.out.print("Elige acción (1-4): ");
                        int accion = leerEntero(scanner, 1, 4);

                        if (accion == 1) {
                            if (jugador.getHerramienta() == null || jugador.getHerramienta().estaRota()) {
                                System.out.println("  [!] ¡Tu arma está rota! Huye y repárala en la Posada (Opción 3) o Sala Oculta.");
                                continue;
                            }
                            int danio = jugador.atacar(enemigo);
                            System.out.println("  -> " + jugador.getNombre() + " " + jugador.getHerramienta().grito()
                                    + " e inflige " + danio + " de daño a " + enemigo.getNombre() + ".");
                        } else if (accion == 2) {
                            if (aliado == null) {
                                System.out.println("  [!] No tienes compañero asignado. Elige otra acción.");
                                continue;
                            }
                            if (!aliado.estaVivo()) {
                                System.out.println("  [!] " + aliado.getNombre() + " cayó en combate y no puede ayudarte.");
                                continue;
                            }
                            int curado = aliado.ayudarCompanero(); // El aliado cura al jugador
                            int danioAliado = aliado.atacar(enemigo);
                            if (curado > 0) {
                                System.out.println("  -> " + aliado.getNombre() + " te curó +" + curado + " HP.");
                            } else {
                                System.out.println("  -> Ya tenías tu vida al máximo.");
                            }
                            System.out.println("  -> " + aliado.getNombre() + " atacó a " + enemigo.getNombre()
                                    + " causando " + danioAliado + " de daño.");
                        } else if (accion == 3) {
                            if (pociones <= 0) {
                                System.out.println("  [!] No te quedan pociones en este combate.");
                                continue;
                            }
                            if (jugador.getVida() == jugador.getVidaMaxima()) {
                                System.out.println("  [!] Ya tienes la vida al máximo. No gastes tu poción.");
                                continue;
                            }
                            pociones--;
                            int curado = jugador.curar(20);
                            System.out.println("  -> Bebiste una poción y recuperaste +" + curado + " HP.");
                        } else {
                            huir = true;
                            System.out.println("  -> Te retiraste estratégicamente del combate.");
                        }

                        // Contraataque del enemigo
                        if (enemigo.estaVivo() && !huir) {
                            if (aliado != null && aliado.estaVivo() && Math.random() < 0.35) {
                                int danioEnemigo = enemigo.atacar(aliado);
                                System.out.println("  -> " + enemigo.getNombre() + " atacó a " + aliado.getNombre()
                                        + " por " + danioEnemigo + " de daño.");
                                if (!aliado.estaVivo()) {
                                    System.out.println("  [!] ¡" + aliado.getNombre() + " ha caído en combate!");
                                }
                            } else {
                                int danioEnemigo = enemigo.atacar(jugador);
                                System.out.println("  -> " + enemigo.getNombre() + " contraatacó a " + jugador.getNombre()
                                        + " por " + danioEnemigo + " de daño.");
                            }
                        }
                        turno++;
                    }

                    System.out.println("\n--- RESULTADO DEL ENCUENTRO ---");
                    if (!enemigo.estaVivo()) {
                        System.out.println("  [VICTORIA] ¡Has derrotado a " + enemigo.getNombre() + "!");
                    } else if (!jugador.estaVivo()) {
                        System.out.println("  [DERROTA] " + jugador.getNombre() + " ha caído en la mazmorra.");
                    } else {
                        System.out.println("  [RETIRADA] Saliste con vida del encuentro.");
                    }
                    System.out.println("Estado final -> " + jugador);
                    break;

                case 5:
                    if (Tramposo.estaBaneado()) {
                        System.out.println("  [X] ACCESO DENEGADO: Cuenta BANEADA.");
                        break;
                    }
                    if (jugador == null) {
                        System.out.println("  [!] Primero debes crear tu personaje principal (Opción 2).");
                    } else {
                        System.out.println("\n--- GESTIÓN DE COMPAÑERO DE GRUPO ---");
                        if (jugador.getCompanero() == null) {
                            System.out.println("1. Reclutar a un Mago aliado (Gandalf el Gris)");
                            System.out.println("2. Reclutar a un Enano aliado (Thorin)");
                            System.out.println("3. Reclutar a un Elfo aliado (Legolas)");
                            System.out.println("4. Volver al menú principal");
                            System.out.print("Elige una opción (1-4): ");
                            int subOpcion = leerEntero(scanner, 1, 4);

                            Personaje aliado = Moldes.crearAliado(subOpcion);
                            if (aliado != null) {
                                jugador.setCompanero(aliado);
                                aliado.setCompanero(jugador);
                                System.out.println("  [OK] ¡Compañero reclutado con éxito!");
                                System.out.println("  -> Aliado en equipo: " + aliado);
                            }
                        } else {
                            Personaje aliadoActual = jugador.getCompanero();
                            System.out.println("Tu compañero actual es: " + aliadoActual
                                    + (aliadoActual.estaVivo() ? "" : " [CAÍDO]"));
                            System.out.println("1. Curar a tu compañero (+10 HP)");
                            System.out.println("2. Pedirle al compañero que te cure (+10 HP)");
                            System.out.println("3. Quitar compañero del grupo");
                            System.out.println("4. Volver al menú principal");
                            System.out.print("Elige una acción (1-4): ");
                            int accionCompanero = leerEntero(scanner, 1, 4);

                            if (accionCompanero == 1) {
                                int curado = jugador.ayudarCompanero();
                                if (curado > 0) {
                                    System.out.println("  [OK] Curaste a " + aliadoActual.getNombre() + " (+" + curado + " HP).");
                                } else {
                                    System.out.println("  [!] No se pudo curar (está al máximo o cayó en combate; usa la Posada en Opción 3).");
                                }
                            } else if (accionCompanero == 2) {
                                int curado = aliadoActual.ayudarCompanero();
                                if (curado > 0) {
                                    System.out.println("  [OK] " + aliadoActual.getNombre() + " te curó +" + curado + " HP.");
                                } else {
                                    System.out.println("  [!] No se pudo curar (ya estás al máximo o tu aliado cayó en combate).");
                                }
                            } else if (accionCompanero == 3) {
                                aliadoActual.setCompanero(null);
                                jugador.setCompanero(null);
                                System.out.println("  [OK] Tu compañero se ha marchado. Ahora viajas solo.");
                            }
                        }
                    }
                    break;

                case 6:
                    System.out.println("\n--- AUDITORÍA DE TRAMPAS Y SALA OCULTA ---");
                    System.out.println("Cuenta: " + usuarioActual);
                    System.out.println("Advertencias actuales: " + Tramposo.getAdvertencias() + "/3");
                    System.out.println("Estado: " + (Tramposo.estaBaneado() ? "BANEADO [X]" : "Cuenta Limpia [OK]"));
                    System.out.println("1. Entrar a la Sala Oculta (Fuente de Reparación y Curación)");
                    System.out.println("2. Simular intento de Cheat (Probar detector de trampas)");
                    System.out.println("3. Volver al menú principal");
                    System.out.print("Elige opción (1-3): ");
                    int opT = leerEntero(scanner, 1, 3);

                    if (opT == 1) {
                        if (jugador == null) {
                            System.out.println("  [!] Crea un personaje primero (Opción 2) para probar las restricciones.");
                        } else if (TrampasDeAcceso.validarAccesoSalaOculta(jugador)) {
                            int curado = jugador.curar(25);
                            if (jugador.getHerramienta() != null) {
                                jugador.getHerramienta().reparar();
                            }
                            System.out.println("  -> Encontraste la Fuente Sombría: +" + curado
                                    + " HP y durabilidad de tu arma restaurada al 100%.");
                        }
                    } else if (opT == 2) {
                        System.out.print("Ingresa un valor numérico a auditar (ilegal si < 0 o > 999, ej. 9999): ");
                        int val = leerEntero(scanner);
                        if (!Tramposo.detectarCheats(val, "Auditoría de Consola")) {
                            System.out.println("  [OK] Valor legal (" + val + "). No se acumularon advertencias.");
                        }
                    }
                    break;

                case 7:
                    if (Tramposo.estaBaneado()) {
                        System.out.println("  [X] ACCESO DENEGADO: Tu cuenta está BANEADA y no puede abrir la Ventana de Batalla.");
                        break;
                    }
                    if (jugador == null) {
                        System.out.println("  [!] Primero debes crear tu personaje en la opción 2 (y reclutar compañero en la 5 si deseas).");
                        break;
                    }
                    VentanaJuego.abrirUnica(jugador, usuarioActual);
                    System.out.println("  [OK] Ventana gráfica lista con tu personaje: " + jugador.getNombre());
                    break;

                case 8:
                    salir = true;
                    VentanaJuego.cerrarSiAbierta();
                    System.out.println("¡Gracias por jugar a La Orden de las Sombras! Saliendo...");
                    break;

                default:
                    System.out.println("Opción inválida. Intenta de nuevo.");
            }
        }
        scanner.close();
    }

    // --- VALIDACIONES DE NICKNAME Y NOMBRES (PÚBLICAS PARA PRUEBAS UNITARIAS) ---

    public static boolean esUsuarioValido(String usuario) {
        if (usuario == null) {
            return false;
        }
        String limpio = usuario.trim();
        if (limpio.equalsIgnoreCase("invitado")
                || limpio.equalsIgnoreCase("admin")
                || limpio.equalsIgnoreCase("root")) {
            return false;
        }
        return PATRON_USUARIO.matcher(limpio).matches();
    }

    public static boolean esNombrePersonajeValido(String nombre) {
        if (nombre == null) {
            return false;
        }
        String limpio = normalizarEspacios(nombre);
        if (limpio.length() < 2 || limpio.length() > 20) {
            return false;
        }
        return PATRON_NOMBRE_PERSONAJE.matcher(limpio).matches();
    }

    public static String normalizarEspacios(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    // --- LÓGICA DE INICIO DE SESIÓN Y REGISTRO ---

    private static String gestionarAcceso(Scanner scanner, Map<String, String> usuarios) {
        System.out.println("\n--- INICIAR SESIÓN ---");
        System.out.print("Ingresa tu nombre de usuario (3-16 caracteres, inicia con letra, sin espacios): ");
        String nombre = scanner.nextLine().trim();

        while (!esUsuarioValido(nombre)) {
            System.out.print("Usuario inválido (debe iniciar con letra, 3-16 letras/números/_ y no ser 'Invitado'): ");
            nombre = scanner.nextLine().trim();
        }

        String clave = nombre.toLowerCase();

        if (usuarios.containsKey(clave)) {
            int intentos = 0;
            while (intentos < 3) {
                System.out.print("Ingresa tu contraseña: ");
                String pass = scanner.nextLine().trim();
                if (usuarios.get(clave).equals(pass)) {
                    System.out.println("  [OK] Sesión iniciada con éxito. ¡Bienvenido de vuelta, " + nombre + "!");
                    return nombre;
                } else {
                    intentos++;
                    System.out.println("  [X] Contraseña incorrecta. Intentos restantes: " + (3 - intentos));
                }
            }
            System.out.println("  [!] Demasiados intentos fallidos. Continuarás como Invitado.");
            return "Invitado";
        } else {
            System.out.println("  [!] El usuario '" + nombre + "' no está registrado.");
            if (leerConfirmacion(scanner, "¿Deseas registrarlo ahora? (S/N): ")) {
                System.out.print("Crea una contraseña para " + nombre + " (mínimo 4 caracteres, sin ':'): ");
                String nuevaPass = scanner.nextLine().trim();
                while (nuevaPass.length() < 4 || nuevaPass.contains(":")) {
                    System.out.print("Contraseña inválida (mínimo 4 caracteres y sin ':'): ");
                    nuevaPass = scanner.nextLine().trim();
                }
                usuarios.put(clave, nuevaPass);
                guardarUsuarios(usuarios);
                System.out.println("  [OK] Usuario registrado e iniciado sesión como: " + nombre);
                return nombre;
            } else {
                System.out.println("  [!] Registro cancelado. Continuarás como Invitado.");
                return "Invitado";
            }
        }
    }

    private static Map<String, String> cargarUsuarios() {
        Map<String, String> mapa = new HashMap<>();
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) {
            return mapa;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2) {
                    String user = partes[0].trim().toLowerCase();
                    String pass = partes[1].trim();
                    if (!user.isEmpty() && !pass.isEmpty()) {
                        mapa.put(user, pass);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] No se pudo leer el archivo de usuarios: " + e.getMessage());
        }
        return mapa;
    }

    private static void guardarUsuarios(Map<String, String> usuarios) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_USUARIOS))) {
            for (Map.Entry<String, String> entry : usuarios.entrySet()) {
                pw.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.out.println("  [!] Error al guardar el usuario: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE LECTURA SEGURA ---

    private static String leerNombrePersonaje(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        String entrada = normalizarEspacios(scanner.nextLine());
        while (!esNombrePersonajeValido(entrada)) {
            System.out.print("Nombre inválido (usa solo letras y espacios, entre 2 y 20 caracteres): ");
            entrada = normalizarEspacios(scanner.nextLine());
        }
        return entrada;
    }

    private static boolean leerConfirmacion(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) {
                return true;
            }
            if (r.equals("n") || r.equals("no")) {
                return false;
            }
            System.out.println("  [!] Respuesta no válida. Escribe 'S' para sí o 'N' para no.");
        }
    }

    private static int leerEntero(Scanner scanner) {
        while (true) {
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Ingresa un número entero: ");
            }
        }
    }

    private static int leerEntero(Scanner scanner, int minimo, int maximo) {
        while (true) {
            int valor = leerEntero(scanner);
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            System.out.print("Opción fuera de rango (" + minimo + "-" + maximo + "): ");
        }
    }
}