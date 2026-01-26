package logica;

import javax.swing.SwingUtilities;

public class HiloGenerador extends Thread {
    private MonitorEnergia monitor;
    private gui.VentanaPrincipal ventana;
    private boolean yaContadoVacio = false;


    public HiloGenerador(MonitorEnergia monitor, gui.VentanaPrincipal ventana) {
        this.monitor = monitor;
        this.ventana = ventana;
       
    }

    @Override
    public void run() {
        while (true) { // El hilo siempre está "vivo"
            try {
                // Esperamos 2 segundos entre cada "tick" de consumo
                Thread.sleep(2000); 

                // Lógica de consumo
                if (!monitor.isRedElectricaActiva() && monitor.getNivelNafta() > 0) {
                    monitor.consumirNafta();
                    
                    // Actualizamos la interfaz (usando invokeLater por seguridad)
                    int naftaActual = monitor.getNivelNafta();
                    SwingUtilities.invokeLater(() -> {
                        ventana.actualizarBarras(naftaActual, -1); // -1 significa no tocar UPS
                        ventana.escribirLog("⛽ Generador consumiendo... Nivel: " + naftaActual);
                    });
                }
                
                if (monitor.getNivelNafta() == 2 && !yaContadoVacio) {
            	    monitor.registrarGeneradorVacio();
            	    ventana.actualizarEstadisticasAgonía();
            	    ventana.escribirLog("🚨 CRÍTICO: Enviando señal de shutdown NAS.");
            	    ventana.escribirLog("🚨 CRÍTICO: Enviando señal de shutdown LAB.");
            	    yaContadoVacio = true;
            	}
                
                //cuando detectes que la nafta es 0:
            	if (monitor.getNivelNafta() == 0 && !yaContadoVacio) {
            	    monitor.registrarGeneradorVacio();
            	    ventana.actualizarEstadisticasAgonía();
            	    ventana.escribirLog("🚨 CRÍTICO: El generador se ha quedado sin combustible.");
            	    yaContadoVacio = true;
            	}
            	// Cuando vuelve la luz o se recarga nafta, resetear:
            	if (monitor.getNivelNafta() > 0) {
            	    yaContadoVacio = false;
            	}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        

    }
}