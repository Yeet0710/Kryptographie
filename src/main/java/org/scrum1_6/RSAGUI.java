package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class RSAGUI extends RSAUTF8 {

    // GUI-Komponenten
    private final JTextArea ownPublicKeyField;
    private final JTextArea keyInfoField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea publicKeyField;
    private final JTextArea signatureArea;
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    private final JSpinner spinnerKeyLength;
    private final JSpinner spinnerMRIterations;

    private static final Color BUTTON_COLOR = new Color(70, 130, 180);

    public RSAGUI() {
        super(1024);  // Lädt (wenn vorhanden) bestehende Schlüssel

        // -----------------------------------------------
        // Ganz zu Beginn des Konstruktors initialisieren:
        ownPublicKeyField = createTextField(
                RSAUtils.getAliceModulus() + ", " + RSAUtils.getAlicePublicKey()
        );
        ownPublicKeyField.setEditable(false);
        ownPublicKeyField.setBackground(Color.WHITE);
        ownPublicKeyField.setLineWrap(true);
        ownPublicKeyField.setWrapStyleWord(true);
        // -----------------------------------------------

        // Frame + Hauptpanel
        JFrame frame = new JFrame("RSA-Verschlüsselung (Alice)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.LIGHT_GRAY);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // ----------------------------------------------------------------------------
        // 1) Key‐Settings: Spinner für Bitlänge + MR‐Iterationen + Button „Schlüssel generieren“
        JPanel settingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        settingRow.setBackground(Color.LIGHT_GRAY);

        // Label + Spinner für Key‐Bitlänge
        JLabel labelKeyLength = new JLabel("<html><b>Key-Bitlänge:</b></html>");
        spinnerKeyLength = new JSpinner(new SpinnerNumberModel(1024, 512, 4096, 128));
        spinnerKeyLength.setPreferredSize(new Dimension(80, 30));

        // Label + Spinner für Miller‐Rabin‐Iterationen
        JLabel labelMR = new JLabel("<html><b>MR-Iterationen:</b></html>");
        spinnerMRIterations = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
        spinnerMRIterations.setPreferredSize(new Dimension(60, 30));

        // Button „Schlüssel generieren“
        JButton btnGenerateKeys = createButton("Schlüssel generieren", e -> {
            // 1. Lese die gewählten Werte
            int keyBits = (Integer) spinnerKeyLength.getValue();
            int mrIter  = (Integer) spinnerMRIterations.getValue();

            // 2. Erzeugen und Speichern der Schlüssel
            try {
                // Die RSAUtils.generateAndSaveKeys‐Methode
                RSAUtils.generateAndSaveKeys(
                        "rsa_e.txt",   // Alice: e
                        "rsa_n.txt",   // Alice: n
                        "rsa_d.txt",   // Alice: d
                        keyBits        // Bitlänge
                );

                // 3. Lade die frisch erzeugten Schlüssel in die statischen Felder
                RSAUtils.loadKeysFromFiles();

                // 4. Update: Zeige Alices neuen Public‐Key (n,e) in ownPublicKeyField
                ownPublicKeyField.setText(
                        RSAUtils.getAliceModulus() + ", " +
                                RSAUtils.getAlicePublicKey()
                );

                // 5. Zeige eine Erfolgsmeldung
                JOptionPane.showMessageDialog(null,
                        "Alice- und Bob-Schlüssel wurden erzeugt.\n" +
                                "Alice-Modulus (Bitlänge): " + RSAUtils.getAliceModulus().bitLength() + "\n" +
                                "Bob-Modulus   (Bitlänge): " + RSAUtils.getBobModulus().bitLength()
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Fehler beim Generieren/Speichern der Schlüssel:\n" + ex.getMessage()
                );
            }
        });

        // Key‐Info‐Feld (zeigt Zeiten, Bitlängen, ...)
        keyInfoField = new JTextArea(5, 50);
        keyInfoField.setEditable(false);
        keyInfoField.setBackground(Color.WHITE);
        keyInfoField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane keyInfoScroll = new JScrollPane(keyInfoField);
        keyInfoScroll.setPreferredSize(new Dimension(800, 100));

        // Alle Komponenten zum settingRow hinzufügen
        settingRow.add(labelKeyLength);
        settingRow.add(spinnerKeyLength);
        settingRow.add(labelMR);
        settingRow.add(spinnerMRIterations);
        settingRow.add(btnGenerateKeys);
        settingRow.add(keyInfoScroll);

        mainPanel.add(settingRow);
        // ----------------------------------------------------------------------------

        /* 2) Anzeige: Alices öffentlicher Schlüssel (n, e) + „Schlüssel kopieren“
        ownPublicKeyField = createTextField(
                (RSAUtils.getAliceModulus() != null ? RSAUtils.getAliceModulus() : BigInteger.ZERO)
                        + ", " +
                        (RSAUtils.getAlicePublicKey() != null ? RSAUtils.getAlicePublicKey() : BigInteger.ZERO)
        );

         */
        JButton btnCopyOwnKey = createButton("Schlüssel kopieren", e -> {
            copyToClipboard(ownPublicKeyField.getText());
        });
        mainPanel.add(createRow(
                "Eigener öffentlicher Schlüssel (Alice, n, e):",
                ownPublicKeyField,
                btnCopyOwnKey
        ));

        // 3) Klartext + „Verschlüsseln (Alice→Bob)“
        inputArea = createTextArea();
        JButton btnEncrypt = createButton("Verschlüsseln (Alice→Bob)", e -> encryptMessage());
        mainPanel.add(createRow("Klartext:", inputArea, btnEncrypt));

        // 4) Chiffrat (Base64) + „Chiffrat speichern“
        outputArea = createTextArea();
        JButton btnSaveCipher = createButton("Chiffrat speichern", e -> saveCiphertextToFile());
        mainPanel.add(createRow(
                "Verschlüsseltes Chiffrat (Base64):",
                outputArea,
                btnSaveCipher
        ));

        // 5) Fremd-PublicKey (n, e) + „Schlüssel übernehmen“
        publicKeyField = createTextArea();
        JButton btnSetFriendKey = createButton("Schlüssel übernehmen", e -> setFriendPubKey());
        mainPanel.add(createRow(
                "Öffentlicher Schlüssel des Partners (n, e):",
                publicKeyField,
                btnSetFriendKey
        ));

        // 6) Signatur‐Bereich
        JPanel signaturRow = new JPanel(new BorderLayout(10, 10));
        signaturRow.setBackground(Color.LIGHT_GRAY);
        signaturRow.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel labelSig = new JLabel("<html><b>Signatur:</b></html>");
        labelSig.setPreferredSize(new Dimension(250, 30));
        signaturRow.add(labelSig, BorderLayout.WEST);

        signatureArea = createTextArea();
        JScrollPane scrollPaneSig = new JScrollPane(signatureArea);
        scrollPaneSig.setPreferredSize(new Dimension(600, 80));
        scrollPaneSig.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneSig.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        signaturRow.add(scrollPaneSig, BorderLayout.CENTER);

        JPanel buttonPanelSig = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanelSig.setBackground(Color.LIGHT_GRAY);
        JButton signButton   = createButton("Signieren",  e -> signMessage());
        JButton verifyButton = createButton("Verifizieren", e -> verifySignature());
        buttonPanelSig.add(signButton);
        buttonPanelSig.add(verifyButton);
        signaturRow.add(buttonPanelSig, BorderLayout.EAST);

        mainPanel.add(signaturRow);

        // 7) Footer (Navigation)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(Color.LIGHT_GRAY);
        footerPanel.add(createButton("Zurück zur Startseite", e -> {
            frame.dispose();
            new StartView();
        }));
        footerPanel.add(createButton("Bob: Entschlüsseln", e -> new BobDecryptionGUI()));
        footerPanel.add(createButton("Alice: Entschlüsseln", e -> new AliceDecryptionGUI()));
        mainPanel.add(footerPanel);

        // 8) Frame einpacken, scrollbars aktivieren
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(mainScrollPane);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Hilfsmethode: Zeile mit Label + Input + Button
    private JPanel createRow(String labelText, JComponent input, JButton button) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 10));
        rowPanel.setBackground(Color.LIGHT_GRAY);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel("<html><b>" + labelText + "</b></html>");
        label.setPreferredSize(new Dimension(250, 30));
        rowPanel.add(label, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(input);
        scrollPane.setPreferredSize(new Dimension(600, 80));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rowPanel.add(scrollPane, BorderLayout.CENTER);

        if (button != null) {
            button.setPreferredSize(new Dimension(230, 40));
            rowPanel.add(button, BorderLayout.EAST);
        }

        return rowPanel;
    }

    // Hilfsmethode: nicht editierbares TextArea
    private JTextArea createTextField(String text) {
        JTextArea area = new JTextArea(text, 3, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        area.setFont(new JTextField().getFont());
        return area;
    }

    // Hilfsmethode: editierbares TextArea
    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(4, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    // Hilfsmethode: Button mit einheitlicher Farbe & Schrift
    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    // Kopieren in Zwischenablage
    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    // „Chiffrat speichern“-Logik
    private void saveCiphertextToFile() {
        String base64String = outputArea.getText();
        if (base64String.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kein Chiffrat vorhanden!");
            return;
        }
        try {
            File file = new File("geheim.cir");
            try (java.io.OutputStreamWriter writer =
                         new java.io.OutputStreamWriter(new java.io.FileOutputStream(file))) {
                writer.write(base64String);
            }
            JOptionPane.showMessageDialog(null, "Chiffrat wurde in Datei 'geheim.cir' geschrieben.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Schreiben: " + ex.getMessage());
        }
    }

    // Verschlüsselungs-Logik
    private void encryptMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie einen Klartext ein.");
            return;
        }
        // Verschlüsseln mit Bob's Key (oder gefallest friendKey)
        RSAResult result = encrypt(message, true);
        BigInteger usedModulus = (friendPubKey != null && friendModulus != null)
                ? friendModulus
                : RSAUtils.getBobModulus();
        String base64String = blocksToBase64String(result.blocks, usedModulus);
        outputArea.setText(base64String);

        // Zeige kurz die Bitlänge des verwendeten Modulus
        JOptionPane.showMessageDialog(null,
                "Verschlüsselung abgeschlossen.\n" +
                        "Modulus-Bitlänge: " + usedModulus.bitLength() + " Bit"
        );
    }

    // „Partner-Schlüssel übernehmen“-Logik
    private void setFriendPubKey() {
        String input = publicKeyField.getText().trim();
        if (input.isEmpty() || input.equalsIgnoreCase("reset") || input.equalsIgnoreCase("null")) {
            friendPubKey = null;
            friendModulus = null;
            setPublicKey(null, null);
            JOptionPane.showMessageDialog(null, "Partner-Schlüssel zurückgesetzt. Bobs Schlüssel wird verwendet.");
            return;
        }
        try {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                BigInteger mod = new BigInteger(parts[0].trim());
                BigInteger pub = new BigInteger(parts[1].trim());
                friendModulus = mod;
                friendPubKey = pub;
                setPublicKey(mod, pub);
                JOptionPane.showMessageDialog(null, "Partner-Schlüssel erfolgreich übernommen!");
            } else {
                JOptionPane.showMessageDialog(null,
                        "Bitte gib den öffentlichen Schlüssel und Modulus (Komma-getrennt) ein.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Ungültiger öffentlicher Schlüssel:\n" + ex.getMessage());
        }
    }

    // Signieren-Logik
    private void signMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht zum Signieren ein.");
            return;
        }
        try {
            // RSAUtils.sign(...) aufrufen
            BigInteger signature = RSAUtils.sign(message);
            signatureArea.setText(signature.toString());
            JOptionPane.showMessageDialog(null, "Nachricht erfolgreich signiert!");
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Signieren: " + ex.getMessage());
        }
    }

    // Verifizieren-Logik
    private void verifySignature() {
        String message = inputArea.getText().trim();
        String signatureText = signatureArea.getText().trim();
        if (message.isEmpty() || signatureText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie Nachricht und Signatur ein.");
            return;
        }
        try {
            // RSAUtils.verify(...) aufrufen
            BigInteger signature = new BigInteger(signatureText);
            boolean isValid = RSAUtils.verify(message, signature);
            if (isValid) {
                JOptionPane.showMessageDialog(null, "Signatur ist gültig.");
            } else {
                JOptionPane.showMessageDialog(null, "Signatur ist ungültig.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Ungültiges Signaturformat:\n" + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Verifikation:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
