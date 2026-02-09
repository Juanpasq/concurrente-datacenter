package logica;

import javax.swing.SwingUtilities;

public class HiloUps extends Thread {
    private MonitorEnergia monitor;
    private gui.VentanaPrincipal ventana;
    private boolean yaContadoVacio = false;

    public HiloUps(MonitorEnergia monitor, gui.VentanaPrincipal ventana) {
        this.monitor = monitor;
        this.ventana = ventana;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(3000); // La UPS es un poco más lenta para reaccionar (3 seg)

                boolean hayRed = monitor.isRedElectricaActiva();
                int nafta = monitor.getNivelNafta();
                int upsActual = monitor.getNivelUps();

                //Descarga (No hay red Y no hay nafta)
                if (!hayRed && nafta <= 0 && upsActual > 0) {
                    monitor.usarUps();
                    actualizarInterfaz();
                } 
                //Recarga (Hay red o hay nafta, y no está llena)
                else if ((hayRed || nafta > 0) && upsActual < 4) {
                    monitor.cargarUps();
                    actualizarInterfaz();
                }
                
                if (monitor.getNivelUps() == 1 && !yaContadoVacio) {
                    //monitor.registrarUpsVacia();
                    ventana.actualizarEstadisticasAgonía();
                    //ventana.escribirLog("🚨 CRÍTICO: Enviando señal de shutdown PROD.");
                    yaContadoVacio = true;
                }
                
                if (monitor.getNivelUps() == 0 && !yaContadoVacio) {
                    monitor.registrarUpsVacia();
                    ventana.actualizarEstadisticasAgonía();
                    ventana.escribirLog("🔋 UPS actualizando nivel: 7%");
                    ventana.escribirLog("🚨 CRÍTICO: Enviando señal de shutdown PROD...");
                    //ventana.escribirLog("enviando ...");
                    ventana.escribirLog("💀 BLACKOUT: La UPS se ha agotado completamente.");
                    yaContadoVacio = true;
                }
                if (monitor.getNivelUps() > 0) {
                    yaContadoVacio = false;
                }
                
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarInterfaz() {
        int nivel = monitor.getNivelUps();
        //Encolamos los pedidos con Invoker
        SwingUtilities.invokeLater(() -> {
            ventana.actualizarBarras(-1, nivel); // -1 para no tocar la nafta
            ventana.escribirLog("🔋 UPS actualizando nivel: " + (nivel * 25) + "%");
        });
    }
    

}