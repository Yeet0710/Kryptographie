package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;

public class RSAGUI {

    private RSAUTF8 rsa;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JLabel rightLabel;


    public RSAGUI() {
        rsa = new RSAUTF8(1024);
        JFrame frame = new JFrame("RSA Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        JPanel textPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Eingabefeld für Klartext
        inputArea = new JTextArea("");
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        // Ausgabe-Textfeld für das verschlüsselte oder entschlüsselte Ergebnis
        outputArea = new JTextArea("");
        outputArea.setEditable(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        // Linke Seite der GUI: Klartext-Eingabe
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Klartext"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        textPanel.add(leftPanel);

        // Rechte Seite der GUI: Verschlüsselte Nachricht oder Entschlüsselungsergebnis
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightLabel = new JLabel("Verschlüsselter Text");
        rightPanel.add(rightLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        textPanel.add(rightPanel);
        panel.add(textPanel, BorderLayout.CENTER);

        // Erstellen der Buttons zur Verschlüsselung und Entschlüsselung
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton encryptButton = new JButton("Verschlüsseln");
        JButton decryptButton = new JButton("Entschlüsseln");

        // Platzierung der Buttons in den Panels
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftButtonPanel.add(encryptButton);
        leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);


        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.add(decryptButton);
        rightPanel.add(rightButtonPanel, BorderLayout.SOUTH);

        frame.add(panel, BorderLayout.CENTER);

        // Anzeige des öffentlichen Schlüssels
        JLabel publicKeyLabel = new JLabel("Öffentlicher Schlüssel: " + rsa.getPublicKey());
        frame.add(publicKeyLabel, BorderLayout.SOUTH);

        encryptButton.addActionListener(e -> encryptMessage());
        decryptButton.addActionListener(e -> decryptMessage());

        frame.setVisible(true);
    }

    private void decryptMessage() {
        rightLabel.setText("Entschlüsselte Nachricht");
        inputArea.setText("");

        try{
            BigInteger encrypted = new BigInteger(outputArea.getText());
            String decrypted = rsa.decrypt(encrypted);
            outputArea.setText(decrypted);
        } catch (Exception e) {
            outputArea.setText("Fehler: Ungültige verschüsselte Nachricht.");
        }
    }

    private void encryptMessage() {
        String message = inputArea.getText();
        BigInteger encrypted = rsa.encrypt(message);
        outputArea.setText(encrypted.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
