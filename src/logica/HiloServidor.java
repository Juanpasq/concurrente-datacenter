package logica;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HiloServidor extends Thread {
    private String nombre;
    private MonitorEnergia monitor;
    private JLabel etiqueta;
    private int prioridad; // 1 = Alta (PROD), 2 = Baja (NAS/LAB)
    private gui.VentanaPrincipal ventana; // <--- PASO A: Declarar la variable
    

    public HiloServidor(String nombre, MonitorEnergia monitor, JLabel etiqueta, int prioridad) {
        this.nombre = nombre;
        this.monitor = monitor;
        this.etiqueta = etiqueta;
        this.prioridad = prioridad;
       // this.ventana = ventana;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); 
                String fuente = monitor.getFuenteActual();
                boolean debeEstarPrendido = false;

                // 1. Lógica de encendido (La que ya y funciona)
                if (fuente.equals("RED") || fuente.equals("GENERADOR")) {
                    debeEstarPrendido = true;
                } else if (fuente.equals("UPS")) {
                    debeEstarPrendido = (prioridad == 1); 
                }

                if (gui.VentanaPrincipal.emergenciaTermica) {
                    debeEstarPrendido = false;
                }
                // DETECTOR DE ENCENDIDO (Lo que manda el mensaje al Log)
                // Si el cuadradito está ROJO y la lógica dice que ahora debe estar VERDE
                if (etiqueta.getBackground().equals(java.awt.Color.RED) && debeEstarPrendido) {
                    
                    // Llamamos directamente al log estático de tu ventana
                    //gui.VentanaPrincipal.logEventos.append("✅ Enviando señal ON: " + nombre + "\n");
                    gui.VentanaPrincipal.logEventos.append("✅ Server ON: " + nombre + "\n");

                    
                    // (Opcional) Esto lo dejamos para que lo sigas viendo en Eclipse si querés
                    System.out.println("DEBUG: El servidor " + nombre + " se encendió.");
                }
                
             // --- DETECTOR DE APAGADO  ---
                // Si el cuadradito está VERDE pero la lógica dice que debe APAGARSE
                if (etiqueta.getBackground().equals(java.awt.Color.GREEN) && !debeEstarPrendido) {
                    if (gui.VentanaPrincipal.logEventos != null) {
                        gui.VentanaPrincipal.logEventos.append("❌ Server OFF: " + nombre + "\n");
                    }
                }

                actualizarEstadoVisual(debeEstarPrendido);

            } catch (InterruptedException e) {
                break; 
            }
        }
    }

    private void actualizarEstadoVisual(boolean prendido) {
        SwingUtilities.invokeLater(() -> {
            if (prendido) {
                etiqueta.setBackground(Color.GREEN);
                etiqueta.setText("🖥️  "+nombre + " [ON]");
                
            } else {
                etiqueta.setBackground(Color.RED);
                etiqueta.setText("🖥️  "+ nombre + " [OFF]");
            }
        });
    }
}