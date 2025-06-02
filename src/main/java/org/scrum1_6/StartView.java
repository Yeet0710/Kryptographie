package org.scrum1_6;

import javax.swing.*;
import java.awt.*;

public class StartView {

    public StartView(){
        JFrame frame = new JFrame("Verschlüsselungsauswahl");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);  // Einheitliche Größe wie RSAGUI

        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Bitte wählen Sie eine Verschlüsselungsmethode:", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(40, 10, 40, 10));

        JButton rsaButton = new JButton("RSA-Verschlüsselung starten");
        setupButton(rsaButton);
        rsaButton.addActionListener(e -> {
            frame.dispose();
            new RSAGUI();
        });

        JButton eccButton = new JButton("ECC-Verschlüsselung starten");
        setupButton(eccButton);
        eccButton.addActionListener(e -> {
            frame.dispose();
            new ECCGUI(); // Diese Klasse muss noch implementiert werden
        });

        panel.add(label);
        panel.add(Box.createVerticalStrut(20));
        panel.add(rsaButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(eccButton);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 50));
        button.setPreferredSize(new Dimension(300, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartView::new);
    }
}



