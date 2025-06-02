package org.scrum1_6;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class BobDecryptionGUI extends RSAUTF8 {

    private static final Charset CP437 = Charset.forName("Cp437");

    public BobDecryptionGUI() {
        super(2048);
        System.out.println("DEBUG: BobDecryptionGUI wird initialisiert.");

        JFrame frame = new JFrame("Bob's Entschlüsselungs-Oberfläche");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new BorderLayout());
        JButton selectFileButton = new JButton("Datei auswählen (.cir)");
        panel.add(selectFileButton, BorderLayout.NORTH);

        JTextArea outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        selectFileButton.addActionListener(e -> {
            System.out.println("DEBUG: JFileChooser wird geöffnet.");
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Bitte .cir-Datei auswählen");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Chiffrat (.cir)", "cir");
            chooser.setFileFilter(filter);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                System.out.println("DEBUG: Datei ausgewählt: " + selectedFile.getAbsolutePath());
                if (selectedFile != null) {
                    try {
                        // Lese den Inhalt der Datei mit CP437 ein
                        String content = new String(Files.readAllBytes(selectedFile.toPath()), CP437);
                        System.out.println("DEBUG: Inhalt der Datei (CP437): " + content);
                        // Verwende Bobs Modulus für die Umwandlung der Blöcke
                        BigInteger modulus = RSAUtils.getBobModulus();
                        System.out.println("DEBUG: Verwendeter Modulus (Alice): " + modulus);
                        List<BigInteger> recoveredBlocks = base64StringToBlocks(content, modulus);
                        System.out.println("DEBUG: Anzahl der wiederhergestellten Blöcke: " + recoveredBlocks.size());
                        for (int i = 0; i < recoveredBlocks.size(); i++) {
                            System.out.println("DEBUG: Wiederhergestellter Block " + i + ": " + recoveredBlocks.get(i));
                        }
                        RSAResult recoveredResult = new RSAResult(recoveredBlocks);
                        // Für die Entschlüsselung von Nachrichten, die von Alice an Bob gesendet wurden,
                        // wird in decrypt() das Flag false übergeben (Bobs privater Schlüssel wird genutzt).
                        long startDecrypt = System.currentTimeMillis();
                        String decrypted = decrypt(recoveredResult, false);
                        long decryptionTime = System.currentTimeMillis() - startDecrypt;
                        System.out.println("DEBUG: Entschlüsselungszeit: " + decryptionTime + " ms");
                        outputArea.setText(decrypted);
                        System.out.println("DEBUG: Entschlüsselter Text: " + decrypted);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Fehler beim Lesen der Datei: " + ex.getMessage());
                        System.out.println("DEBUG: Fehler beim Lesen der Datei: " + ex.getMessage());
                    }
                }
            } else {
                System.out.println("DEBUG: Dateiauswahl abgebrochen.");
            }
        });

        frame.add(panel);
        frame.setVisible(true);
        System.out.println("DEBUG: BobDecryptionGUI initialisiert und sichtbar.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BobDecryptionGUI::new);
    }
}
