package org.ellipticCurveFinal;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Scanner;

/**
 * ECC-Kommunikationsanwendung.
 *
 * Nutzt eine einzige Konfigurationsdatei ecc_config.txt zur Speicherung aller
 * Domain-Parameter und des eigenen Schlüsselpaares und eine Datei
 * partner_config.txt für den öffentlichen Schlüssel der Partnergruppe.
 *
 * ecc_config.txt enthält:
 *   p   = Primzahl des Körpers
 *   a   = "a" aus Sum-of-Squares (p = a^2 + b^2)
 *   Gx  = x-Koordinate des Basispunkts G
 *   Gy  = y-Koordinate des Basispunkts G
 *   d   = privater Schlüssel
 *   Qx  = x-Koordinate des öffentlichen Schlüssels Q
 *   Qy  = y-Koordinate des öffentlichen Schlüssels Q
 *
 * partner_config.txt enthält:
 *   Qx  = x-Koordinate des öffentlichen Schlüssels der Partnergruppe
 *   Qy  = y-Koordinate des öffentlichen Schlüssels der Partnergruppe
 *
 * Fehlen die Dateien oder sind leer, werden die Werte automatisch ermittelt
 * und gespeichert.
 */
public class ECCCommunicationApp {
    private static final Path CONFIG         = Paths.get("ecc_config.txt");
    private static final Path PARTNER_CONFIG = Paths.get("partner_config.txt");

    public static void main(String[] args) {
        try {
            // --- Eigene Domain-Parameter und Schlüssel ---
            Properties props = new Properties();
            if (Files.notExists(CONFIG) || Files.size(CONFIG) == 0) {
                Files.createDirectories(CONFIG.getParent() == null ? Paths.get(".") : CONFIG.getParent());
                SecureRandom rnd = new SecureRandom();

                // 1) sichere Kurve
                SecureFiniteFieldEllipticCurve sCurve = new SecureFiniteFieldEllipticCurve(256, 20);
                FiniteFieldEllipticCurve curve = sCurve.getCurve();
                BigInteger p = curve.getP();
                BigInteger q = sCurve.getQ();
                BigInteger a = FiniteFieldEllipticCurve.SumOfSquares.represent(p).x;

                // 2) Basispunkt G
                ECPoint G = curve.findGenerator(q);
                BigInteger Gx = G.getX(), Gy = G.getY();

                // 3) Schlüsselpaar (d, Q)
                BigInteger d;
                do { d = new BigInteger(q.bitLength(), rnd); }
                while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
                ECPoint Q = G.multiply(d, curve);
                BigInteger Qx = Q.getX(), Qy = Q.getY();

                // 4) Speichern
                props.setProperty("p",  p.toString());
                props.setProperty("a",  a.toString());
                props.setProperty("Gx", Gx.toString());
                props.setProperty("Gy", Gy.toString());
                props.setProperty("d",  d.toString());
                props.setProperty("Qx", Qx.toString());
                props.setProperty("Qy", Qy.toString());
                try (OutputStream os = Files.newOutputStream(CONFIG,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    props.store(os, "ECC Konfiguration und Schlüssel");
                }
                System.out.println("Konfigurationsdatei erzeugt: " + CONFIG.toAbsolutePath());
            }

            // --- Laden eigener Konfiguration ---
            try (InputStream is = Files.newInputStream(CONFIG)) {
                props.load(is);
            }
            BigInteger p  = new BigInteger(props.getProperty("p"));
            BigInteger a  = new BigInteger(props.getProperty("a"));
            BigInteger Gx = new BigInteger(props.getProperty("Gx"));
            BigInteger Gy = new BigInteger(props.getProperty("Gy"));
            BigInteger d  = new BigInteger(props.getProperty("d"));
            BigInteger Qx = new BigInteger(props.getProperty("Qx"));
            BigInteger Qy = new BigInteger(props.getProperty("Qy"));

            FiniteFieldEllipticCurve curve = new FiniteFieldEllipticCurve(p);
            BigInteger N = p.add(BigInteger.ONE).subtract(a.shiftLeft(1));
            BigInteger q = N.divide(BigInteger.valueOf(8));
            curve.setQ(q);
            ECPoint G = new FiniteFieldECPoint(Gx, Gy).normalize(curve);
            ECPoint Q = new FiniteFieldECPoint(Qx, Qy).normalize(curve);

            // --- Partner-Schlüssel laden oder erfragen ---
            Properties partnerProps = new Properties();
            ECPoint partnerQ;
            if (Files.exists(PARTNER_CONFIG) && Files.size(PARTNER_CONFIG) > 0) {
                try (InputStream pis = Files.newInputStream(PARTNER_CONFIG)) {
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
                try (OutputStream pos = Files.newOutputStream(PARTNER_CONFIG,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    partnerProps.store(pos, "Partner öffentliche ECC-Daten");
                }
                partnerQ = new FiniteFieldECPoint(pqx, pqy).normalize(curve);
                System.out.println("Partner-Konfiguration gespeichert.");
            }

            // --- Interaktion ---
            Scanner sc = new Scanner(System.in);
            System.out.println("[e] Verschlüsseln an Partner | [d] Entschlüsseln von Partner");
            String mode = sc.nextLine().trim().toLowerCase();

            if ("e".equals(mode)) {
                System.out.println("Klartext:");
                byte[] plain = sc.nextLine().getBytes("UTF-8");
                SecureRandom rnd2 = new SecureRandom();
                BigInteger k2;
                do { k2 = new BigInteger(q.bitLength(), rnd2); }
                while (k2.compareTo(BigInteger.ONE) < 0 || k2.compareTo(q) >= 0);
                ECPoint R2 = G.multiply(k2, curve);
                ECPoint shared2 = partnerQ.multiply(k2, curve);
                byte[] key2    = shared2.getX().toByteArray();
                byte[] cipher2 = xor(plain, key2);
                System.out.println("--- Verschlüsselt für Partner ---");
                System.out.println("R.x: " + R2.getX());
                System.out.println("R.y: " + R2.getY());
                System.out.println("Cipher: " + new BigInteger(1, cipher2));

            } else if ("d".equals(mode)) {
                System.out.println("R.x:");
                BigInteger rx = new BigInteger(sc.nextLine().trim());
                System.out.println("R.y:");
                BigInteger ry = new BigInteger(sc.nextLine().trim());
                System.out.println("Cipher:");
                byte[] cipher = new BigInteger(sc.nextLine().trim()).toByteArray();
                ECPoint Rpt = new FiniteFieldECPoint(rx, ry).normalize(curve);
                ECPoint sharedDec = Rpt.multiply(d, curve);
                byte[] keyDec    = sharedDec.getX().toByteArray();
                byte[] plainDec  = xor(cipher, keyDec);
                System.out.println("--- Entschlüsselt von Partner ---");
                System.out.println(new String(plainDec, "UTF-8"));

            } else {
                System.err.println("Ungültige Auswahl.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] xor(byte[] data, byte[] key) {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return out;
    }
}
