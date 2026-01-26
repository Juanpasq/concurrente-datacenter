package logica;

import gui.VentanaPrincipal;
import javax.swing.SwingUtilities;

public class MonitorEnergia {
    
    // Variables de estado (Recursos compartidos)
    private boolean redElectricaActiva = true;
    private int nivelNafta = 10; // 10 unidades
    private int nivelUps = 4;    // 4 unidades (100%)
    private int conteoVacioGenerador = 0;
    private int conteoVacioUps = 0;
    
    // Referencia a la ventana para actualizarla
    private VentanaPrincipal ventana;

    public MonitorEnergia(VentanaPrincipal ventana) {
        this.ventana = ventana;
    }

    // --- MÉTODOS SINCRONIZADOS (Solo un hilo entra a la vez) ---

    public synchronized void setRedElectrica(boolean estado) {
        this.redElectricaActiva = estado;
        // Notificamos a los hilos que algo cambió
        notifyAll();
    }

    public synchronized boolean isRedElectricaActiva() {
        return redElectricaActiva;
    }

    public synchronized int getNivelNafta() {
        return nivelNafta;
    }

    public synchronized void consumirNafta() {
        // 1. Verificamos si hay ALGO consumiendo (Servidores o Aires)
        boolean hayConsumo = false;

        if (ventana != null) {
            // ¿Hay algún servidor en Verde?
            boolean serversActivos = ventana.lblProd.getBackground().equals(java.awt.Color.GREEN) ||
                                     ventana.lblNas.getBackground().equals(java.awt.Color.GREEN)  ||
                                     ventana.lblLab.getBackground().equals(java.awt.Color.GREEN);

            // ¿Hay algún aire en Celeste?
            java.awt.Color celeste = new java.awt.Color(173, 216, 230);
            boolean airesActivos = ventana.lblAire1.getBackground().equals(celeste) ||
                                   ventana.lblAire2.getBackground().equals(celeste);

            if (serversActivos || airesActivos) {
                hayConsumo = true;
            }
        }

        // 2. Solo restamos nafta si hay consumo activo
        if (hayConsumo) {
            if (nivelNafta > 0) {
                nivelNafta--;
            }
        } else {
            // El generador está en "Ralentí" o Standby (No gasta)
            // System.out.println("Generador en Standby - Sin carga");
        }
    }
    
    
    //public synchronized void consumirNafta() {
    	//Verificamos si hay ALGO consumiendo (Servidores o Aires)
      //  boolean hayConsumo = false;
       // if (nivelNafta > 0) nivelNafta--;
    //}

    public synchronized void recargarNafta() {
        nivelNafta = 10;
        notifyAll(); // Despierta hilos que esperaban nafta
    }

    public synchronized int getNivelUps() {
        return nivelUps;
    }

    public synchronized void usarUps() {
        // 1. Contamos cuántos servidores están encendidos realmente
        int consumoActual = 0;
        
        // Usamos la referencia 'ventana' que ya tenés en la clase
        if (ventana.lblProd.getBackground().equals(java.awt.Color.GREEN)) consumoActual++;
        if (ventana.lblNas.getBackground().equals(java.awt.Color.GREEN)) consumoActual++;
        if (ventana.lblLab.getBackground().equals(java.awt.Color.GREEN)) consumoActual++;

        // 2. Solo bajamos el nivel si hay al menos un servidor encendido
        if (consumoActual > 0) {
            if (nivelUps > 0) nivelUps--;
            // Opcional: System.out.println("UPS consumiendo: " + consumoActual + " equipos.");
        } else {
            // Si entra acá, los servidores están apagados (por calor o prioridad)
            // y la UPS se mantiene fija.
        }
    }

    public synchronized void cargarUps() {
        if (nivelUps < 4) nivelUps++;

    }

    // Este método es vital: decide qué fuente de energía se usa
    public synchronized String getFuenteActual() {
        if (redElectricaActiva) return "RED";
        if (nivelNafta > 0) return "GENERADOR";
        if (nivelUps > 0) return "UPS";
        return "OFF";
    }
    
 // Métodos para incrementar y obtener los valores
    public synchronized void registrarGeneradorVacio() {
        conteoVacioGenerador++;
    }

    public synchronized void registrarUpsVacia() {
        conteoVacioUps++;
    }

    public synchronized int getConteoVacioGenerador() { return conteoVacioGenerador; }
    public synchronized int getConteoVacioUps() { return conteoVacioUps; }
}