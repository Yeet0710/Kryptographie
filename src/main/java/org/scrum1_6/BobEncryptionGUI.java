package org.scrum1_6;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class BobEncryptionGUI extends RSAUTF8 {

    // GUI-Felder für Verschlüsselung
    private final JTextField ownPublicKeyField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea recipientKeyField;
    private final JTextArea signatureArea;

    // Gemeinsame Button-Größe und Farbe
    private static final Dimension BUTTON_SIZE = new Dimension(150, 30);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);

    // CP437-Zeichensatz
    private static final Charset CP437 = Charset.forName("Cp437");

    /**
     * Konstruktor: Erstellt die grafische Oberfläche für Bob, der für Alice verschlüsselt.
     */
    public BobEncryptionGUI() {
        //  initialisieren RSAUTF8
        super(2048);
        System.out.println("DEBUG: BobEncryptionGUI wird initialisiert.");

        JFrame frame = new JFrame("Bob's Verschlüsselungs-Oberfläche für Alice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // ROW 0: Eigener öffentlicher Schlüssel (Bobs Schlüssel)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Eigener öffentlicher Schlüssel (Bob, e, n):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        // Bobs Schlüssel aus RSAUtils
        ownPublicKeyField = new JTextField(RSAUtils.getBobPublicKey() + ", " + RSAUtils.getBobModulus(), 60);
        ownPublicKeyField.setEditable(false);
        mainPanel.add(ownPublicKeyField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton copyButton = new JButton("Schlüssel kopieren");
        setupButton(copyButton, BUTTON_SIZE, BUTTON_COLOR);
        copyButton.addActionListener(e -> {
            copyToClipboard(ownPublicKeyField.getText());
            System.out.println("DEBUG: Bobs Schlüssel in die Zwischenablage kopiert.");
        });
        mainPanel.add(copyButton, gbc);

        // ROW 1: Empfänger-Schlüssel (Alices öffentlicher Schlüssel)
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Empfänger-Schlüssel (Alice, e, n):"), gbc);

        gbc.gridx = 1;
        recipientKeyField = new JTextArea(2, 60);
        recipientKeyField.setEditable(false);
        recipientKeyField.setText(RSAUtils2047.getAlicePublicKey() + ", " + RSAUtils2047.getAliceModulus());
        JScrollPane scrollRecipient = new JScrollPane(recipientKeyField);
        mainPanel.add(scrollRecipient, gbc);

        // ROW 2: Klartext
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Klartext:"), gbc);

        gbc.gridx = 1;
        inputArea = new JTextArea(5, 60);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollInput = new JScrollPane(inputArea);
        mainPanel.add(scrollInput, gbc);

        gbc.gridx = 2;
        JButton encryptButton = new JButton("Verschlüsseln (Bob→Alice)");
        setupButton(encryptButton, BUTTON_SIZE, BUTTON_COLOR);
        encryptButton.addActionListener(e -> encryptMessage());
        mainPanel.add(encryptButton, gbc);

        // ROW 3: Chiffrat
        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Verschlüsseltes Chiffrat (CP437):"), gbc);

        gbc.gridx = 1;
        outputArea = new JTextArea(5, 60);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollOutput = new JScrollPane(outputArea);
        mainPanel.add(scrollOutput, gbc);

        gbc.gridx = 2;
        JButton saveButton = new JButton("Chiffrat speichern");
        setupButton(saveButton, BUTTON_SIZE, BUTTON_COLOR);
        saveButton.addActionListener(e -> saveCiphertextToFile());
        mainPanel.add(saveButton, gbc);

        // ROW 4: Signatur
        gbc.gridy = 4;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Signatur:"), gbc);

        gbc.gridx = 1;
        signatureArea = new JTextArea(3, 60);
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);
        JScrollPane scrollSignature = new JScrollPane(signatureArea);
        mainPanel.add(scrollSignature, gbc);

        gbc.gridx = 2;
        JButton signButton = new JButton("Signieren");
        setupButton(signButton, BUTTON_SIZE, BUTTON_COLOR);
        signButton.addActionListener(e -> signMessage());
        mainPanel.add(signButton, gbc);

        // ROW 5: Option zum Öffnen von Alices Decryption-Oberfläche
        gbc.gridy = 5;
        gbc.gridx = 2;
        JButton openAliceDecryptionButton = new JButton("Alice: Entschlüsseln");
        setupButton(openAliceDecryptionButton, BUTTON_SIZE, BUTTON_COLOR);
        openAliceDecryptionButton.addActionListener(e -> {
            System.out.println("DEBUG: Öffne Alices Entschlüsselungs-Oberfläche.");
            new AliceDecryptionGUI();
        });
        mainPanel.add(openAliceDecryptionButton, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
        System.out.println("DEBUG: BobEncryptionGUI initialisiert und sichtbar.");
    }

    // Hilfsmethode zur Button-Konfiguration
    private void setupButton(JButton button, Dimension size, Color bgColor) {
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    /**
     * Speichert den im Ausgabe-Feld enthaltenen CP437-String als Datei (chiffrat.cir).
     */
    private void saveCiphertextToFile() {
        String cp437String = outputArea.getText();
        if (cp437String.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kein Chiffrat vorhanden!");
            System.out.println("DEBUG: Save aborted – kein Chiffrat.");
            return;
        }
        try {
            File file = new File("chiffrat.cir");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), CP437)) {
                writer.write(cp437String);
            }
            JOptionPane.showMessageDialog(null, "Chiffrat wurde in die Datei 'chiffrat.cir' geschrieben.");
            System.out.println("DEBUG: Chiffrat erfolgreich in Datei gespeichert: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Schreiben der Datei: " + ex.getMessage());
            System.out.println("DEBUG: Fehler beim Schreiben der Datei: " + ex.getMessage());
        }
    }

    /**
     * Verschlüsselt den eingegebenen Klartext. Hierbei wird Alices öffentlicher Schlüssel
     * verwendet – d.h. Bob verschlüsselt für Alice.
     */
    private void encryptMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie einen Klartext ein.");
            System.out.println("DEBUG: Kein Klartext eingegeben.");
            return;
        }
        System.out.println("DEBUG: Verschlüsselung gestartet für Nachricht: " + message);
        long startEncrypt = System.currentTimeMillis();
        // Da Bob für Alice verschlüsseln soll, wird "false" übergeben,
        // sodass in RSAUTF8 der Fall verwendet wird, bei dem Alices öffentliche Schlüssel genutzt wird.
        RSAResult result = encrypt(message, false);
        long encryptionTime = System.currentTimeMillis() - startEncrypt;
        System.out.println("DEBUG: Verschlüsselungszeit: " + encryptionTime + " ms");
        System.out.println("DEBUG: Anzahl der verschlüsselten Blöcke: " + result.blocks.size());
        for (int i = 0; i < result.blocks.size(); i++) {
            System.out.println("DEBUG: Block " + i + ": " + result.blocks.get(i));
        }
        // Für die Darstellung verwenden wir Alices Modulus
        BigInteger usedModulus = RSAUtils2047.getAliceModulus();
        String cp437String = blocksToCp437String(result.blocks, usedModulus);
        outputArea.setText(cp437String);
        System.out.println("DEBUG: Verschlüsseltes Chiffrat (CP437): " + cp437String);
    }

    /**
     * Signiert den Klartext (mittels Bobs privatem Schlüssel) und zeigt die Signatur an.
     */
    private void signMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht zum Signieren ein.");
            System.out.println("DEBUG: Kein Text zum Signieren eingegeben.");
            return;
        }
        try {

            BigInteger signature = RSAUtils.sign(message);
            signatureArea.setText(signature.toString());
            JOptionPane.showMessageDialog(null, "Nachricht erfolgreich signiert!");
            System.out.println("DEBUG: Signatur erstellt: " + signature);
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Signieren: " + e.getMessage());
            System.out.println("DEBUG: Fehler beim Signieren: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BobEncryptionGUI::new);
    }
}
