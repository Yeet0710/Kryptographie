package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.List;

public class RSAGUI {

    private RSAUTF8 rsa;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JLabel centerLabel;
    private JLabel inputLabel;
    private JTextArea publicKeyField; // Eingabe des pubKey von Bob
    private BigInteger friendPubKey; // pubKey von Bob
    private BigInteger friendModulus; // Modulus von Bob
    private JTextField ownPublicKeyField; // Eigenes Public Key-Feld


    public RSAGUI() {
        rsa = new RSAUTF8(1024);

        // Hauptfenster
        JFrame frame = new JFrame("RSA Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);

        // Hauptpanel mit GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10); // Abstand zw Komponenten
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // gleichmäßige Verteilung

        // Einheitliche Buttons
        Dimension buttonSize = new Dimension(30,50);
        Color buttonColor = new Color(70, 130, 180);

        // Breite für Textfelder anpassen
        Dimension textSize = new Dimension(600, 50);

        // Anzeige des eigenen pubKey
        gbc.gridx = 0; // erste Spalte
        gbc.gridy = 0; // erste Zeile
        gbc.weightx = 2; //Label über 2 Spalten
        panel.add(new JLabel("Eigener öffentlicher Schlüssel (e,n):"), gbc);

        gbc.gridy = 1;
        ownPublicKeyField = new JTextField(rsa.getPublicKey() + "," + rsa.getModulus());
        ownPublicKeyField.setEditable(false);
        ownPublicKeyField.setPreferredSize(textSize);
        panel.add(ownPublicKeyField, gbc);

        gbc.gridx = 2; // Button in der rechten Spalte
        gbc.weightx = 1; // wächst mit der Fenstergröße
        JButton copyButton = new JButton("Kopieren");
        setupButton(copyButton, buttonSize,buttonColor);
        copyButton.addActionListener(e -> copyToClipboard(ownPublicKeyField.getText()));
        panel.add(copyButton, gbc);


        // Eingabefeld für Klartext
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        inputLabel = new JLabel("Klartext");
        panel.add(inputLabel, gbc);

        gbc.gridy = 3;
        inputArea = new JTextArea(3, 50);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setPreferredSize(textSize);
        panel.add(new JScrollPane(inputArea), gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        JButton encryptButton = new JButton("Verschlüsseln");
        setupButton(encryptButton, buttonSize, buttonColor);
        encryptButton.addActionListener(e -> encryptMessage());
        panel.add(encryptButton, gbc);

        // Ausgabe-Textfeld für das verschlüsselte oder entschlüsselte Ergebnis
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        centerLabel = new JLabel("Verschlüsselte Nachricht");
        panel.add(centerLabel, gbc);

        gbc.gridy = 5;
        outputArea = new JTextArea(3, 50);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setPreferredSize(textSize);
        panel.add(new JScrollPane(outputArea), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        JButton decryptButton = new JButton("Entschlüsseln");
        setupButton(decryptButton, buttonSize, buttonColor);
        decryptButton.addActionListener(e -> decryptMessage());
        panel.add(decryptButton, gbc);


        // Eingabefeld für pubKey und n von Bob
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Öffentlicher Schlüssel und Modulus von Bob (Komma getrennt):"), gbc);

        gbc.gridy = 7;
        publicKeyField = new JTextArea(3, 50);
        publicKeyField.setLineWrap(true);
        publicKeyField.setWrapStyleWord(true);
        publicKeyField.setPreferredSize(textSize);
        panel.add(new JScrollPane(publicKeyField), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        JButton setPublicKeyButton = new JButton("Übernehmen");
        setupButton(setPublicKeyButton, buttonSize, buttonColor);
        setPublicKeyButton.addActionListener(e -> setFriendPubKey());
        panel.add(setPublicKeyButton, gbc);

        // Panel zum Frame hinzufügen
        frame.add(panel,BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // Methode zur einheitlichen Button-Styling
    private void setupButton(JButton button, Dimension size, Color bgColor) {
        button.setPreferredSize(size);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    // Kopieren in die Zwischenablage
    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        JOptionPane.showMessageDialog(null, "Öffentlicher Schlüssel kopiert!");
    }


    private void setFriendPubKey() {
        try {
            //Nutzer gibt den PubKey und Modulus durch koma getrennt
            String[] parts = publicKeyField.getText().split(",");
            if(parts.length == 2) {
                friendPubKey = new BigInteger(parts[0].trim());
                friendModulus = new BigInteger(parts[1].trim());
                JOptionPane.showMessageDialog(null, "Öffentlicher Schlüssel des Kommunikationspartners gesetzt!");
            } else {
                JOptionPane.showMessageDialog(null, "Bitte geben Sie den öffentlichen Schlüssel und Modulus getrennt durch ein Komma ein.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ungültiger öffentlicher Schlüssel!");
        }
    }

    private void decryptMessage() {
        centerLabel.setText("Entschlüsselte Nachricht");
        inputArea.setText("");

        try{
            String encryptedText = outputArea.getText();
            if (encryptedText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Kein verschlüsselter Text zum Entschlüsseln!");
                return;
            }

            List<BigInteger> encryptedBlocks = parseBigIntegerList(encryptedText);
            String decrypted = rsa.decrypt(encryptedBlocks);
            outputArea.setText(decrypted);
        } catch (Exception e) {
            outputArea.setText("Fehler: Ungültige verschüsselte Nachricht.");
        }
    }

    private void encryptMessage() {
        if (friendPubKey == null || friendModulus == null) {
            JOptionPane.showMessageDialog(null, "Bitte zuerst den öffentlichen Schlüssel des Kommunikationspartners setzen!");
            return;
        }

        String message = inputArea.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht ein.");
            return;
        }

        List<BigInteger> encryptedBlocks = rsa.encrypt(message, friendPubKey, friendModulus);

        StringBuilder encryptedText = new StringBuilder();
        for (BigInteger block : encryptedBlocks) {
            encryptedText.append(block.toString()).append("\n");
        }

        outputArea.setText(encryptedText.toString());
        centerLabel.setText("Verschlüsselte Nachricht (Senden an den Empfänger)");
    }
    // **Hilfsmethode zum Parsen der verschlüsselten Nachricht als Liste**
    private List<BigInteger> parseBigIntegerList(String text) {
        List<BigInteger> blocks = new java.util.ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            blocks.add(new BigInteger(line.trim()));
        }
        return blocks;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
