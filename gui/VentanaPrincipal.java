package gui;

import javax.swing.*;

import java.awt.*;
import logica.MonitorEnergia;
import logica.HiloGenerador;
import logica.HiloAire;
import logica.HiloUps; 
import logica.HiloServidor;
import gui.VentanaEstadisticas;

public class VentanaPrincipal extends JFrame {

    // Etiquetas de estado
    public JLabel lblProd, lblNas, lblLab, lblAire1, lblAire2;
    private JLabel lblFuenteActual; // <--- Fuente de energía actual
    
    //Contador de veces off PROD
    public static int contadorApagadosProd = 0;
    public JLabel lblEstadisticaProd;
    
    //Aire acc
    private double temperatura = 22.0; // Temperatura inicial ideal
    private JLabel lblTempAmbiente;     // La etiqueta para la interfaz
    public static boolean emergenciaTermica = false;
    public static boolean aire1Roto = false; // Interruptor para el Aire 1
    
    // Estadísticas
    private JLabel lblContadorCortes, lblTiempoFuera, lblNivelNaftaStats, lblNivelUpsStats;
    private int cortes = 0;

    // Barras de progreso
    private JProgressBar barNafta, barUps;
    
    // Log
    public static JTextArea logEventos;
    private MonitorEnergia monitor;
    private long tiempoInicioCorte = 0;
    private int contadorCortes = 0;
    private long tiempoTotalCortes = 0;
    private JLabel lblVacioGen, lblVacioUps;
    
    //ventana de estadisticas
    private VentanaEstadisticas vStats;

