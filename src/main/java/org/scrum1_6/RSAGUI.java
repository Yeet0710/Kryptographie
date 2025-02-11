package org.scrum1_6;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.List;

public class RSAGUI {

    private RSAUTF8 rsa;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JLabel centerLabel;
    private JTextArea publicKeyField; // Eingabe des pubKey von Bob
    private BigInteger friendPubKey; // pubKey von Bob
    private BigInteger friendModulus; // Modulus von Bob


    public RSAGUI() {
        rsa = new RSAUTF8(1024);

        JFrame frame = new JFrame("RSA Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        // Eingabefeld für Klartext
        inputArea = new JTextArea("");
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        // Ausgabe-Textfeld für das verschlüsselte oder entschlüsselte Ergebnis
        outputArea = new JTextArea("");
        outputArea.setEditable(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        // Eingabefeld für pubKey und n
        publicKeyField = new JTextArea();
        publicKeyField.setEditable(true);
        publicKeyField.setLineWrap(true);
        publicKeyField.setWrapStyleWord(true);

        // Linke Seite der GUI: Klartext-Eingabe
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Klartext"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        textPanel.add(leftPanel);

        // Verschlüsselte Nachricht oder Entschlüsselungsergebnis
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerLabel = new JLabel("Verschlüsselter Text");
        centerPanel.add(centerLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        textPanel.add(centerPanel);

        // Panel für PubKey und n von Bob
        JPanel keyInputPanel = new JPanel(new BorderLayout());
        keyInputPanel.add(new JLabel("Öffentlicher Schlüssel und Modulus von Bob (Komma getrennt): "), BorderLayout.NORTH);
        keyInputPanel.add(publicKeyField,BorderLayout.CENTER);
        keyInputPanel.add(new JScrollPane(publicKeyField), BorderLayout.CENTER);
        textPanel.add(keyInputPanel);


        panel.add(textPanel, BorderLayout.CENTER);


        // **Schön gestaltete Buttons**
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(100,10,20,10));

        JButton encryptButton = createStyledButton("Verschlüsseln", new Color(60, 179, 113));
        JButton decryptButton = createStyledButton("Entschlüsseln", new Color(30, 144, 255));
        JButton setPublicKeyButton = createStyledButton("Übernehmen", new Color(255, 165, 0));

        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(setPublicKeyButton);

        buttonPanel.add(Box.createVerticalGlue()); // Zentriert Buttons vertikal
        buttonPanel.add(encryptButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 120))); // Abstand zwischen Buttons
        buttonPanel.add(decryptButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 120))); // Abstand zwischen Buttons
        buttonPanel.add(setPublicKeyButton);


        panel.add(buttonPanel, BorderLayout.EAST);

        JLabel publicKeyLabel = new JLabel("Eigener öffentlicher Schlüssel: " + rsa.getPublicKey());
        frame.add(publicKeyLabel, BorderLayout.NORTH);



        encryptButton.addActionListener(e -> encryptMessage());
        decryptButton.addActionListener(e -> decryptMessage());
        setPublicKeyButton.addActionListener(e -> setFriendPubKey());;

        frame.add(panel,BorderLayout.CENTER);
        frame.setVisible(true);
    }



    // Methode zum Erstellen schöner Buttons mit Hover-Effekt
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 35)); // Kleinere Buttons!
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Zentriert Buttons

        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE); //weiße schrift
        button.setFont(new Font("Arial", Font.BOLD, 14));
       // button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        button.setOpaque(true);

        // **Hover-Effekt**
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
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
