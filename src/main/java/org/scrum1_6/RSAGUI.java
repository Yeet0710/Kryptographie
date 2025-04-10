package org.scrum1_6;

import javax.swing.*;
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


public class RSAGUI extends RSAUTF8 {

    // GUI-Felder für Verschlüsselung
    private final JTextField ownPublicKeyField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea publicKeyField;
    private final JTextArea signatureArea;

    // Partner-Schlüssel
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    // Gemeinsame Button-Größe und Farbe
    private static final Dimension BUTTON_SIZE = new Dimension(150, 30);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    // CP437-Zeichensatz
    private static final Charset CP437 = Charset.forName("Cp437");

    /**
     * Konstruktor: Ruft den Konstruktor der Basisklasse (RSAUTF8) mit einer Bitlänge von 2048 auf,
     * erstellt die grafische Oberfläche und initialisiert alle Komponenten.
     */
    public RSAGUI() {
        super(2048);
        System.out.println("DEBUG: RSAGUI (Alice) wird initialisiert.");

        JFrame frame = new JFrame("Alice's Verschlüsselungs-Oberfläche");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // ROW 0: Eigener öffentlicher Schlüssel (Alices Schlüssel)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Eigener öffentlicher Schlüssel (Alice, e, n):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        ownPublicKeyField = new JTextField(RSAUtils2047.getAlicePublicKey() + ", " + RSAUtils2047.getAliceModulus(), 60);
        ownPublicKeyField.setEditable(false);
        mainPanel.add(ownPublicKeyField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton copyButton = new JButton("Schlüssel kopieren");
        setupButton(copyButton, BUTTON_SIZE, BUTTON_COLOR);
        copyButton.addActionListener(e -> {
            copyToClipboard(ownPublicKeyField.getText());
            System.out.println("DEBUG: Eigener Schlüssel in die Zwischenablage kopiert.");
        });
        mainPanel.add(copyButton, gbc);

        // ROW 1: Klartext
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Klartext:"), gbc);

        gbc.gridx = 1;
        inputArea = new JTextArea(5, 60);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollInput = new JScrollPane(inputArea);
        mainPanel.add(scrollInput, gbc);

        gbc.gridx = 2;
        JButton encryptButton = new JButton("Verschlüsseln (Alice→Bob)");
        setupButton(encryptButton, BUTTON_SIZE, BUTTON_COLOR);
        encryptButton.addActionListener(e -> encryptMessage());
        mainPanel.add(encryptButton, gbc);

        // ROW 2: Chiffrat
        gbc.gridy = 2;
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

        // ROW 3: Partner-Schlüssel
        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Öffentlicher Schlüssel des Partners (e, n):"), gbc);

        gbc.gridx = 1;
        publicKeyField = new JTextArea(3, 60);
        publicKeyField.setLineWrap(true);
        publicKeyField.setWrapStyleWord(true);
        JScrollPane scrollPartner = new JScrollPane(publicKeyField);
        mainPanel.add(scrollPartner, gbc);

        gbc.gridx = 2;
        JButton setPublicKeyButton = new JButton("Schlüssel übernehmen");
        setupButton(setPublicKeyButton, BUTTON_SIZE, BUTTON_COLOR);
        setPublicKeyButton.addActionListener(e -> setFriendPubKey());
        mainPanel.add(setPublicKeyButton, gbc);

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

        // ROW 5: Öffne Bob's Decryption Interface
        gbc.gridy = 5;
        gbc.gridx = 2;
        JButton openBobDecryptionButton = new JButton("Bob: Entschlüsseln");
        setupButton(openBobDecryptionButton, BUTTON_SIZE, BUTTON_COLOR);
        openBobDecryptionButton.addActionListener(e -> {
            System.out.println("DEBUG: Öffne Bob's Entschlüsselungs-Oberfläche.");
            new BobDecryptionGUI();
        });
        mainPanel.add(openBobDecryptionButton, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
        System.out.println("DEBUG: RSAGUI (Alice) initialisiert und sichtbar.");
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
     * Verschlüsselt den eingegebenen Klartext mit Alices Nachricht an Bob.
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
        // Bei "Alice → Bob" wird in RSAUTF8 beim Verschlüsseln true übergeben
        RSAResult result = encrypt(message, true);
        long encryptionTime = System.currentTimeMillis() - startEncrypt;
        System.out.println("DEBUG: Verschlüsselungszeit: " + encryptionTime + " ms");
        System.out.println("DEBUG: Anzahl der verschlüsselten Blöcke: " + result.blocks.size());
        for (int i = 0; i < result.blocks.size(); i++) {
            System.out.println("DEBUG: Block " + i + ": " + result.blocks.get(i));
        }
        // Für die Darstellung verwenden wir Bobs Modulus
        BigInteger usedModulus = RSAUtils.getBobModulus();
        String cp437String = blocksToCp437String(result.blocks, usedModulus);
        outputArea.setText(cp437String);
        System.out.println("DEBUG: Verschlüsseltes Chiffrat (CP437): " + cp437String);
    }

    /**
     * Übernimmt den in publicKeyField eingegebenen Partner-Schlüssel.
     */
    private void setFriendPubKey() {
        String input = publicKeyField.getText().trim();
        if (input.isEmpty() || input.equalsIgnoreCase("reset") || input.equalsIgnoreCase("null")) {
            friendPubKey = null;
            friendModulus = null;
            setPublicKey(null, null);
            JOptionPane.showMessageDialog(null, "Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
            System.out.println("DEBUG: Partner-Schlüssel zurückgesetzt.");
            return;
        }
        try {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                friendPubKey = new BigInteger(parts[0].trim());
                friendModulus = new BigInteger(parts[1].trim());
                setPublicKey(friendPubKey, friendModulus);
                JOptionPane.showMessageDialog(null, "Partner-Schlüssel erfolgreich übernommen!");
                System.out.println("DEBUG: Partner-Schlüssel übernommen: " + friendPubKey + ", " + friendModulus);
            } else {
                JOptionPane.showMessageDialog(null, "Bitte geben Sie den öffentlichen Schlüssel und Modulus (Komma getrennt) ein.");
                System.out.println("DEBUG: Falsches Format für Partner-Schlüssel.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ungültiger öffentlicher Schlüssel!\n" + e.getMessage());
            System.out.println("DEBUG: Fehler beim Setzen des Partner-Schlüssels: " + e.getMessage());
        }
    }

    /**
     * Signiert den Klartext (mittels Alices privatem Schlüssel) und zeigt die Signatur an.
     */
    private void signMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht zum Signieren ein.");
            System.out.println("DEBUG: Kein Text zum Signieren eingegeben.");
            return;
        }
        try {
            BigInteger signature = RSAUtils2047.sign(message);
            signatureArea.setText(signature.toString());
            JOptionPane.showMessageDialog(null, "Nachricht erfolgreich signiert!");
            System.out.println("DEBUG: Signatur erstellt: " + signature);
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Signieren: " + e.getMessage());
            System.out.println("DEBUG: Fehler beim Signieren: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
