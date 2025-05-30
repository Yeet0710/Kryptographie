package org.ellipticCurveFinal;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
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
            saveDomainParameters();
        }
        generateKeyPair();
    }

    private void generateDomainParameters() {
        SecureFiniteFieldEllipticCurve sec = new SecureFiniteFieldEllipticCurve(512, 20);
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
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) return false;
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            }
            this.p = new BigInteger(props.getProperty("p"));
            this.q = new BigInteger(props.getProperty("q"));
            BigInteger gx = new BigInteger(props.getProperty("Gx"));
            BigInteger gy = new BigInteger(props.getProperty("Gy"));
            this.curve = new FiniteFieldEllipticCurve(p);
            this.curve.setQ(q);
            this.generator = new FiniteFieldECPoint(gx, gy).normalize(curve);
            if (!curve.isValidPoint(generator)) return false;
            return true;
        } catch (Exception e) {
            System.out.println("Fehler Laden Domain-Parameter: " + e.getMessage());
            return false;
        }
    }

    private void saveDomainParameters() {
        try {
            Properties props = new Properties();
            props.setProperty("p", p.toString());
            props.setProperty("q", q.toString());
            props.setProperty("Gx", generator.getX().toString());
            props.setProperty("Gy", generator.getY().toString());
            props.setProperty("bitLength", String.valueOf(p.bitLength()));
            // TODO: write to file
            System.out.println("Domain-Parameter gespeichert: p-bit="+p.bitLength());
        } catch (Exception e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
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
