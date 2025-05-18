package org.scrum1_6;

import javax.swing.*;
import java.awt.*;

public class ECCGUI {

    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea decryptedArea;
    private final JTextArea publicKeyArea;
    private final JTextArea privateKeyArea;
    private final JLabel durationLabel;

    public ECCGUI() {
        JFrame frame = new JFrame("ECC-Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        // Klartext-Eingabe
        mainPanel.add(createRow("Klartext:", inputArea = createTextArea(),
                createButton("Verschlüsseln", e -> encryptECC())));

        // Chiffrat-Ausgabe
        mainPanel.add(createRow("Verschlüsselter Text:", outputArea = createTextArea(),
                createButton("Entschlüsseln", e -> decryptECC())));

        // Entschlüsselter Text
        mainPanel.add(createRow("Entschlüsselter Text:", decryptedArea = createTextArea(), null));

        // Schlüsselanzeigen
        mainPanel.add(createRow("Öffentlicher Schlüssel:", publicKeyArea = createTextField("[Wird gesetzt]"), null));
        mainPanel.add(createRow("Privater Schlüssel:", privateKeyArea = createTextField("[Wird gesetzt]"), null));

        // Daueranzeige
        durationLabel = new JLabel("Dauer: - ms");
        durationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        durationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        mainPanel.add(durationLabel);

        frame.add(new JScrollPane(mainPanel));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createRow(String labelText, JComponent input, JButton button) {
        JPanel row = new JPanel(new BorderLayout(10, 10));
        row.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        row.setBackground(Color.LIGHT_GRAY);

        JLabel label = new JLabel("<html><b>" + labelText + "</b></html>");
        label.setPreferredSize(new Dimension(200, 30));
        row.add(label, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(input);
        scrollPane.setPreferredSize(new Dimension(600, 80));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        row.add(scrollPane, BorderLayout.CENTER);

        if (button != null) {
            button.setPreferredSize(new Dimension(230, 40));
            row.add(button, BorderLayout.EAST);
        }

        return row;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(4, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JTextArea createTextField(String text) {
        JTextArea field = new JTextArea(text, 3, 40);
        field.setEditable(false);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    // Dummy-Logik für ECC
    private void encryptECC() {
        long start = System.currentTimeMillis();
        String klartext = inputArea.getText();
        String chiffrat = "[Verschlüsselt] " + klartext;
        outputArea.setText(chiffrat);
        publicKeyArea.setText("Öffentlicher ECC-Schlüssel: (x, y)");
        privateKeyArea.setText("Privater ECC-Schlüssel: d");
        durationLabel.setText("Dauer: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void decryptECC() {
        long start = System.currentTimeMillis();
        String entschlüsselt = outputArea.getText().replace("[Verschlüsselt] ", "");
        decryptedArea.setText(entschlüsselt);
        durationLabel.setText("Dauer: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ECCGUI::new);
    }
}
