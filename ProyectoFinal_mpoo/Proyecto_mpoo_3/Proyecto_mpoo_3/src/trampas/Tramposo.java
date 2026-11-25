package trampas;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Tramposo {
    private static final String ARCHIVO_SANCIONES = "sanciones.txt";
    private static final String USUARIO_GLOBAL = "_global";
    public static final int LIMITE_ADVERTENCIAS = 3;

    private static final Map<String, Integer> advertenciasPorUsuario = cargarSanciones();
    private static String usuarioActivo = USUARIO_GLOBAL;

    public static void establecerUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            usuarioActivo = USUARIO_GLOBAL;
        } else {
            usuarioActivo = usuario.trim().toLowerCase();
            advertenciasPorUsuario.putIfAbsent(usuarioActivo, 0);
        }
    }

    public static boolean detectarCheats(int valorIngresado, String nombreAtributo) {
        return detectarCheats(valorIngresado, 0, 999, nombreAtributo);
    }

    public static boolean detectarCheats(int valorIngresado, int minLegal, int maxLegal, String nombreAtributo) {
        if (valorIngresado > maxLegal || valorIngresado < minLegal) {
            int actuales = getAdvertencias();
            int nuevas = Math.min(LIMITE_ADVERTENCIAS, actuales + 1);
            advertenciasPorUsuario.put(usuarioActivo, nuevas);
            guardarSancionesSiAplica();

            System.out.println("\n  [!] ¡ALERTA DE TRAMPA DETECTADA!");
            System.out.println("  -> Valor ilegal (" + valorIngresado + ") en " + nombreAtributo
                    + " (rango permitido: " + minLegal + " - " + maxLegal + ")");
            System.out.println("  -> Advertencias acumuladas: " + nuevas + "/" + LIMITE_ADVERTENCIAS);

            if (nuevas >= LIMITE_ADVERTENCIAS) {
                System.out.println("  [X] ¡BANEADO! La cuenta ha sido bloqueada por intentos repetidos de trampa.");
            }
            return true;
        }
        return false;
    }

    public static boolean estaBaneado() {
        return getAdvertencias() >= LIMITE_ADVERTENCIAS;
    }

    public static void reiniciarAdvertencias() {
        advertenciasPorUsuario.put(usuarioActivo, 0);
        guardarSancionesSiAplica();
    }

    public static int getAdvertencias() {
        return advertenciasPorUsuario.getOrDefault(usuarioActivo, 0);
    }

    private static Map<String, Integer> cargarSanciones() {
        Map<String, Integer> mapa = new HashMap<>();
        File archivo = new File(ARCHIVO_SANCIONES);
        if (!archivo.exists()) {
            return mapa;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2) {
                    try {
                        int adv = Math.max(0, Math.min(LIMITE_ADVERTENCIAS, Integer.parseInt(partes[1].trim())));
                        mapa.put(partes[0].trim().toLowerCase(), adv);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] No se pudo leer el archivo de sanciones: " + e.getMessage());
        }
        return mapa;
    }

    private static void guardarSancionesSiAplica() {
        if (USUARIO_GLOBAL.equals(usuarioActivo)) {
            return; // Evita ensuciar archivos durante pruebas unitarias aisladas
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_SANCIONES))) {
            for (Map.Entry<String, Integer> entry : advertenciasPorUsuario.entrySet()) {
                if (!USUARIO_GLOBAL.equals(entry.getKey())) {
                    pw.println(entry.getKey() + ":" + entry.getValue());
                }
            }
        } catch (IOException e) {
            System.out.println("  [!] Error al guardar sanciones: " + e.getMessage());
        }
    }
}