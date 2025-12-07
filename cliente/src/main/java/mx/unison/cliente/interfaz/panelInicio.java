package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import javax.imageio.ImageIO;
import mx.unison.cliente.main.appFrame;

public class panelInicio extends JPanel {

    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 32);

    public panelInicio(appFrame app) {
        setLayout(new BorderLayout(20, 20));
        setBackground(AZUL_UNISON);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Panel central con logo y títulos
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);

        // Cargar y mostrar logo
        JLabel logoLabel = cargarLogo();
        if (logoLabel != null) {
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centro.add(logoLabel);
            centro.add(Box.createVerticalStrut(30));
        }

        // Título principal
        JLabel titulo = new JLabel("Sistema de Monitoreo");
        titulo.setFont(FONT_TITLE);
        titulo.setForeground(DORADO_UNISON);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(titulo);

        centro.add(Box.createVerticalStrut(15));


        // Autor
        JLabel autor = new JLabel("Desarrollado por: Efren Alejandro Gonzalez");
        autor.setFont(FONT_SEGOE);
        autor.setForeground(new Color(220, 220, 220));
        autor.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(autor);

        // Panel de botones
        JPanel botones = new JPanel();
        botones.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botones.setOpaque(false);

        JButton bMonitor = crearBoton("Monitor");
        JButton bHistorico = crearBoton("Histórico");

        bMonitor.addActionListener(e -> app.mostrar(appFrame.MONITOR));
        bHistorico.addActionListener(e -> app.mostrar(appFrame.HISTORICO));

        botones.add(bMonitor);
        botones.add(bHistorico);

        add(centro, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    /**
     * Intenta cargar el logo desde resources
     * Rutas intentadas: /resources/logo_unison.png, /logo_unison.png
     */
    private JLabel cargarLogo() {
        try {
            // Intenta cargar desde resources
            InputStream is = getClass().getResourceAsStream("/resources/logo_unison.png");
            if (is == null) {
                is = getClass().getResourceAsStream("/logo_unison.png");
            }

            if (is != null) {
                Image img = ImageIO.read(is);
                // Escalar a tamaño apropiado (200x200)
                Image scaled = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled));
            } else {
                System.out.println("Logo no encontrado. Coloca 'logo_unison.png' en src/main/resources/");
            }
        } catch (Exception ex) {
            System.err.println("Error cargando logo: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Crea botones con el estilo UNISON (bordes redondeados de 4px simulados)
     */
    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(DORADO_UNISON);
        btn.setForeground(AZUL_UNISON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Borde redondeado simulado de 4px
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DORADO_UNISON, 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Efecto hover (dorado oscuro #D99E30)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0xD9, 0x9E, 0x30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_UNISON);
            }
        });

        return btn;
    }
}