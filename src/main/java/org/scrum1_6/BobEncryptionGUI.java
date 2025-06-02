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
import java.security.NoSuchAlgorithmException;

public class BobEncryptionGUI extends RSAUTF8 {

    private final JTextField ownPublicKeyField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea recipientKeyField;
    private final JTextArea signatureArea;

    private static final Dimension BUTTON_SIZE = new Dimension(150, 30);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);

    public BobEncryptionGUI() {
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Eigener öffentlicher Schlüssel (Bob, e, n):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
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

        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Empfänger-Schlüssel (Alice, e, n):"), gbc);

        gbc.gridx = 1;
        recipientKeyField = new JTextArea(2, 60);
        recipientKeyField.setEditable(false);
        recipientKeyField.setText(RSAUtils2047.getAlicePublicKey() + ", " + RSAUtils2047.getAliceModulus());
        JScrollPane scrollRecipient = new JScrollPane(recipientKeyField);
        mainPanel.add(scrollRecipient, gbc);

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

        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Verschlüsseltes Chiffrat (Base64):"), gbc);

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

    private void saveCiphertextToFile() {
        String base64String = outputArea.getText();
        if (base64String.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kein Chiffrat vorhanden!");
            System.out.println("DEBUG: Save aborted – kein Chiffrat.");
            return;
        }
        try {
            File file = new File("chiffrat.b64");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file))) {
                writer.write(base64String);
            }
            JOptionPane.showMessageDialog(null, "Chiffrat wurde in die Datei 'chiffrat.b64' geschrieben.");
            System.out.println("DEBUG: Chiffrat erfolgreich in Datei gespeichert: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Schreiben der Datei: " + ex.getMessage());
            System.out.println("DEBUG: Fehler beim Schreiben der Datei: " + ex.getMessage());
        }
    }

    private void encryptMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie einen Klartext ein.");
            System.out.println("DEBUG: Kein Klartext eingegeben.");
            return;
        }
        System.out.println("DEBUG: Verschlüsselung gestartet für Nachricht: " + message);
        long startEncrypt = System.currentTimeMillis();
        RSAResult result = encrypt(message, false);
        long encryptionTime = System.currentTimeMillis() - startEncrypt;
        System.out.println("DEBUG: Verschlüsselungszeit: " + encryptionTime + " ms");
        System.out.println("DEBUG: Anzahl der verschlüsselten Blöcke: " + result.blocks.size());
        for (int i = 0; i < result.blocks.size(); i++) {
            System.out.println("DEBUG: Block " + i + ": " + result.blocks.get(i));
        }
        BigInteger usedModulus = RSAUtils2047.getAliceModulus();
        String base64String = blocksToBase64String(result.blocks, usedModulus);
        outputArea.setText(base64String);
        System.out.println("DEBUG: Verschlüsseltes Chiffrat (Base64): " + base64String);
    }

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