    //Le pasamos la ventana de estadisticas para que conecte
    public VentanaPrincipal(VentanaEstadisticas vStats) {
    	
    
    	this.vStats = vStats;
        this.monitor = new MonitorEnergia(this);
        setTitle("Monitor de DataCenter - TP Concurrente");
        this.monitor = new MonitorEnergia(this);
        setSize(850, 600); // Un poco más grande para que quepan las estadísticas
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. PANEL SUPERIOR: Equipos ---
        JPanel pnlEquipos = new JPanel(new GridLayout(1, 2));
        
        JPanel pnlServidores = new JPanel();
        pnlServidores.setBorder(BorderFactory.createTitledBorder("Estado de Servidores"));
        lblProd = crearLabelEquipo("PROD", Color.GREEN);
        lblNas = crearLabelEquipo("NAS", Color.GREEN);
        lblLab = crearLabelEquipo("LAB", Color.GREEN);
        pnlServidores.add(lblProd);
        pnlServidores.add(lblNas);
        pnlServidores.add(lblLab);
        

        JPanel pnlAires = new JPanel();
        pnlAires.setBorder(BorderFactory.createTitledBorder("Climatización"));
        lblAire1 = crearLabelEquipo("❄️ AIRE 1", new Color(173, 216, 230));
        lblAire2 = crearLabelEquipo("❄️ AIRE 2", new Color(173, 216, 230));
        pnlAires.add(lblAire1);
        pnlAires.add(lblAire2);

        pnlEquipos.add(pnlServidores);
        pnlEquipos.add(pnlAires);

        // --- 2. PANEL CENTRAL: Energía y Fuente ---
        JPanel pnlCentral = new JPanel(new BorderLayout());
        
        // Barras
        JPanel pnlBarras = new JPanel(new GridLayout(2, 1));
        pnlBarras.setBorder(BorderFactory.createTitledBorder("Niveles de Energía"));
        barNafta = new JProgressBar(0, 10);
        barNafta.setForeground(new Color(64, 64, 64));
        barNafta.setValue(10);
        barNafta.setStringPainted(true);
        barNafta.setString("Generador (Nafta)");
        barNafta.setPreferredSize(new Dimension(300, 20));
        
        barUps = new JProgressBar(0, 4);
        barUps.setValue(4);
        barUps.setStringPainted(true);
        barUps.setString("UPS (Batería)");
        barUps.setForeground(new Color(144, 238, 144)); // Verde claro (Light Green)
        barUps.setPreferredSize(new Dimension(300, 20));
        
        pnlBarras.add(barNafta);
        pnlBarras.add(barUps);

        // Fuente Actual (ETIQUETA GRANDE)
        lblFuenteActual = new JLabel("FUENTE: RED ELÉCTRICA", SwingConstants.CENTER);
        lblFuenteActual.setFont(new Font("Arial", Font.BOLD, 18));
        lblFuenteActual.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblFuenteActual.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlCentral.add(pnlBarras, BorderLayout.CENTER);
        pnlCentral.add(lblFuenteActual, BorderLayout.SOUTH);

        // --- 3. PANEL INFERIOR: Controles, Logs y Stats ---
        JPanel pnlInferior = new JPanel(new BorderLayout());
        
        
        // Botones y funciones >>>>>>
        JPanel pnlBotones = new JPanel();
        //pnlBotones.add(new JButton("Cortar Luz"));
        //pnlBotones.add(new JButton("Restaurar Luz"));
        //pnlBotones.add(new JButton("Recargar Nafta"));
        
        //apagar aire 1 
        JButton btnApagarAire1 = new JButton("ON/OFF Aire 1");
        btnApagarAire1.addActionListener(e -> {
            aire1Roto = !aire1Roto; // Cambia entre encendido y apagado
            if (aire1Roto) {
                escribirLog("❄️ Aire 1 apagado.");
            } else {
                escribirLog("❄️ Aire 1 reconectado.");
            }
        });
        pnlBotones.add(btnApagarAire1);
        
        JButton btnCortar = new JButton("Cortar Luz");
     
        btnCortar.addActionListener(e -> {
            if (monitor.isRedElectricaActiva()) { // Solo si no estaba ya cortada
                monitor.setRedElectrica(false);
                tiempoInicioCorte = System.currentTimeMillis(); // Guardamos el momento exacto
                contadorCortes++;
                lblContadorCortes.setText("Cortes de luz: " + contadorCortes);
                escribirLog("⚠️ CORTE DE LUZ DETECTADO.");
            }
        });
        
        // test falla en los 2
        JButton btnTestTemp = new JButton("Simular Falla Aire");
        btnTestTemp.addActionListener(e -> {
            // Aquí NO usamos vPri. porque ya estamos dentro de la clase
            this.temperatura = 34.0; 
            
            // Apagamos los aires visualmente para que la temperatura empiece a subir sola
            lblAire1.setBackground(Color.RED);
            lblAire2.setBackground(Color.RED);
            
            escribirLog("⚠️ SIMULACIÓN: Aires desactivados. Monitoreando calor...");
        });
        pnlBotones.add(btnTestTemp);
        
        
        JButton btnRestaurar = new JButton("Restaurar Luz");
        btnRestaurar.addActionListener(e -> {
            monitor.setRedElectrica(true);
            logEventos.append("✅ CAMBIANDO A RED ELECTRICA.\n");
            
        });
        
        btnRestaurar.addActionListener(e -> {
            if (!monitor.isRedElectricaActiva()) { // Solo si estaba cortada
                monitor.setRedElectrica(true);
                long tiempoFinCorte = System.currentTimeMillis();
                long segundosFuera = (tiempoFinCorte - tiempoInicioCorte) / 1000;
                tiempoTotalCortes += segundosFuera;
                
                lblTiempoFuera.setText("Tiempo fuera red: " + segundosFuera + "s");
                escribirLog("✅ RED ELECTRICA RESTAURADA. Duración del corte: " + segundosFuera + "s");
                lblTiempoFuera.setText("Tiempo fuera red: 00s / " + tiempoTotalCortes + "s");
            }
        });
        
        
        JButton btnRecargar = new JButton("Recargar Nafta");
        btnRecargar.addActionListener(e -> {
            monitor.recargarNafta();
            barNafta.setForeground(new Color(64, 64, 64));
            logEventos.append("⛽ Generador recargado al 100%.\n");
            barNafta.setValue(10);
            barNafta.setString("Generador (Nafta): 100%");
        });

        pnlBotones.add(btnCortar);
        pnlBotones.add(btnRestaurar);
        pnlBotones.add(btnRecargar);
        
        //Dimensiones de botones, panel estadisticas, text area de logs
        // Log y Stats (Centro de la parte inferior)
        JPanel pnlLogYStats = new JPanel(new BorderLayout());
        
        logEventos = new JTextArea(10, 20);
        logEventos.setEditable(false);
        logEventos.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Fuente más prolija para logs
        logEventos.setText("SISTEMA INICIADO: Red eléctrica estable.\n");
        JScrollPane scrollLog = new JScrollPane(logEventos);
        scrollLog.setPreferredSize(new Dimension(300, 300)); // Fijamos un tamaño para que no compita con los stats
        
        // Panel de Estadísticas (A la derecha del log)
        JPanel pnlStats = new JPanel(new GridLayout(7, 1, 5, 5)); // 6 filas, 1 columna, 5px de espacio
        pnlStats.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        pnlStats.setPreferredSize(new Dimension(250, 0)); // Le damos un ancho fijo para que no "empuje" al log
        
        lblEstadisticaProd = new JLabel("PROD OFF: 0");
        pnlStats.add(lblEstadisticaProd);
        
        //Aire ACC
        lblTempAmbiente = new JLabel("🌡️ Temp. Sala: 22.0°C");
        lblTempAmbiente.setFont(new Font("Arial", Font.BOLD, 14));
        lblTempAmbiente.setForeground(new Color(0, 100, 0)); // Empezamos en verde
        pnlAires.add(lblTempAmbiente);
        
        lblContadorCortes = new JLabel("Cortes de luz: 0");
        lblTiempoFuera = new JLabel("Tiempo fuera red: 0s");
        lblNivelNaftaStats = new JLabel("Nafta actual: 10/10");
        lblNivelUpsStats = new JLabel("Batería UPS: 4/4");
        
        pnlStats.add(lblContadorCortes);
        pnlStats.add(lblTiempoFuera);
        pnlStats.add(lblNivelNaftaStats);
        pnlStats.add(lblNivelUpsStats);
        //pnlStats.add(lblVacioGen); // El nuevo de agotamiento generador
        //pnlStats.add(lblVacioUps); // El nuevo de agotamiento UPS

        pnlLogYStats.add(scrollLog, BorderLayout.CENTER);
        pnlLogYStats.add(pnlStats, BorderLayout.EAST);

        pnlInferior.add(pnlBotones, BorderLayout.NORTH);
        pnlInferior.add(pnlLogYStats, BorderLayout.CENTER);

        // Agregar todo al JFrame
        add(pnlEquipos, BorderLayout.NORTH);
        add(pnlCentral, BorderLayout.CENTER);
        add(pnlInferior, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
        
        //Conteo de las veces que llego a cero generador y ups
        lblVacioGen = new JLabel("Generador agotado: 0 veces");
        lblVacioUps = new JLabel("UPS agotada: 0 veces");
        pnlStats.add(lblVacioGen);
        pnlStats.add(lblVacioUps); 
        
        
    }

    private JLabel crearLabelEquipo(String nombre, Color color) {
        JLabel label = new JLabel(nombre, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(120, 45));
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Creamos la ventana de estadísticas primero
            VentanaEstadisticas vStats = new VentanaEstadisticas();
            
            // 2. Creamos la ventana principal pasándole la de estadísticas
            // Importante: El constructor de VentanaPrincipal debe recibir vStats
            VentanaPrincipal vPri = new VentanaPrincipal(vStats);
            
            // 3. Posicionamos y mostramos las ventanas
            vPri.setLocationRelativeTo(null); // Centra la principal
            vStats.setVisible(true);
            vPri.setVisible(true);

            // 4. Arrancamos los hilos de Energía
            new logica.HiloGenerador(vPri.monitor, vPri).start();
            new logica.HiloUps(vPri.monitor, vPri).start();

            // 5. Arrancamos los hilos de los Servidores
            // PROD (Prioridad 1), NAS y LAB (Prioridad 2)
            new logica.HiloServidor("PROD", vPri.monitor, vPri.lblProd, 1).start();
            new logica.HiloServidor("NAS", vPri.monitor, vPri.lblNas, 2).start();
            new logica.HiloServidor("LAB", vPri.monitor, vPri.lblLab, 2).start();

            // 6. Arrancamos los hilos de los Aires
            new logica.HiloAire("AIRE 1", vPri.monitor, vPri.lblAire1, 1).start();
            new logica.HiloAire("AIRE 2", vPri.monitor, vPri.lblAire2, 2).start();

            // 7. HILO DE CONTROL VISUAL Y REFRESCO (El "motor" de la interfaz)
            new Thread(() -> {
                while (true) {
                	
                    try {
                        Thread.sleep(500); // Actualiza cada medio segundo
                        
                        
                       //AIRES ACC y boton stress aires
                        if (vPri.temperatura >= 30.0) {
                            if (!vPri.emergenciaTermica) {
                                vPri.emergenciaTermica = true;
                                vPri.escribirLog("🚨 EMERGENCIA TÉRMICA: Enviando señal POWER OFF >>> PROD - LAB - NAS");
                                vPri.escribirLog("🚨 EMERGENCIA TÉRMICA: Sistemas apagados por seguridad (>30°C): Enviando señal POWER ON >>> PROD - LAB - NAS");

                            }
                        } else if (vPri.temperatura < 25.0) {
                            if (vPri.emergenciaTermica) {
                                vPri.emergenciaTermica = false;
                                vPri.escribirLog("✅ TEMPERATURA SEGURA (<25°C): .");
                            }
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            vPri.lblTempAmbiente.setText(String.format(" 🌡️ %.1f°C ", vPri.temperatura));
                            
                            // Si hay emergencia, pintamos todo el panel de servidores de un color de alerta
                            if (vPri.emergenciaTermica) {
                                vPri.lblTempAmbiente.setForeground(Color.RED);
                            }
                        });
                        vPri.lblEstadisticaProd.setText("OFF PROD: " + gui.VentanaPrincipal.contadorApagadosProd);
                        
                        /*if (vPri.temperatura >= 30.0) {
                            SwingUtilities.invokeLater(() -> {
                                vPri.lblTempAmbiente.setText("🔥 CRÍTICO: " + String.format("%.1f", vPri.temperatura) + "°C");
                                vPri.getContentPane().setBackground(new Color(255, 200, 200)); // Fondo rosado de alerta
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                vPri.getContentPane().setBackground(null); // Vuelve al color normal si baja
                            });
                        }*/
                        
                     // Dentro del try del hilo de refresco en el main
                        double cambioTemp = 0;

                        // CALOR: Sumamos si los servidores están prendidos, PROD consume mas. (NO están en rojo)
                        if (!vPri.lblProd.getBackground().equals(Color.RED)) cambioTemp += 0.09;
                        if (!vPri.lblNas.getBackground().equals(Color.RED))  cambioTemp += 0.05;
                        if (!vPri.lblLab.getBackground().equals(Color.RED))  cambioTemp += 0.07;

                        // FRÍO: Restamos si los aires están enfriando (Celeste)
                        Color colorAireActivo = new Color(173, 216, 230);
                        if (vPri.lblAire1.getBackground().equals(colorAireActivo)) cambioTemp -= 0.15;
                        if (vPri.lblAire2.getBackground().equals(colorAireActivo)) cambioTemp -= 0.15;

                        // Actualizamos la variable de la ventana
                        vPri.temperatura += cambioTemp;

                        // Límites de seguridad para que no sea infinito|
                        if (vPri.temperatura < 18) vPri.temperatura = 18; // Los aires no congelan más que eso
                        if (vPri.temperatura > 35) vPri.temperatura = 35; // Punto de falla crítica

                        SwingUtilities.invokeLater(() -> {
                            // Actualizamos el texto con un solo decimal
                            vPri.lblTempAmbiente.setText(String.format(" 🌡️ %.1f°C ", vPri.temperatura));
                            
                            // Cambiamos el color del texto según la gravedad
                            if (vPri.temperatura > 28) {
                                vPri.lblTempAmbiente.setForeground(Color.RED); // ¡Calor!
                            } else if (vPri.temperatura > 24) {
                                vPri.lblTempAmbiente.setForeground(Color.ORANGE); // Advertencia
                            } else {
                                vPri.lblTempAmbiente.setForeground(new Color(0, 100, 0)); // Todo OK (Verde)
                            }
                        });                        
                        
                        // Actualizamos la fuente actual (Texto grande)
                        String fuente = vPri.monitor.getFuenteActual();
                        SwingUtilities.invokeLater(() -> vPri.actualizarFuenteVisual(fuente));
                                           
                        
                        // MANDAMOS LOS DATOS A LA VENTANA DE ESTADÍSTICAS
                        vPri.refrescarEstadisticasVisuales();
                        
                    } catch (Exception e) {
                        System.err.println("Error en hilo de refresco: " + e.getMessage());
                    }
                }
            }).start();
        });
    }
    
 // Método para actualizar las barras desde los hilos
    public void actualizarBarras(int nafta, int ups) {
        if (nafta != -1) {
            barNafta.setValue(nafta);
            barNafta.setString("Generador (Nafta): " + (nafta * 10) + "%");
            lblNivelNaftaStats.setText("Nafta actual: " + nafta + "/10");
        }
        if (ups != -1) {
            barUps.setValue(ups);
            barUps.setString("UPS (Batería): " + (ups * 25) + "%");
            lblNivelUpsStats.setText("Batería UPS: " + ups + "/4");
        }
    }
    
    public void actualizarFuenteVisual(String fuente) {
        lblFuenteActual.setText("FUENTE: " + fuente);
        if (fuente.equals("RED")) {
            lblFuenteActual.setForeground(new Color(0, 100, 0));
        } else if (fuente.equals("GENERADOR")) {
            lblFuenteActual.setForeground(Color.BLUE);
        } else if (fuente.equals("UPS")) {
            lblFuenteActual.setForeground(Color.ORANGE);
        } else {
            lblFuenteActual.setForeground(Color.RED);
        }
    }

    // Método para escribir en el log desde fuera
    public void escribirLog(String mensaje) {
        logEventos.append(mensaje + "\n");
    }
    
    public void actualizarEstadisticasAgonía() {
        SwingUtilities.invokeLater(() -> {
            lblVacioGen.setText("Generador agotado: " + monitor.getConteoVacioGenerador() + " veces");
            lblVacioUps.setText("UPS agotada: " + monitor.getConteoVacioUps() + " veces");
        });
    }
    
    //Este método actualiza la OTRA ventana (vStats)
    public void refrescarEstadisticasVisuales() {
    	
        if (vStats != null) { // Verificamos que la ventana exista para evitar errores
            vStats.actualizarDatos(
                this.contadorCortes, 
                this.calcularTiempoActual(), // Método que haremos ahora
                monitor.getNivelNafta(), 
                monitor.getNivelUps(), 
                monitor.getConteoVacioGenerador(), 
                monitor.getConteoVacioUps()
                
            );
        }
        
     // agregacion del tiempo total acumulado
        long tiempoActual = calcularTiempoActual(); // El tiempo que lleva el corte actual
        lblTiempoFuera.setText("Tiempo fuera red: " + tiempoActual + "s / " + tiempoTotalCortes + "s");
        
    }

    // Método ayudante para calcular cuánto tiempo va de corte
    private long calcularTiempoActual() {
        if (monitor.isRedElectricaActiva()) {
            return 0; // Si hay luz, el tiempo de "corte actual" es 0
        }
        // Si no hay luz, restamos la hora actual menos cuando empezó el corte
        return (System.currentTimeMillis() - tiempoInicioCorte) / 1000;
    }
}