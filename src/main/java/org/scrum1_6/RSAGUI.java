package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RSAGUI {

    private final RSAUTF8 rsa;

    // GUI-Felder
    private final JTextField ownPublicKeyField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea publicKeyField;
    private final JTextArea signatureArea;

    // Partner-Key
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    // Gemeinsame Button-Größe (Breite, Höhe)
    private static final Dimension BUTTON_SIZE = new Dimension(150, 30);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);

    public RSAGUI() {
        rsa = new RSAUTF8(1024);

        JFrame frame = new JFrame("RSA Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 800);

        // Hauptpanel mit GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // Zeilen und Spalten definieren wir so:
        //   - col0: Label
        //   - col1: Textfeld oder TextArea
        //   - col2: Button
        // Wir legen 6 Zeilen an (für ownKey, Klartext, Chiffrat, PartnerKey, Signatur, und extra Zeile für "Verifizieren" oder was nötig ist).

        // ROW 0: Eigener öffentlicher Schlüssel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Eigener öffentlicher Schlüssel (Alice, e, n):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        ownPublicKeyField = new JTextField(RSAUtils.getAlicePublicKey() + ", " + RSAUtils.getAliceModulus(), 60);
        ownPublicKeyField.setEditable(false);
        mainPanel.add(ownPublicKeyField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton copyButton = new JButton("Schlüssel kopieren");
        setupButton(copyButton, BUTTON_SIZE, BUTTON_COLOR);
        copyButton.addActionListener(e -> copyToClipboard(ownPublicKeyField.getText()));
        mainPanel.add(copyButton, gbc);

        // ROW 1: Klartext
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Klartext:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputArea = new JTextArea(5, 60);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollInput = new JScrollPane(inputArea);
        mainPanel.add(scrollInput, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton encryptButton = new JButton("Verschlüsseln");
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
        JButton decryptButton = new JButton("Entschlüsseln");
        setupButton(decryptButton, BUTTON_SIZE, BUTTON_COLOR);
        decryptButton.addActionListener(e -> decryptMessage());
        mainPanel.add(decryptButton, gbc);

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

        // ROW 5: Nur Button "Verifizieren" in Spalte 2
        gbc.gridy = 5;
        gbc.gridx = 0;
        mainPanel.add(new JLabel(""), gbc); // leeres Label

        gbc.gridx = 1;
        mainPanel.add(new JLabel(""), gbc); // leeres Feld

        gbc.gridx = 2;
        JButton verifyButton = new JButton("Verifizieren");
        setupButton(verifyButton, BUTTON_SIZE, BUTTON_COLOR);
        verifyButton.addActionListener(e -> verifySignature());
        mainPanel.add(verifyButton, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Passt das Aussehen der Buttons an.
     */
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
        JOptionPane.showMessageDialog(null, "Schlüssel wurde kopiert!");
    }

    private void setFriendPubKey() {
        String input = publicKeyField.getText().trim();
        if (input.isEmpty()
                || input.equalsIgnoreCase("reset")
                || input.equalsIgnoreCase("null"))
        {
            friendPubKey = null;
            friendModulus = null;
            rsa.setPublicKey(null, null);
            JOptionPane.showMessageDialog(null,
                    "Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
            return;
        }
        try {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                friendPubKey = new BigInteger(parts[0].trim());
                friendModulus = new BigInteger(parts[1].trim());
                rsa.setPublicKey(friendPubKey, friendModulus);
                JOptionPane.showMessageDialog(null,
                        "Partner-Schlüssel erfolgreich übernommen!");
            } else {
                JOptionPane.showMessageDialog(null,
                        "Bitte geben Sie den öffentlichen Schlüssel und Modulus (Komma getrennt) ein.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Ungültiger öffentlicher Schlüssel!\n" + e.getMessage());
        }
    }

    private void encryptMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie einen Klartext ein.");
            return;
        }
        boolean useBobKey = (friendPubKey == null || friendModulus == null);
        if (useBobKey) {
            JOptionPane.showMessageDialog(null,
                    "Kein Partner-Schlüssel gesetzt. Es wird Bobs öffentlicher Schlüssel verwendet.");
        }

        long startEncrypt = System.currentTimeMillis();
        RSAUTF8.RSAResult result = rsa.encrypt(message, true);
        long encryptionTime = System.currentTimeMillis() - startEncrypt;
        System.out.println("Verschlüsselungszeit: " + encryptionTime + " ms");

        BigInteger usedModulus = useBobKey ? RSAUtils.getBobModulus() : friendModulus;
        String cp437String = RSAUTF8.blocksToCp437String(result.blocks, usedModulus);
        outputArea.setText(cp437String);

        // Chiffrat in Datei schreiben
        try {
            File chiffratFile = new File("chiffrat.cir");
            try (FileWriter writer = new FileWriter(chiffratFile, StandardCharsets.UTF_8)) {
                writer.write(cp437String);
            }
            JOptionPane.showMessageDialog(null, "Chiffrat wurde in die Datei 'chiffrat.cir' geschrieben.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Schreiben in die Datei: " + e.getMessage());
        }
    }

    private void decryptMessage() {
        String encryptedText = outputArea.getText().trim();
        if (encryptedText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kein Chiffrat zum Entschlüsseln!");
            return;
        }
        long startDecrypt = System.currentTimeMillis();
        List<BigInteger> recoveredBlocks = RSAUTF8.cp437StringToBlocks(encryptedText, RSAUtils.getBobModulus());
        RSAUTF8.RSAResult recoveredResult = new RSAUTF8.RSAResult(
                recoveredBlocks,
                recoveredBlocks.size() * RSAUTF8.getEncryptionBlockSize(RSAUtils.getBobModulus())
        );
        String decrypted = rsa.decrypt(recoveredResult, false);
        long decryptionTime = System.currentTimeMillis() - startDecrypt;
        System.out.println("Entschlüsselungszeit: " + decryptionTime + " ms");

        inputArea.setText(""); // Klartextfeld leeren
        outputArea.setText(decrypted); // Zeige Entschlüsselung im gleichen Feld
    }

    private void signMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht zum Signieren ein.");
            return;
        }
        try {
            BigInteger signature = RSAUtils.sign(message);
            signatureArea.setText(signature.toString());
            JOptionPane.showMessageDialog(null, "Nachricht erfolgreich signiert!");
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Signieren: " + e.getMessage());
        }
    }

    private void verifySignature() {
        String message = inputArea.getText().trim();
        String signatureText = signatureArea.getText().trim();
        if (message.isEmpty() || signatureText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte Nachricht und Signatur eingeben!");
            return;
        }
        try {
            BigInteger signature = new BigInteger(signatureText);
            boolean isValid = RSAUtils.verify(message, signature);
            JOptionPane.showMessageDialog(null,
                    "Verifikation " + (isValid ? "erfolgreich!" : "fehlgeschlagen!"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Verifikation: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
