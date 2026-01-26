package logica;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HiloAire extends Thread {
    private String nombre;
    private MonitorEnergia monitor;
    private JLabel etiqueta;
    private int tipo; // 1 = Principal, 2 = Refuerzo

    public HiloAire(String nombre, MonitorEnergia monitor, JLabel etiqueta, int tipo) {
        this.nombre = nombre;
        this.monitor = monitor;
        this.etiqueta = etiqueta;
        this.tipo = tipo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                
                boolean hayRed = monitor.isRedElectricaActiva();
                int nafta = monitor.getNivelNafta();
                boolean encendido = false;

                // 1. Lógica según el tipo de Aire
                if (tipo == 1) {
                    // Aire 1: Funciona con Red o Generador (nafta)
                    if (hayRed || nafta > 0) encendido = true;
                } else {
                    // Aire 2: Solo funciona con Red Eléctrica
                    if (hayRed) encendido = true;
                }

                // 2. LÓGICA DE APAGADO MANUAL (Filtro final)
                // Si es el Aire 1 y apretamos el botón de "roto", lo forzamos a apagarse
                if (nombre.equals("❄️ AIRE 1") && gui.VentanaPrincipal.aire1Roto) {
                    encendido = false; 
                }
                if (tipo == 1 && gui.VentanaPrincipal.aire1Roto) {
                    encendido = false; // Forzamos apagado
                }

                // 3. ACTUALIZACIÓN VISUAL
                // Usamos solo el método que reconoce tu clase (actualizarVisual)
                // y le pasamos la variable 'encendido' que ya tiene toda la lógica
                //
                actualizarVisual(encendido);

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void actualizarVisual(boolean on) {
        SwingUtilities.invokeLater(() -> {
            if (on) {
                etiqueta.setBackground(new Color(173, 216, 230));
                etiqueta.setText("❄  " + nombre + " [ON]");
            } else {
                etiqueta.setBackground(Color.RED);
                etiqueta.setText("❄  " + nombre + " [OFF]");
            }
        });
    }
}