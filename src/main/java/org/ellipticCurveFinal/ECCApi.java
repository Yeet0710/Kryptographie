package org.ellipticCurveFinal;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
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
    private boolean domainParametersLoaded = false;

    // Session Schlüsselpaar
    private BigInteger privateKey;
    private ECPoint publicKey;

    private ECCApi() {

    }

    public static synchronized ECCApi getInstance() {

        if (instance == null) {
            instance = new ECCApi();
            instance.initialize();
        }
        return instance;

    }

    private void initialize() {

        System.out.println("=== ECC-System Initialisierung ===");

        // Domain-Parameter laden oder generieren
        if (loadDomainParametersFromFile()) {
            System.out.println("Parameter aus der Datei geladen!");
        } else {
            System.out.println("Generiere neue Domain-Parameter...");
            generateDomainParameters();
            saveDomainParameters();
        }

        // Session-Schlüssel generieren
        generateKeyPair();

    }

    /**
     * Domain-Parameter generieren
     */
    private void generateDomainParameters() {

        long startTime = System.currentTimeMillis();

        SecureFiniteFieldEllipticCurve secureCurve = new SecureFiniteFieldEllipticCurve(256, 20);

        this.curve = secureCurve.getCurve();
        this.p = curve.getP();
        this.q = secureCurve.getQ();
        this.generator = curve.findGenerator(q);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Parameter generiert in " + duration + "ms");

    }

    public String getPublicKeyDisplay() {

        return "Q = (" + publicKey.getX() + ", " + publicKey.getY() + ")";

    }

    public String getPrivateKeyDisplay() {

        return "x = " + privateKey;

    }

    public String getDomainParametersDisplay() {

        return "p = " + p + "\n" +
                "q = " + q + "\n" +
                "G = (" + generator.getX() + ", " + generator.getY() + ")";
    }

    /**
     * Schlüsselpaar generieren
     */
    private void generateKeyPair() {

        SecureRandom random = new SecureRandom();

        // Privater Schlüssel x e [1, q-1]
        do {
            privateKey = new BigInteger(q.bitLength(), random);
        } while (privateKey.compareTo(BigInteger.ONE) < 0 || privateKey.compareTo(q) >= 0);

        // Öffentlicher Schlüssel y = x * g
        publicKey = generator.multiply(privateKey, curve);

    }

    private boolean loadDomainParametersFromFile() {

        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                return false;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Parameter laden
            this.p = new BigInteger(props.getProperty("p"));
            this.q = new BigInteger(props.getProperty("q"));

            BigInteger gx = new BigInteger(props.getProperty("Gx"));
            BigInteger gy = new BigInteger(props.getProperty("Gy"));

            // Kurve und Generator rekonstruieren
            this.curve = new FiniteFieldEllipticCurve(p);
            this.curve.setQ(q);
            this.generator = new FiniteFieldECPoint(gx, gy).normalize(curve);

            // Validieren
            if (!curve.isValidPoint(generator)) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Domain-Parameter: " + e.getMessage());
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

            System.out.println("Domain-Parameter gespeichert");

        } catch (Exception e) {
            System.out.println("Domain-Parameter konnten nicht gespeichert werden: " + e.getMessage());
        }s

    }

}
