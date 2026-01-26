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

                if (tipo == 1) {
                    // Aire 1: Red o Generador
                    if (hayRed || nafta > 0) encendido = true;
                } else {
                    // Aire 2: Solo Red
                    if (hayRed) encendido = true;
                }

                actualizarVisual(encendido);
            } catch (InterruptedException e) {}
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