package org.scrum1_6;

import org.ellipticCurveFinal.ECCApi;
import org.ellipticCurveFinal.ECCSignature;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

public class ECCGUI {

    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JTextArea decryptedArea;
    private final JTextArea publicKeyArea;
    private final JTextArea privateKeyArea;
    private final JLabel durationLabel;
    private final JLabel allgemeinLabel;
    // Felder für Bitlänge und Miller-Rabin
    private final JTextField bitlengthField;
    private final JTextField millerRabinField;
    private final JTextArea sigField;

    private ECCApi api;
    private ECCSignature.Signature lastSignature;

    public ECCGUI() {
        //Standardwerte
        api = ECCApi.getInstance(256, 20);

        JFrame frame = new JFrame("ECC-Verschlüsselung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        //Panel für Schlüssel-Generierung
        JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        paramPanel.setBackground(Color.LIGHT_GRAY);
        paramPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        paramPanel.add(new JLabel("<html><b>Bitlänge:</b></html>"));
        // Hier sind 5 Spalten angegeben – das Feld bleibt also klein
        bitlengthField = new JTextField("256", 5);
        paramPanel.add(bitlengthField);

        paramPanel.add(new JLabel("<html><b>Miller-Rabin:</b></html>"));
        millerRabinField = new JTextField("20", 5);
        paramPanel.add(millerRabinField);

        JButton setParamsButton = createButton("Parameter setzen", e -> setParameters());
        paramPanel.add(setParamsButton);

        paramPanel.add(new JLabel("<html><b>Signatur:</b></html>"));
        sigField = new JTextArea(2, 40);
        paramPanel.add(sigField);

        JButton signButton = createButton("Sign", e -> sign());
        paramPanel.add(signButton);

        JButton verifyButton = createButton("Verify", e -> verify());
        paramPanel.add(verifyButton);

        // Ganz oben ins mainPanel einfügen
        mainPanel.add(paramPanel);

        // Klartext-Eingabe
        mainPanel.add(createRow("Klartext:", inputArea = createTextArea(),
                createButton("Verschlüsseln", e -> encryptECC())));

        // Chiffrat-Ausgabe
        mainPanel.add(createRow("Verschlüsselter Text:", outputArea = createTextArea(),
                createButton("Entschlüsseln", e -> decryptECC())));

        // Entschlüsselter Text
        mainPanel.add(createRow("Entschlüsselter Text:", decryptedArea = createTextArea(), null));

        // Schlüsselanzeigen
        mainPanel.add(createRow("Öffentlicher Schlüssel:", publicKeyArea = createTextField("[Wird gesetzt]"), null));
        publicKeyArea.setText(api.getPublicKeyDisplay());
        mainPanel.add(createRow("Privater Schlüssel:", privateKeyArea = createTextField("[Wird gesetzt]"), null));
        privateKeyArea.setText(api.getPrivateKeyDisplay());

        //Schlüssel der Informatiker laden
        JPanel schluesselLadenPanel = new JPanel();
        schluesselLadenPanel.add(createButton("Schlüssel der Informatiker laden", e -> loadKeys()));
        mainPanel.add(schluesselLadenPanel);

        JPanel anzeigePanel = new JPanel();

        // Daueranzeige
        durationLabel = new JLabel("Dauer: - ms");
        durationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        durationLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        anzeigePanel.add(durationLabel);

        //Allgemeine Anzeige
        allgemeinLabel = new JLabel("Allgemeine Ausgabe: ");
        allgemeinLabel.setFont(new Font("Arial", Font.BOLD, 14));
        allgemeinLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        anzeigePanel.add(allgemeinLabel);
        mainPanel.add(anzeigePanel);

        frame.add(new JScrollPane(mainPanel));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadKeys() {
        api.loadDataFromFile();
        publicKeyArea.setText(api.getPublicKeyDisplay());
        privateKeyArea.setText(api.getPrivateKeyDisplay());
    }

    private void verify() {
        String text = sigField.getText();
        String[] lines = text.split("\n");
        String r = lines[0].replace("r: ", "").trim();
        String s = lines[1].replace("s: ", "").trim();
        BigInteger rB = new BigInteger(r);
        BigInteger sB = new BigInteger(s);
        api.setSig(new ECCSignature.Signature(rB, sB));
        if (api.verify(inputArea.getText())) {
            allgemeinLabel.setText("Allgemeine Ausgabe: Verfikation erfolgreich!");
        } else {
            allgemeinLabel.setText("Allgemeine Ausgabe: Verfikation nicht erfolgreich!");
        }
    }

    private void sign() {
        String input = inputArea.getText();
        api.sign(input);
        sigField.setText("r: " + api.getSig().r.toString() + "\n"
                        + "s: " + api.getSig().s.toString());
    }

    private void setParameters() {
        int bits = Integer.parseInt(bitlengthField.getText().trim());
        int mr = Integer.parseInt(millerRabinField.getText().trim());
        //Neue Instanz erzeugen
        api.generateKeysAndParameters(bits, mr);
        //Schlüssel anzeigen
        publicKeyArea.setText(api.getPublicKeyDisplay());
        privateKeyArea.setText(api.getPrivateKeyDisplay());
    }


    private JPanel createRow(String labelText, JComponent input, JButton button) {
        JPanel row = new JPanel(new BorderLayout(10, 10));
        row.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        row.setBackground(Color.LIGHT_GRAY);

        JLabel label = new JLabel("<html><b>" + labelText + "</b></html>");
        label.setPreferredSize(new Dimension(200, 30));
        row.add(label, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(input);
        scrollPane.setPreferredSize(new Dimension(600, 80));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        row.add(scrollPane, BorderLayout.CENTER);

        if (button != null) {
            button.setPreferredSize(new Dimension(230, 40));
            row.add(button, BorderLayout.EAST);
        }

        return row;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(4, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JTextArea createTextField(String text) {
        JTextArea field = new JTextArea(text, 3, 40);
        field.setEditable(false);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    // Dummy-Logik für ECC
    private void encryptECC() {
        long start = System.currentTimeMillis();
        String klartext = inputArea.getText();
        String chiffrat = "[Verschlüsselt] " + api.encrypt(klartext);
        outputArea.setText(chiffrat);
        publicKeyArea.setText("Öffentlicher ECC-Schlüssel: (x, y)");
        privateKeyArea.setText("Privater ECC-Schlüssel: d");
        durationLabel.setText("Dauer: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void decryptECC() {
        long start = System.currentTimeMillis();
        String entschlüsselt = api.decrypt(outputArea.getText().replace("[Verschlüsselt] ", ""));
        decryptedArea.setText(entschlüsselt);
        durationLabel.setText("Dauer: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ECCGUI::new);
    }
}
