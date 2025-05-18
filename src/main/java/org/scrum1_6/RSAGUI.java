package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class RSAGUI extends RSAUTF8 {

    private final JTextArea ownPublicKeyField;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea publicKeyField;
    private final JTextArea signatureArea;
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    private static final Charset CP437 = Charset.forName("Cp437");
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);

    public RSAGUI() {
        super(2048);

        JFrame frame = new JFrame("RSA-Verschlüsselung (Alice)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.LIGHT_GRAY);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createRow("Eigener öffentlicher Schlüssel (Alice, e, n):",
                ownPublicKeyField = createTextField(RSAUtils2047.getAlicePublicKey() + ", " + RSAUtils2047.getAliceModulus()),
                createButton("Schlüssel kopieren", e -> copyToClipboard(ownPublicKeyField.getText()))));

        mainPanel.add(createRow("Klartext:", inputArea = createTextArea(),
                createButton("Verschlüsseln (Alice→Bob)", e -> encryptMessage())));

        mainPanel.add(createRow("Verschlüsseltes Chiffrat (CP437):", outputArea = createTextArea(),
                createButton("Chiffrat speichern", e -> saveCiphertextToFile())));

        mainPanel.add(createRow("Öffentlicher Schlüssel des Partners (e, n):", publicKeyField = createTextArea(),
                createButton("Schlüssel übernehmen", e -> setFriendPubKey())));

        // Signaturzeile mit zwei Buttons rechts
        JPanel signaturRow = new JPanel();
        signaturRow.setBackground(Color.LIGHT_GRAY);
        signaturRow.setLayout(new BorderLayout(10, 10));
        signaturRow.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel("<html><b>Signatur:</b></html>");
        label.setPreferredSize(new Dimension(250, 30));
        signaturRow.add(label, BorderLayout.WEST);

        signatureArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(signatureArea);
        scrollPane.setPreferredSize(new Dimension(600, 80));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        signaturRow.add(scrollPane, BorderLayout.CENTER);

// Zwei Buttons untereinander rechts mit gleicher Breite wie andere Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        JButton signButton = createButton("Signieren", e -> signMessage());
        JButton verifyButton = createButton("Verifizieren", e -> verifySignature());

        signButton.setPreferredSize(new Dimension(230, 40));
        verifyButton.setPreferredSize(new Dimension(230, 40));

        buttonPanel.add(signButton);
        buttonPanel.add(verifyButton);

        signaturRow.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(signaturRow);

        // Footer mit Navigation
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Color.LIGHT_GRAY);
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.add(createButton("Zurück zur Startseite", e -> {
            frame.dispose();
            new StartView();
        }));
        footerPanel.add(createButton("Bob: Entschlüsseln", e -> new BobDecryptionGUI()));
        footerPanel.add(createButton("Alice: Entschlüsseln", e -> new AliceDecryptionGUI()));

        mainPanel.add(footerPanel);

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        frame.add(mainScrollPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createRow(String labelText, JComponent input, JButton button) {
        JPanel rowPanel = new JPanel();
        rowPanel.setBackground(Color.LIGHT_GRAY);
        rowPanel.setLayout(new BorderLayout(10, 10));
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

    private JTextArea createTextField(String text) {
        JTextArea area = new JTextArea(text, 3, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        area.setFont(new JTextField().getFont());
        return area;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(4, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    private void saveCiphertextToFile() {
        String cp437String = outputArea.getText();
        if (cp437String.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kein Chiffrat vorhanden!");
            return;
        }
        try {
            File file = new File("chiffrat.cir");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), CP437)) {
                writer.write(cp437String);
            }
            JOptionPane.showMessageDialog(null, "Chiffrat wurde in die Datei 'chiffrat.cir' geschrieben.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Schreiben der Datei: " + ex.getMessage());
        }
    }

    private void encryptMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie einen Klartext ein.");
            return;
        }
        RSAResult result = encrypt(message, true);
        BigInteger usedModulus = RSAUtils.getBobModulus();
        String cp437String = blocksToCp437String(result.blocks, usedModulus);
        outputArea.setText(cp437String);
    }

    private void setFriendPubKey() {
        String input = publicKeyField.getText().trim();
        if (input.isEmpty() || input.equalsIgnoreCase("reset") || input.equalsIgnoreCase("null")) {
            friendPubKey = null;
            friendModulus = null;
            setPublicKey(null, null);
            JOptionPane.showMessageDialog(null, "Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
            return;
        }
        try {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                friendPubKey = new BigInteger(parts[0].trim());
                friendModulus = new BigInteger(parts[1].trim());
                setPublicKey(friendPubKey, friendModulus);
                JOptionPane.showMessageDialog(null, "Partner-Schlüssel erfolgreich übernommen!");
            } else {
                JOptionPane.showMessageDialog(null, "Bitte geben Sie den öffentlichen Schlüssel und Modulus (Komma getrennt) ein.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ungültiger öffentlicher Schlüssel!\n" + e.getMessage());
        }
    }

    private void signMessage() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Nachricht zum Signieren ein.");
            return;
        }
        try {
            BigInteger signature = RSAUtils2047.sign(message);
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
            JOptionPane.showMessageDialog(null, "Bitte geben Sie sowohl Nachricht als auch Signatur ein.");
            return;
        }

        try {
            BigInteger signature = new BigInteger(signatureText);
            boolean isValid = RSAUtils2047.verify(message, signature);
            if (isValid) {
                JOptionPane.showMessageDialog(null, "Signatur ist gültig.");
            } else {
                JOptionPane.showMessageDialog(null, "Signatur ist ungültig.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Ungültiges Signaturformat: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Verifikation: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAGUI::new);
    }
}
