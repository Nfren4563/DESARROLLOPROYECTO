package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import mx.unison.cliente.serial.simuladorDatos;
import mx.unison.cliente.serial.ArduinoReader;
import mx.unison.cliente.com.socketCliente;
import mx.unison.cliente.sensorArduino.sensor;
import mx.unison.cliente.main.appFrame;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;

public class panelMonitoreo extends JPanel {

    // Colores UNISON según especificaciones del PDF
    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color AZUL_OSCURO = new Color(0x01, 0x52, 0x94);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Color DORADO_OSCURO = new Color(0xD9, 0x9E, 0x30);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SEGOE_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // Series de datos para la gráfica
    private TimeSeries seriesX = new TimeSeries("X");
    private TimeSeries seriesY = new TimeSeries("Y");
    private TimeSeries seriesZ = new TimeSeries("Z");

    // Componentes para lectura de datos
    private simuladorDatos simulador;
    private ArduinoReader arduino;
    private socketCliente socket = new socketCliente("localhost", 5000);
    private appFrame app;

    // Componentes de interfaz
    private JComboBox<String> puertoCombo;
    private JRadioButton rbSimulador;
    private JRadioButton rbArduino;
    private JButton btnIniciar;
    private JButton btnDetener;
    private JButton btnRefrescar;
    private JLabel statusLabel;

    private boolean isRunning = false;

    public panelMonitoreo(appFrame app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Crear los tres paneles principales
        add(crearPanelControles(), BorderLayout.NORTH);
        add(crearPanelEstado(), BorderLayout.SOUTH);
        add(crearGrafica(), BorderLayout.CENTER);
    }

