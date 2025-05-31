package org.ellipticCurveFinal;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Properties;

public class ECCApi {

    private static ECCApi instance;
    private static final String CONFIG_FILE = "ecc_config.txt";

    // Domain-Parameter
    private FiniteFieldEllipticCurve curve;
    private ECPoint generator;
    private BigInteger p, q;

    // Session-Schlüsselpaar
    private BigInteger privateKey;
    private ECPoint publicKey;

    public void setBitlength(int bitlength) {
        this.bitlength = bitlength;
    }

    public void setMillerRabin(int millerRabin) {
        this.millerRabin = millerRabin;
    }

    private int bitlength = 256;
    private int millerRabin = 20;

    private ECCApi() {
        initialize();
    }

    public static synchronized ECCApi getInstance() {
        if (instance == null) {
            instance = new ECCApi();
        }
        return instance;
    }

    private void initialize() {
        System.out.println("=== ECC-System Initialisierung ===");
        if (loadDomainParametersFromFile()) {
            System.out.println("Parameter aus Datei geladen.");
        } else {
            System.out.println("Generiere neue Domain-Parameter...");
            generateDomainParameters();
        }
        generateKeyPair();
        saveDomainParameters();
    }

    private void generateDomainParameters() {
        SecureFiniteFieldEllipticCurve sec = new SecureFiniteFieldEllipticCurve(bitlength, millerRabin);
        this.curve = sec.getCurve();
        this.p = curve.getP();
        this.q = sec.getQ();
        this.generator = curve.findGenerator(q);
        System.out.println("Domain-Parameter generiert: p-bitlength=" + p.bitLength());
    }

    private void generateKeyPair() {
        SecureRandom rnd = new SecureRandom();
        do {
            privateKey = new BigInteger(q.bitLength(), rnd);
        } while (privateKey.compareTo(BigInteger.ONE) < 0 || privateKey.compareTo(q) >= 0);
        this.publicKey = generator.multiply(privateKey, curve).normalize(curve);
        System.out.println("Schlüsselpaar generiert.");
    }

    private boolean loadDomainParametersFromFile() {
        Path pfad = Path.of(CONFIG_FILE);
        if (!Files.exists(pfad)) {
            System.out.println("Datei für Public Key nicht gefunden: " + CONFIG_FILE);
            return false;
        }

        // Lese die ganze Zeile (CSV) ein:
        String line = null;
        try {
            line = Files.readString(pfad, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            System.out.println("Fehler beim einlesen der Datei: " + e);
        }
        // Erwarte genau 6 Felder (durch Komma getrennt)
        String[] parts = line.split(",");
        if (parts.length != 6) {
            System.out.println("Ungültiges Public Key‐Format (erwartet 6 Felder): " + line);
            return false;
        }

        // Konvertiere in BigInteger und ECPoint:
        try {
            // 1) Domain‐Parameter:
            p = new BigInteger(parts[0]);
            q = new BigInteger(parts[1]);
            BigInteger gx = new BigInteger(parts[2]);
            BigInteger gy = new BigInteger(parts[3]);
            generator = new FiniteFieldECPoint(gx, gy);

            // 2) Public Key:
            BigInteger yx = new BigInteger(parts[4]);
            BigInteger yy = new BigInteger(parts[5]);
            publicKey = new FiniteFieldECPoint(yx, yy);

            // 3) Rekonstruiere das Kurvenobjekt:
            FiniteFieldEllipticCurve curve1 = new FiniteFieldEllipticCurve(p);
            curve = curve1;

            return true;
        } catch (NumberFormatException nfe) {
            System.out.println("Fehler beim Parsen des Public Key: " + nfe.getMessage());
            return false;
        }
    }

    private void saveDomainParameters() {
        // 1) Baue den CSV‐String: p, q, Gx, Gy, Yx, Yy
        StringBuilder sb = new StringBuilder();
        sb.append(p.toString()).append(",");
        sb.append(q.toString()).append(",");
        sb.append(generator.getX().toString()).append(",");
        sb.append(generator.getY().toString()).append(",");
        sb.append(publicKey.getX().toString()).append(",");
        sb.append(publicKey.getY().toString());
        // (Kein abschließendes Komma, da genau sechs Felder)

        // 2) Schreibe den String in die Datei (UTF-8):
        Path pfad = Path.of(CONFIG_FILE);
        try {
            Files.write(pfad, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("Fehler bei der Datei-Speicherung: " + e);
        }
    }

    public String getDomainParametersDisplay() {
        return "p = " + p + "\nq = " + q + "\nG = (" + generator.getX() + ", " + generator.getY() + ")";
    }

    public String getPublicKeyDisplay() {
        return "Q = (" + publicKey.getX() + ", " + publicKey.getY() + ")";
    }

    public String getPrivateKeyDisplay() {
        return "x = " + privateKey;
    }

    /**
     * Verschlüsselt den Text und liefert Base64-Chiffretext.
     */
    public String encrypt(String text) {
        ECCElgamalBlockCipher.Result r = ECCElgamalBlockCipher.encrypt(
                text, generator, publicKey, p, q, curve
        );
        return r.base64;
    }

    /**
     * Dekodiert Base64 und entschlüsselt zum Klartext.
     */
    public String decrypt(String base64) {
        ECCElgamalBlockCipher.Result r = ECCElgamalBlockCipher.base64ToResult(
                base64, p, curve
        );
        return ECCElgamalBlockCipher.decrypt(
                r, privateKey, p, curve
        );
    }

    // Getter für direkte Nutzung
    public BigInteger getP() { return p; }
    public BigInteger getQ() { return q; }
    public ECPoint getG() { return generator; }
    public ECPoint getPublicKey() { return publicKey; }
    public BigInteger getPrivateKey() { return privateKey; }
    public FiniteFieldEllipticCurve getCurve() { return curve; }
}
