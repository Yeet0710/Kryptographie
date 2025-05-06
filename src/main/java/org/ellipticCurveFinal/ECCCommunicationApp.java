package org.ellipticCurveFinal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Scanner;

/**
 * ECC-Kommunikationsanwendung mit Block-Chiffrierung.
 *
 * Speichert Domain-Parameter und eigenes Schlüsselpaar in ecc_config.txt
 * und den Partner-öffentlichen Schlüssel in partner_config.txt.
 */
public class ECCCommunicationApp {
    private static final Path CONFIG = Paths.get("ecc_config.txt");
    private static final Path PARTNER_CONFIG = Paths.get("partner_config.txt");

    public static void main(String[] args) {
        try {
            // --- Eigene Konfiguration laden/erzeugen ---
            Properties props = new Properties();
            if (Files.notExists(CONFIG) || Files.size(CONFIG) == 0) {
                Files.createDirectories(CONFIG.getParent() == null ? Paths.get(".") : CONFIG.getParent());
                SecureRandom rnd = new SecureRandom();
                SecureFiniteFieldEllipticCurve sCurve = new SecureFiniteFieldEllipticCurve(256, 20);
                FiniteFieldEllipticCurve curve = sCurve.getCurve();
                BigInteger p = curve.getP();
                BigInteger q = sCurve.getQ();
                BigInteger a = FiniteFieldEllipticCurve.SumOfSquares.represent(p).x;
                ECPoint G = curve.findGenerator(q);
                BigInteger Gx = G.getX(), Gy = G.getY();
                BigInteger d;
                do { d = new BigInteger(q.bitLength(), rnd); }
                while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
                ECPoint Q = G.multiply(d, curve);
                BigInteger Qx = Q.getX(), Qy = Q.getY();
                props.setProperty("p", p.toString());
                props.setProperty("a", a.toString());
                props.setProperty("Gx", Gx.toString());
                props.setProperty("Gy", Gy.toString());
                props.setProperty("d", d.toString());
                props.setProperty("Qx", Qx.toString());
                props.setProperty("Qy", Qy.toString());
                try (var os = Files.newOutputStream(CONFIG, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    props.store(os, null);
                }
                System.out.println("Konfigurationsdatei erzeugt: " + CONFIG.toAbsolutePath());
            }
            try (var is = Files.newInputStream(CONFIG)) {
                props.load(is);
            }
            BigInteger p = new BigInteger(props.getProperty("p"));
            BigInteger a = new BigInteger(props.getProperty("a"));
            BigInteger Gx = new BigInteger(props.getProperty("Gx"));
            BigInteger Gy = new BigInteger(props.getProperty("Gy"));
            BigInteger d = new BigInteger(props.getProperty("d"));
            BigInteger Qx = new BigInteger(props.getProperty("Qx"));
            BigInteger Qy = new BigInteger(props.getProperty("Qy"));
            FiniteFieldEllipticCurve curve = new FiniteFieldEllipticCurve(p);
            BigInteger N = p.add(BigInteger.ONE).subtract(a.shiftLeft(1));
            BigInteger q = N.divide(BigInteger.valueOf(8));
            curve.setQ(q);
            ECPoint G = new FiniteFieldECPoint(Gx, Gy).normalize(curve);
            ECPoint Q = new FiniteFieldECPoint(Qx, Qy).normalize(curve);

            // --- Partner-Schlüssel laden/erfragen ---
            Properties partnerProps = new Properties();
            ECPoint partnerQ;
            if (Files.exists(PARTNER_CONFIG) && Files.size(PARTNER_CONFIG) > 0) {
                try (var pis = Files.newInputStream(PARTNER_CONFIG)) {
                    partnerProps.load(pis);
                }
                BigInteger pqx = new BigInteger(partnerProps.getProperty("Qx"));
                BigInteger pqy = new BigInteger(partnerProps.getProperty("Qy"));
                partnerQ = new FiniteFieldECPoint(pqx, pqy).normalize(curve);
                System.out.println("Partner-Konfiguration geladen.");
            } else {
                Scanner in = new Scanner(System.in);
                System.out.println("Partner-Konfig nicht gefunden. Qx der Partnergruppe:");
                BigInteger pqx = new BigInteger(in.nextLine().trim());
                System.out.println("Qy der Partnergruppe:");
                BigInteger pqy = new BigInteger(in.nextLine().trim());
                partnerProps.setProperty("Qx", pqx.toString());
                partnerProps.setProperty("Qy", pqy.toString());
                try (var pos = Files.newOutputStream(PARTNER_CONFIG, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    partnerProps.store(pos, null);
                }
                partnerQ = new FiniteFieldECPoint(pqx, pqy).normalize(curve);
                System.out.println("Partner-Konfiguration gespeichert.");
            }

            // --- Interaktion mit Block-Chiffrierung ---
            Scanner sc = new Scanner(System.in);
            System.out.println("[e] Verschlüsseln an Partner | [d] Entschlüsseln von Partner");
            String mode = sc.nextLine().trim().toLowerCase();

            if ("e".equals(mode)) {
                System.out.println("Klartext:");
                String text = sc.nextLine();
                var res = ECCBlockCipher.encrypt(text, G, partnerQ, p, q, curve);
                System.out.println("--- Verschlüsselt für Partner ---");
                System.out.println("Ciphertext: " + res.cipherText);
                System.out.println("R.x: " + res.Rx);
                System.out.println("R.y: " + res.Ry);
            } else if ("d".equals(mode)) {
                System.out.println("Ciphertext:");
                String ct = sc.nextLine();
                System.out.println("R.x:");
                BigInteger rx = new BigInteger(sc.nextLine().trim());
                System.out.println("R.y:");
                BigInteger ry = new BigInteger(sc.nextLine().trim());
                String plain = ECCBlockCipher.decrypt(ct, rx, ry, d, p, curve);
                System.out.println("--- Entschlüsselt von Partner ---");
                System.out.println(plain);
            } else {
                System.err.println("Ungültige Auswahl.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
