package org.ellipticCurveFinal;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class ECCApi {

    private static ECCApi instance;
    private static final String CONFIG_FILE = "ecc_config.txt";
    private static final String CONFIG_FILE_PRIVATE = "ecc_config_private.txt";

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

    public ECCSignature.Signature getSig() {
        return sig;
    }

    public void setSig(ECCSignature.Signature sig) {
        this.sig = sig;
    }

    private ECCSignature.Signature sig;

    private ECCApi(int bitlength, int millerRabin) {
        initialize(bitlength, millerRabin);
    }

    public static synchronized ECCApi getInstance(int bitlength, int millerRabin) {
        if (instance == null) {
            instance = new ECCApi(bitlength, millerRabin);
        }
        return instance;
    }

    private void initialize(int bitlength, int millerRabin) {
        System.out.println("=== ECC-System Initialisierung ===");
        this.bitlength = bitlength;
        this.millerRabin = millerRabin;
        if (loadDomainParametersFromFile()) {
            System.out.println("Parameter aus Datei geladen.");
        } else {
            System.out.println("Generiere neue Domain-Parameter...");
            generateDomainParameters();
        }
        if (loadPrivateKeyFromFile()) {
            System.out.println("Private Key aus der Datei geladen.");
        } else {
            generateKeyPair();
        }
        saveDomainParameters();
    }

    private boolean loadPrivateKeyFromFile() {
        Path pfad = Path.of(CONFIG_FILE_PRIVATE);
        if (!Files.exists(pfad)) {
            System.out.println("Datei für Private key nicht gefunden: " + CONFIG_FILE_PRIVATE);
        }

        String line = null;
        try {
            line = Files.readString(pfad, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            System.out.println("Fehler beim einlesen der Datei: " + e);
        }

        String[] parts = line.split(",");
        if (parts.length != 5) {
            System.out.println("Ungültiges Private Key‐Format (erwartet 5 Felder): " + line);
            return false;
        }

        try {
            // 1) Domain‐Parameter:
            p = new BigInteger(parts[0]);
            q = new BigInteger(parts[1]);
            BigInteger gx = new BigInteger(parts[2]);
            BigInteger gy = new BigInteger(parts[3]);
            generator = new FiniteFieldECPoint(gx, gy);

            // 2) Private Key:
            privateKey = new BigInteger(parts[4]);

            // 3) Rekonstruiere das Kurvenobjekt:
            curve = new FiniteFieldEllipticCurve(p);

            return true;
        } catch (NumberFormatException nfe) {
            System.out.println("Fehler beim Parsen des Private Key: " + nfe.getMessage());
            return false;
        }
    }



    public void generateKeysAndParameters(int bitlength, int millerRabin) {
        setBitlength(bitlength);
        setMillerRabin(millerRabin);
        generateDomainParameters();
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
            curve = new FiniteFieldEllipticCurve(p);

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

        // 2) Schreibe den String in die Datei (UTF-8):
        Path pfad = Path.of(CONFIG_FILE);
        try {
            Files.writeString(pfad, sb.toString());
        } catch (Exception e) {
            System.out.println("Fehler bei der Datei-Speicherung (Public key): " + e);
        }

        // 3) Baue den CSV-String für Private-Key
        StringBuilder sb2 = new StringBuilder();
        sb2.append(p.toString()).append(",");
        sb2.append(q.toString()).append(",");
        sb2.append(generator.getX().toString()).append(",");
        sb2.append(generator.getY().toString()).append(",");
        sb2.append(privateKey.toString());

        // 4) Schreibe den String in die Datei
        pfad = Path.of(CONFIG_FILE_PRIVATE);
        try {
            Files.writeString(pfad, sb2.toString());
        } catch (Exception e) {
            System.out.println("Fehler beim speichern der Datei (Private Key): " + e);
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
    public String decrypt(String text) {
        ECCElgamalBlockCipher.Result r = ECCElgamalBlockCipher.base64ToResult(
                text, p, curve
        );
        return ECCElgamalBlockCipher.decrypt(
                r, privateKey, p, curve
        );
    }

    /**
     * Signiert eine Nachricht mit dem aktuellen Private Key (ECDSA).
     *
     * @param message  Die zu signierende Nachricht (UTF-8).
     * @return ECDSA-Signatur (r,s).
     */
    public void sign(String message) {
        // Verwendet ECCSignature.sign(message, x, q, G, curve) → liefert (r,s)
        this.sig = ECCSignature.sign(
                message,
                this.privateKey,
                this.q,
                this.generator,
                this.curve
        );
    }

    /**
     * Verifiziert eine gegebene ECDSA-Signatur (r,s) für die Nachricht
     * mit dem aktuellen Public Key.
     *
     * @param message  Die signierte Nachricht (UTF-8).
     * @return true, falls gültig; false sonst.
     */
    public boolean verify(String message) {
        // Verwendet ECCSignature.verify(message, (r,s), Y, q, G, curve)
        return ECCSignature.verify(
                message,
                this.sig,
                this.publicKey,
                this.q,
                this.generator,
                this.curve
        );
    }

    public boolean loadDataFromFile() {
        String pubKeyFile = "ecc_32bit.pub";
        String privKeyFile = "ecc_32bit.key";
        Path pfad = Path.of(pubKeyFile);
        Path pfad2 = Path.of(privKeyFile);
        if (!Files.exists(pfad)) {
            System.out.println("Datei für Public Key nicht gefunden: " + pubKeyFile);
            return false;
        }
        if (!Files.exists(pfad2)) {
            System.out.println("Datei für Private Key nicht gefunden: " + privKeyFile);
        }

        // Lese die ganze Zeile (CSV) ein (PublicKey):
        String line = null;
        try {
            line = Files.readString(pfad, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            System.out.println("Fehler beim einlesen der Datei (PublicKey): " + e);
        }
        // Erwarte genau 6 Felder (durch Komma getrennt) (PublicKey)
        String[] parts = line.split(",");
        if (parts.length != 6) {
            System.out.println("Ungültiges Public Key‐Format (erwartet 6 Felder): " + line);
            return false;
        }

        // Lese die ganze Zeile (CSV) ein (PrivateKey):
        String line2 = null;
        try {
            line2 = Files.readString(pfad2, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            System.out.println("Fehler beim einlesen der Datei (PrivateKey): " + e);
        }
        String[] parts2 = line2.split(",");
        if (parts2.length != 5) {
            System.out.println("Ungültiges Private Key-Format (erwartet 5 Felder): " + line2);
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
            curve = new FiniteFieldEllipticCurve(p);

            // 4) Lese den PrivateKey ein
            privateKey = new BigInteger(parts2[4]);

            return true;
        } catch (NumberFormatException nfe) {
            System.out.println("Fehler beim Parsen des Public Key: " + nfe.getMessage());
            return false;
        }
    }

    // Getter für direkte Nutzung
    public BigInteger getP() { return p; }
    public BigInteger getQ() { return q; }
    public ECPoint getG() { return generator; }
    public ECPoint getPublicKey() { return publicKey; }
    public BigInteger getPrivateKey() { return privateKey; }
    public FiniteFieldEllipticCurve getCurve() { return curve; }
}
