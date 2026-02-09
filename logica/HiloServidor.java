package logica;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HiloServidor extends Thread {
    private String nombre;
    private MonitorEnergia monitor;
    private JLabel etiqueta;
    private int prioridad; // 1 = Alta (PROD), 2 = Baja (NAS/LAB)
    private gui.VentanaPrincipal ventana; //Declarar la variable
    

    public HiloServidor(String nombre, MonitorEnergia monitor, JLabel etiqueta, int prioridad) {
        this.nombre = nombre;
        this.monitor = monitor;
        this.etiqueta = etiqueta;
        this.prioridad = prioridad;
       // this.ventana = ventana;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); 
                String fuente = monitor.getFuenteActual();
                boolean debeEstarPrendido = false;

                // Logica de encendido
                if (fuente.equals("RED") || fuente.equals("GENERADOR")) {
                    debeEstarPrendido = true;
                } else if (fuente.equals("UPS")) {
                    debeEstarPrendido = (prioridad == 1); 
                }

                // Filtro por emergencia térmica
                if (gui.VentanaPrincipal.emergenciaTermica) {
                    debeEstarPrendido = false;
                }

                // DETECTOR DE ENCENDIDO
                if (etiqueta.getBackground().equals(java.awt.Color.RED) && debeEstarPrendido) {
                    gui.VentanaPrincipal.logEventos.append("✅ Server ON: " + nombre + "\n");
                    System.out.println("DEBUG: El servidor " + nombre + " se encendió.");
                }
                
                // DETECTOR DE APAGADO
                if (etiqueta.getBackground().equals(java.awt.Color.GREEN) && !debeEstarPrendido) {
                    gui.VentanaPrincipal.logEventos.append("❌ Server OFF: " + nombre + "\n");
                    
                    // Si la prioridad es 1, sabemos que es PROD sin importar el nombre
                    if (this.prioridad == 1) { 
                        gui.VentanaPrincipal.contadorApagadosProd++;
                        System.out.println("DEBUG: Se sumó 1 al contador. Total: " + gui.VentanaPrincipal.contadorApagadosProd);
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