    /**
     * Crea el panel superior con controles de modo y botones
     */
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AZUL_UNISON);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Panel izquierdo: Selección de modo y puerto con GridBagLayout
        JPanel panelIzquierdo = new JPanel(new GridBagLayout());
        panelIzquierdo.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Etiqueta "Modo:"
        gbc.gridx = 0;
        JLabel lblModo = new JLabel("Modo:");
        lblModo.setForeground(Color.WHITE);
        lblModo.setFont(FONT_SEGOE_BOLD);
        panelIzquierdo.add(lblModo, gbc);

        // Radio buttons para modo Simulador / Arduino
        gbc.gridx = 1;
        rbSimulador = new JRadioButton("Simulador", true);
        estilizarRadioButton(rbSimulador);
        panelIzquierdo.add(rbSimulador, gbc);

        gbc.gridx = 2;
        rbArduino = new JRadioButton("Arduino");
        estilizarRadioButton(rbArduino);
        panelIzquierdo.add(rbArduino, gbc);

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rbSimulador);
        grupoModo.add(rbArduino);

        // Separador visual
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 15, 0, 15);
        JSeparator separador = new JSeparator(SwingConstants.VERTICAL);
        separador.setPreferredSize(new Dimension(2, 28));
        separador.setForeground(new Color(255, 255, 255, 100));
        panelIzquierdo.add(separador, gbc);

        // Selector de puerto COM
        gbc.gridx = 4;
        gbc.insets = new Insets(0, 8, 0, 8);
        JLabel lblPuerto = new JLabel("Puerto COM:");
        lblPuerto.setForeground(Color.WHITE);
        lblPuerto.setFont(FONT_SEGOE);
        panelIzquierdo.add(lblPuerto, gbc);

        gbc.gridx = 5;
        puertoCombo = new JComboBox<>(ArduinoReader.getAvailablePorts());
        puertoCombo.setFont(FONT_SEGOE);
        puertoCombo.setPreferredSize(new Dimension(110, 34));
        puertoCombo.setEnabled(false);
        panelIzquierdo.add(puertoCombo, gbc);

        // Botón refrescar puertos
        gbc.gridx = 6;
        gbc.insets = new Insets(0, 4, 0, 8);
        btnRefrescar = crearBotonIcono("↻");
        btnRefrescar.setToolTipText("Refrescar puertos");
        btnRefrescar.setEnabled(false);
        btnRefrescar.addActionListener(e -> refrescarPuertos());
        panelIzquierdo.add(btnRefrescar, gbc);

        // Event listeners para habilitar/deshabilitar selector según modo
        rbArduino.addActionListener(e -> {
            puertoCombo.setEnabled(true);
            btnRefrescar.setEnabled(true);
        });

        rbSimulador.addActionListener(e -> {
            puertoCombo.setEnabled(false);
            btnRefrescar.setEnabled(false);
        });

        // Panel derecho: Botones de acción
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecho.setOpaque(false);

        btnIniciar = crearBoton("▶ Iniciar");
        btnDetener = crearBoton("■ Detener");
        JButton btnVolver = crearBoton("← Volver");

        btnDetener.setEnabled(false);

        btnIniciar.addActionListener(e -> iniciarMonitoreo());
        btnDetener.addActionListener(e -> detenerMonitoreo());
        btnVolver.addActionListener(e -> {
            detenerMonitoreo();
            app.mostrar(appFrame.INICIO);
        });

        panelDerecho.add(btnIniciar);
        panelDerecho.add(btnDetener);
        panelDerecho.add(btnVolver);

        panel.add(panelIzquierdo, BorderLayout.WEST);
        panel.add(panelDerecho, BorderLayout.EAST);

        return panel;
    }

    /**
     * Crea el panel inferior con el estado del sistema
     */
    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        statusLabel = new JLabel("● Detenido - Seleccione un modo y presione Iniciar");
        statusLabel.setFont(FONT_SEGOE);
        statusLabel.setForeground(Color.GRAY);
        panel.add(statusLabel);

        return panel;
    }

    /**
     * Crea la gráfica en tiempo real con JFreeChart
     */
    private ChartPanel crearGrafica() {
        // Configurar dataset con las tres series
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        // Crear gráfica
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Lectura en Tiempo Real - Sensor XYZ",
                "Tiempo",
                "Valor",
                dataset,
                true,  // leyenda
                true,  // tooltips
                false  // URLs
        );

        // Estilizar gráfica
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        // Crear panel de gráfica
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        chartPanel.setPreferredSize(new Dimension(800, 500));

        return chartPanel;
    }

    /**
     * Inicia el monitoreo según el modo seleccionado
     */
    private void iniciarMonitoreo() {
        // Limpiar datos anteriores de la gráfica
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();

        if (rbSimulador.isSelected()) {
            iniciarSimulador();
        } else {
            iniciarArduino();
        }
    }

    /**
     * Inicia el modo simulador
     */
    private void iniciarSimulador() {
        simulador = new simuladorDatos(this::procesarDato);
        simulador.start();
        actualizarEstadoUI(true, "● Simulador activo - Generando datos aleatorios cada segundo");
    }

    /**
     * Inicia el modo Arduino
     */
    private void iniciarArduino() {
        String puerto = (String) puertoCombo.getSelectedItem();

        // Validar que se haya seleccionado un puerto
        if (puerto == null || puerto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un puerto COM de la lista.\n\n" +
                            "Si no aparecen puertos:\n" +
                            "• Conecte el Arduino al puerto USB\n" +
                            "• Presione el botón Refrescar (↻)",
                    "Puerto no seleccionado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mostrar mensaje de conexión
        statusLabel.setText("⏳ Conectando a " + puerto + "...");
        statusLabel.setForeground(new Color(255, 140, 0)); // Naranja
        btnIniciar.setEnabled(false);

        // Conectar en hilo separado para no bloquear la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                arduino = new ArduinoReader(
                        panelMonitoreo.this::procesarDato,
                        error -> SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("⚠ Error: " + error);
                            statusLabel.setForeground(Color.RED);
                        })
                );
                return arduino.connect(puerto);
            }

            @Override
            protected void done() {
                try {
                    boolean conectado = get();
                    if (conectado) {
                        arduino.start();
                        actualizarEstadoUI(true, "● Conectado a " + puerto + " - Leyendo datos del Arduino");
                    } else {
                        actualizarEstadoUI(false, "● Detenido");
                        JOptionPane.showMessageDialog(panelMonitoreo.this,
                                "No se pudo conectar al puerto " + puerto + "\n\n" +
                                        "Posibles causas:\n" +
                                        "• El Arduino no está conectado al USB\n" +
                                        "• Otro programa está usando el puerto (Arduino IDE)\n" +
                                        "• No tiene permisos de acceso al puerto\n" +
                                        "• El puerto seleccionado es incorrecto\n\n" +
                                        "Soluciones:\n" +
                                        "1. Verifique la conexión USB del Arduino\n" +
                                        "2. Cierre el Monitor Serie del Arduino IDE\n" +
                                        "3. Presione Refrescar para actualizar puertos\n" +
                                        "4. Intente con otro puerto de la lista",
                                "Error de conexión",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    actualizarEstadoUI(false, "● Error al conectar");
                    JOptionPane.showMessageDialog(panelMonitoreo.this,
                            "Error inesperado al conectar:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Detiene el monitoreo activo
     */
    private void detenerMonitoreo() {
        if (simulador != null) {
            simulador.stop();
            simulador = null;
        }
        if (arduino != null) {
            arduino.disconnect();
            arduino = null;
        }
        actualizarEstadoUI(false, "● Detenido");
    }

    /**
     * Procesa cada dato recibido (desde Simulador o Arduino)
     */
    private void procesarDato(sensor d) {
        // Actualizar gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            Millisecond ahora = new Millisecond();
            seriesX.addOrUpdate(ahora, d.getX());
            seriesY.addOrUpdate(ahora, d.getY());
            seriesZ.addOrUpdate(ahora, d.getZ());

            // Limitar datos en memoria (últimos 60 puntos = 1 minuto)
            if (seriesX.getItemCount() > 60) {
                seriesX.delete(0, 0);
                seriesY.delete(0, 0);
                seriesZ.delete(0, 0);
            }
        });

        // Enviar datos al servidor en hilo separado
        new Thread(() -> socket.sendSensorData(d)).start();
    }

    /**
     * Actualiza el estado de la interfaz (botones habilitados/deshabilitados)
     */
    private void actualizarEstadoUI(boolean corriendo, String mensaje) {
        isRunning = corriendo;
        btnIniciar.setEnabled(!corriendo);
        btnDetener.setEnabled(corriendo);
        rbSimulador.setEnabled(!corriendo);
        rbArduino.setEnabled(!corriendo);
        puertoCombo.setEnabled(!corriendo && rbArduino.isSelected());
        btnRefrescar.setEnabled(!corriendo && rbArduino.isSelected());

        statusLabel.setText(mensaje);
        statusLabel.setForeground(corriendo ? new Color(0, 150, 0) : Color.GRAY);
    }

    /**
     * Refresca la lista de puertos COM disponibles
     */
    private void refrescarPuertos() {
        puertoCombo.removeAllItems();
        String[] puertos = ArduinoReader.getAvailablePorts();

        for (String puerto : puertos) {
            puertoCombo.addItem(puerto);
        }

        if (puertos.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron puertos COM disponibles.\n\n",
                    "Sin puertos disponibles",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            String plural = puertos.length == 1 ? "" : "s";
            JOptionPane.showMessageDialog(this,
                    "Se encontraron " + puertos.length + " puerto" + plural + " COM disponible" + plural + ":\n\n" +
                            String.join(", ", puertos),
                    "Puertos actualizados",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Crea un botón con el estilo UNISON (borde redondeado de 4px)
     */
    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_SEGOE);
        btn.setBackground(DORADO_UNISON);
        btn.setForeground(AZUL_UNISON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Borde redondeado de 4px (simulado)
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DORADO_UNISON, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Efecto hover (dorado oscuro)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_OSCURO);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_UNISON);
            }
        });

        return btn;
    }

    /**
     * Crea un botón pequeño con ícono (para refrescar)
     */
    private JButton crearBotonIcono(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(DORADO_UNISON);
        btn.setForeground(AZUL_UNISON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(40, 34));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_OSCURO);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_UNISON);
            }
        });

        return btn;
    }

    /**
     * Aplica estilo UNISON a un radio button
     */
    private void estilizarRadioButton(JRadioButton rb) {
        rb.setForeground(Color.WHITE);
        rb.setFont(FONT_SEGOE);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}