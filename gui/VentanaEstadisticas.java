package gui;

import javax.swing.*;
import java.awt.*;

public class VentanaEstadisticas extends JFrame {
    private JLabel lblCortes, lblTiempo, lblNafta, lblUps, lblVacioGen, lblVacioUps;

    public VentanaEstadisticas() {
        setTitle("Reporte de Estadísticas");
        setSize(300, 400);
        setLayout(new GridLayout(6, 1, 10, 10));
        
        // Inicializamos los labels
        lblCortes = new JLabel("Cortes de luz: 0", SwingConstants.CENTER);
        lblTiempo = new JLabel("Tiempo fuera red: 0s", SwingConstants.CENTER);
        lblNafta = new JLabel("Nafta actual: 10/10", SwingConstants.CENTER);
        lblUps = new JLabel("Batería UPS: 4/4", SwingConstants.CENTER);
        lblVacioGen = new JLabel("Generador agotado: 0 veces", SwingConstants.CENTER);
        lblVacioUps = new JLabel("UPS agotada: 0 veces", SwingConstants.CENTER);

        // Agregamos al panel
        add(lblCortes);
        add(lblTiempo);
        add(lblNafta);
        add(lblUps);
        add(lblVacioGen);
        add(lblVacioUps);

        // La posicionamos un poco a la derecha de la principal
        setLocation(900, 100); 
    }

    // Métodos para actualizar los datos desde afuera
    public void actualizarDatos(int cortes, long tiempo, int nafta, int ups, int vGen, int vUps) {
        lblCortes.setText("Cortes de luz: " + cortes);
        lblTiempo.setText("Tiempo fuera red: " + tiempo + "s");
        lblNafta.setText("Nafta actual: " + nafta + "/10");
        lblUps.setText("Batería UPS: " + ups + "/4");
        lblVacioGen.setText("Generador agotado: " + vGen + " veces");
        lblVacioUps.setText("UPS agotada: " + vUps + " veces");
    }
}