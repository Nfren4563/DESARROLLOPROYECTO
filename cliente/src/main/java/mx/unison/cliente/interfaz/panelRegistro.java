package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;

import mx.unison.cliente.com.socketCliente;
import mx.unison.cliente.sensorArduino.sensor;
import mx.unison.cliente.main.appFrame;
import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class panelRegistro extends JPanel {

    // Colores UNISON
    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color AZUL_OSCURO = new Color(0x01, 0x52, 0x94);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Color DORADO_OSCURO = new Color(0xD9, 0x9E, 0x30);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 14);

    private socketCliente socket = new socketCliente("localhost", 5000);
    private appFrame app;

    private JSpinner fechaSpinner;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private DefaultCategoryDataset dataset;
    private JLabel statusLabel;
    private JButton btnBuscar;
    private JButton btnLimpiar;

    public panelRegistro(appFrame app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior con filtros
        add(crearPanelFiltros(), BorderLayout.NORTH);

        // Panel de estado/carga
        add(crearPanelEstado(), BorderLayout.SOUTH);

        // Gráfica central
        add(crearGrafica(), BorderLayout.CENTER);
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AZUL_UNISON);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel izquierdo: Campos de filtro con GridBagLayout para mejor control
        JPanel inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Campo de fecha
        gbc.gridx = 0;
        JLabel lblFecha = crearLabel("Fecha:");
        inputsPanel.add(lblFecha, gbc);

        gbc.gridx = 1;
        fechaSpinner = new JSpinner(new SpinnerDateModel());
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "yyyy-MM-dd"));
        fechaSpinner.setFont(FONT_SEGOE);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) fechaSpinner.getEditor();
        editor.getTextField().setColumns(12);
        editor.getTextField().setPreferredSize(new Dimension(140, 32));
        estilizarCampo(editor.getTextField());
        inputsPanel.add(fechaSpinner, gbc);

        // Espacio
        gbc.gridx = 2;
        inputsPanel.add(Box.createHorizontalStrut(20), gbc);

        // Campo hora inicio
        gbc.gridx = 3;
        JLabel lblHoraInicio = crearLabel("Hora inicio:");
        inputsPanel.add(lblHoraInicio, gbc);

        gbc.gridx = 4;
        txtHoraInicio = new JTextField("08:00", 8);
        txtHoraInicio.setPreferredSize(new Dimension(100, 32));
        estilizarCampo(txtHoraInicio);
        txtHoraInicio.setToolTipText("Formato: HH:MM o HH:MM:SS");
        inputsPanel.add(txtHoraInicio, gbc);

        // Espacio
        gbc.gridx = 5;
        inputsPanel.add(Box.createHorizontalStrut(20), gbc);

        // Campo hora fin
        gbc.gridx = 6;
        JLabel lblHoraFin = crearLabel("Hora fin:");
        inputsPanel.add(lblHoraFin, gbc);

        gbc.gridx = 7;
        txtHoraFin = new JTextField("18:00", 8);
        txtHoraFin.setPreferredSize(new Dimension(100, 32));
        estilizarCampo(txtHoraFin);
        txtHoraFin.setToolTipText("Formato: HH:MM o HH:MM:SS");
        inputsPanel.add(txtHoraFin, gbc);

        // Panel derecho: Botones de control
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.setOpaque(false);

        btnBuscar = crearBoton("🔍 Buscar");
        btnLimpiar = crearBoton("✖ Limpiar");
        JButton btnVolver = crearBoton("← Volver");

        btnBuscar.addActionListener(e -> buscarDatos());
        btnLimpiar.addActionListener(e -> limpiarGrafica());
        btnVolver.addActionListener(e -> app.mostrar(appFrame.INICIO));

        botonesPanel.add(btnBuscar);
        botonesPanel.add(btnLimpiar);
        botonesPanel.add(btnVolver);

        panel.add(inputsPanel, BorderLayout.WEST);
        panel.add(botonesPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        statusLabel = new JLabel("● Listo - Seleccione los filtros y presione Buscar");
        statusLabel.setFont(FONT_SEGOE);
        statusLabel.setForeground(Color.GRAY);
        panel.add(statusLabel);

        return panel;
    }

    private ChartPanel crearGrafica() {
        dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createLineChart(
                "Histórico de Datos del Sensor - Filtrado por Fecha y Hora",
                "Número de Registro",
                "Valor",
                dataset
        );

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        return chartPanel;
    }

    private void buscarDatos() {
        // Validar formato de horas
        String horaInicio = txtHoraInicio.getText().trim();
        String horaFin = txtHoraFin.getText().trim();

        if (!validarHora(horaInicio)) {
            JOptionPane.showMessageDialog(this,
                    "Formato de hora inicio inválido.\n\nUse el formato HH:MM o HH:MM:SS\nEjemplo: 08:00 o 08:00:00",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            txtHoraInicio.requestFocus();
            return;
        }

        if (!validarHora(horaFin)) {
            JOptionPane.showMessageDialog(this,
                    "Formato de hora fin inválido.\n\nUse el formato HH:MM o HH:MM:SS\nEjemplo: 18:00 o 18:00:00",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            txtHoraFin.requestFocus();
            return;
        }

        Date d = (Date) fechaSpinner.getValue();
        LocalDate fecha = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        btnBuscar.setEnabled(false);
        btnLimpiar.setEnabled(false);
        statusLabel.setText("⏳ Cargando datos desde el servidor...");
        statusLabel.setForeground(new Color(255, 140, 0));

        dataset.clear();

        SwingWorker<List<sensor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<sensor> doInBackground() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}

                return socket.requestHistoricoFiltrado(
                        fecha.toString(),
                        horaInicio,
                        horaFin
                );
            }

            @Override
            protected void done() {
                try {
                    List<sensor> datos = get();
                    actualizarGrafica(datos);

                    if (datos.isEmpty()) {
                        statusLabel.setText("⚠ No se encontraron registros para los filtros especificados");
                        statusLabel.setForeground(new Color(200, 100, 0));

                        JOptionPane.showMessageDialog(panelRegistro.this,
                                "No se encontraron datos para:\n\n" +
                                        "Fecha: " + fecha + "\n" +
                                        "Hora inicio: " + horaInicio + "\n" +
                                        "Hora fin: " + horaFin + "\n\n" +
                                        "Intente con otro rango de fechas/horas.",
                                "Sin resultados",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("✓ Mostrando " + datos.size() + " registro(s) del " +
                                fecha + " entre " + horaInicio + " y " + horaFin);
                        statusLabel.setForeground(new Color(0, 150, 0));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("✖ Error al cargar datos");
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(panelRegistro.this,
                            "Error al obtener datos del servidor:\n\n" + ex.getMessage() +
                                    "\n\nVerifique que:\n" +
                                    "• El servidor esté ejecutándose\n" +
                                    "• La conexión de red esté disponible\n" +
                                    "• El puerto 5000 esté accesible",
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnBuscar.setEnabled(true);
                    btnLimpiar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void actualizarGrafica(List<sensor> datos) {
        dataset.clear();

        for (int i = 0; i < datos.size(); i++) {
            sensor s = datos.get(i);
            String categoria = String.valueOf(i + 1);
            dataset.addValue(s.getX(), "X", categoria);
            dataset.addValue(s.getY(), "Y", categoria);
            dataset.addValue(s.getZ(), "Z", categoria);
        }
    }

    private void limpiarGrafica() {
        dataset.clear();
        statusLabel.setText("● Gráfica limpiada - Seleccione filtros y presione Buscar");
        statusLabel.setForeground(Color.GRAY);
    }

    private boolean validarHora(String hora) {
        return hora.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$");
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(FONT_SEGOE);

        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(DORADO_UNISON, 2, true),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }
        });
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_SEGOE);
        btn.setBackground(DORADO_UNISON);
        btn.setForeground(AZUL_UNISON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DORADO_UNISON, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

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
}