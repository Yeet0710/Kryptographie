package org.scrum1_6;

import org.scrum1_3.schnelleExponentiation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diese Klasse implementiert ein RSA-Ver- und Entschlüsselungsverfahren für UTF‑8-Strings.
 *
 * Vorgehensweise:
 * 1. Der Klartext wird als UTF‑8-String in ein Bytearray konvertiert.
 * 2. Anschließend wird der Text blockweise in BigInteger-Blöcke umgewandelt.
 *    Die Blockgröße wird dynamisch aus dem Modulus bestimmt:
 *       - Für die Verschlüsselung: b = floor(log256(n))
 *       - Für die Darstellung als Chiffrat: b' = ceil(log256(n))
 * 3. Die Blöcke werden blockweise verschlüsselt.
 * 4. Zusätzlich wird die ursprüngliche Länge (in Byte) des Klartexts gespeichert,
 *    um beim Entschlüsseln das überflüssige Padding zu entfernen.
 * 5. Das Chiffrat wird als CP437-String ausgegeben.
 * 6. Bob entschlüsselt die Nachricht, wobei das rekonstruierte Bytearray
 *    auf die ursprüngliche Länge getrimmt wird.
 * 7. Verschlüsselungs- und Entschlüsselungszeiten werden gemessen und ausgegeben.
 *
 * Schlüsselwahl:
 * - Hier verschlüsselt Alice mit Bobs öffentlichem Schlüssel und Bob entschlüsselt mit seinem privaten Schlüssel.
 */
public class RSAUTF8 {

    // Charset für CP437, das alle 256 Byte-Werte korrekt darstellt
    private static final Charset CP437 = Charset.forName("Cp437");

    private BigInteger friendPubKey;
    private BigInteger friendModulus;


    /**
     * Hilfsklasse zur Speicherung der verschlüsselten Blöcke und der ursprünglichen Länge des Klartexts.
     */
    public static class RSAResult {
        public final List<BigInteger> blocks;
        public final int originalLength;  // Länge des originalen UTF‑8-Bytearrays

        public RSAResult(List<BigInteger> blocks, int originalLength) {
            this.blocks = blocks;
            this.originalLength = originalLength;
        }
    }

    /**
     * Konstruktor: Lädt beim Erzeugen (z.B. für Alice & Bob) die Schlüssel aus den entsprechenden Dateien.
     */
    public RSAUTF8(int bitLength) {
        try {
            RSAUtils.loadKeysFromFiles();
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }

    /**
     * Berechnet den natürlichen Logarithmus eines BigIntegers.
     * Der BigInteger wird so skaliert, dass er als double gut darstellbar ist.
     *
     * @param val der zu verarbeitende BigInteger
     * @return der natürliche Logarithmus von val
     */
    public static double logBigInteger(BigInteger val) {
        int blex = val.bitLength() - 1022;
        if (blex > 0) {
            val = val.shiftRight(blex);
        }
        double result = Math.log(val.doubleValue());
        return result + blex * Math.log(2);
    }

    /**
     * Berechnet die Blocklänge basierend auf log256(n).
     * Ist plusOne true, wird 1 addiert (b' = ceil(log256(n)) = floor(log256(n)) + 1),
     * sonst b = floor(log256(n)).
     *
     * @param modulus Der verwendete Modulus
     * @param plusOne Flag, ob 1 addiert werden soll
     * @return die berechnete Blocklänge in Byte
     */
    public static int calculateBlockSize(BigInteger modulus, boolean plusOne) {
        int blockSize = (int) Math.floor(logBigInteger(modulus) / Math.log(256));
        if (plusOne) {
            blockSize++;
        }
        return blockSize;
    }

    /**
     * Liefert die Blocklänge für die Verschlüsselung (Klartext-Blöcke): b = floor(log256(n))
     *
     * @param modulus Der verwendete Modulus
     * @return Blocklänge in Byte
     */
    public static int getEncryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, false);
    }

    /**
     * Liefert die Blocklänge für die Darstellung des Chiffrats (CP437-Blöcke): b' = ceil(log256(n))
     *
     * @param modulus Der verwendete Modulus
     * @return Blocklänge in Byte
     */
    public static int getDecryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, true);
    }

    /**
     * Wandelt einen UTF‑8-String in BigInteger-Blöcke um.
     * Der Text wird in ein UTF‑8-Bytearray konvertiert und in Blöcke der Länge
     * b = floor(log256(n)) zerlegt.
     *
     * @param text der zu verschlüsselnde Klartext
     * @param modulus der Modulus, von dem die Blocklänge abgeleitet wird
     * @return Liste von BigInteger-Blöcken
     */
    public static List<BigInteger> textToBigIntegerBlocks(final String text, final BigInteger modulus) {
        final byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        final int blockSize = getEncryptionBlockSize(modulus);
        final List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < textBytes.length; i += blockSize) {
            byte[] blockBytes = new byte[blockSize];
            int length = Math.min(blockSize, textBytes.length - i);
            System.arraycopy(textBytes, i, blockBytes, 0, length);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    /**
     * Rekonstruiert aus einer Liste von BigInteger-Blöcken ein Bytearray, wobei jeder Block exakt
     * blockLength Byte haben soll.
     * Ist ein Block zu kurz, wird er links (mit führenden Nullen) aufgefüllt;
     * ist er zu lang, werden überflüssige führende Nullen entfernt.
     *
     * @param blocks die Liste der BigInteger-Blöcke
     * @param blockLength die gewünschte Blocklänge in Byte
     * @return Bytearray, in dem alle Blöcke hintereinander angeordnet sind
     */
    public static byte[] bigIntegerBlocksToBytes(List<BigInteger> blocks, int blockLength) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (BigInteger block : blocks) {
                byte[] blockBytes = block.toByteArray();
                if (blockBytes.length < blockLength) {
                    int delta = blockLength - blockBytes.length;
                    byte[] trueBlockBytes = prependZeros(blockBytes, delta);
                    outputStream.write(trueBlockBytes);
                } else if (blockBytes.length > blockLength) {
                    byte[] trueBlockBytes = removeLeadingZeros(blockBytes);
                    outputStream.write(trueBlockBytes);
                } else {
                    outputStream.write(blockBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    /**
     * Wandelt eine Liste von BigInteger-Blöcken in einen CP437-kodierten String um.
     * Jeder Block wird dabei auf die Länge b' = ceil(log256(n)) gebracht.
     *
     * @param blocks die Liste der Chiffrat-Blöcke
     * @param modulus der verwendete Modulus
     * @return CP437-kodierter String des gesamten Chiffrats
     */
    public static String blocksToCp437String(List<BigInteger> blocks, BigInteger modulus) {
        final int modByteLength = getDecryptionBlockSize(modulus);
        byte[] allBytes = bigIntegerBlocksToBytes(blocks, modByteLength);
        return new String(allBytes, CP437);
    }

    /**
     * Wandelt einen CP437-kodierten String in eine Liste von BigInteger-Blöcken um.
     * Der String wird in Bytearrays der Länge b' = ceil(log256(n)) unterteilt.
     *
     * @param text der CP437-String (Chiffrat)
     * @param modulus der verwendete Modulus
     * @return rekonstruierte Liste von BigInteger-Blöcken
     */
    public static List<BigInteger> cp437StringToBlocks(final String text, final BigInteger modulus) {
        final byte[] allBytes = text.getBytes(CP437);
        final int modByteLength = getDecryptionBlockSize(modulus);
        final List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < allBytes.length; i += modByteLength) {
            int length = Math.min(modByteLength, allBytes.length - i);
            byte[] blockBytes = Arrays.copyOfRange(allBytes, i, i + length);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    // --- Methoden zum Padding (Auffüllen/Entfernen führender Nullen) ---

    private static byte[] prependZeros(byte[] original, int numberOfZeros) {
        byte[] newArray = new byte[original.length + numberOfZeros];
        System.arraycopy(original, 0, newArray, numberOfZeros, original.length);
        return newArray;
    }

    public static byte[] removeLeadingZeros(byte[] array) {
        int index = 0;
        while (index < array.length && array[index] == 0) {
            index++;
        }
        if (index == array.length) {
            return new byte[0];
        }
        byte[] result = new byte[array.length - index];
        System.arraycopy(array, index, result, 0, result.length);
        return result;
    }

    /**
     * Verschlüsselt einen UTF‑8-String und speichert dabei die ursprüngliche Byte-Länge.
     * Hierbei verwendet Alice Bobs öffentlichen Schlüssel.
     *
     * @param message der zu verschlüsselnde Klartext
     * @param fromAlice true, wenn Alice verschlüsselt (mit Bobs Schlüssel)
     * @return RSAResult, das die verschlüsselten Blöcke und die Originallänge enthält.
     */
    public RSAResult encrypt(String message, boolean fromAlice) {
        // Für diesen Anwendungsfall verschlüsselt Alice mit Bobs Schlüssel.
        BigInteger pubKey, modulus;
        if (fromAlice) {
            pubKey = RSAUtils.getBobPublicKey();
            modulus = RSAUtils.getBobModulus();
        } else {
            pubKey = RSAUtils.getAlicePublicKey();
            modulus = RSAUtils.getAliceModulus();
        }
        byte[] textBytes = message.getBytes(StandardCharsets.UTF_8);
        int originalLength = textBytes.length;
        List<BigInteger> blocks = textToBigIntegerBlocks(message, modulus);
        List<BigInteger> encryptedBlocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (BigInteger block : blocks) {
            BigInteger cipherBlock = schnelleExponentiation.schnelleExponentiation(block, pubKey, modulus);
            encryptedBlocks.add(cipherBlock);
        }
        long encryptionTime = System.currentTimeMillis() - startTime;
        // Die Verschlüsselungszeit wird erst nach Ausgabe des gesamten Chiffrats angezeigt.
        RSAResult result = new RSAResult(encryptedBlocks, originalLength);
        return result;
    }

    /**
     * Bob entschlüsselt die verschlüsselte Nachricht.
     * Dabei wird der rekonstruierte Bytearray auf die ursprüngliche Länge getrimmt,
     * um überflüssiges Padding zu entfernen.
     *
     * @param result Das RSAResult, das die verschlüsselten Blöcke und die Originallänge enthält.
     * @param toAlice Falls true, würde Alice entschlüsseln; hier wird angenommen, dass Bob entschlüsselt (toAlice = false)
     * @return Der entschlüsselte Klartext als UTF‑8-String.
     */
    public String decrypt(RSAResult result, boolean toAlice) {
        // Hier wird angenommen, dass Bob entschlüsselt (toAlice = false)
        BigInteger privKey, modulus;
        if (toAlice) {
            privKey = RSAUtils.getAlicePrivateKey();
            modulus = RSAUtils.getAliceModulus();
        } else {
            privKey = RSAUtils.getBobPrivateKey();
            modulus = RSAUtils.getBobModulus();
        }
        List<BigInteger> decryptedBlocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (BigInteger block : result.blocks) {
            BigInteger plainBlock = schnelleExponentiation.schnelleExponentiation(block, privKey, modulus);
            decryptedBlocks.add(plainBlock);
        }
        long decryptionTime = System.currentTimeMillis() - startTime;
        // Rekonstruiere den Bytearray der entschlüsselten Blöcke mit fester Blocklänge
        int blockSize = getEncryptionBlockSize(modulus);
        byte[] allBytes = bigIntegerBlocksToBytes(decryptedBlocks, blockSize);
        // Trimme das Bytearray auf die ursprünglich gespeicherte Länge, um das Padding zu entfernen.
        if (allBytes.length > result.originalLength) {
            allBytes = Arrays.copyOfRange(allBytes, 0, result.originalLength);
        }
        String clearText = new String(allBytes, StandardCharsets.UTF_8);
        return clearText;
    }

    public void setPublicKey(BigInteger pubKey, BigInteger modulus) {
        // Hier definierst du z. B. Felder friendPubKey, friendModulus in RSAUTF8
        // (falls du sie nicht schon hast), um den Partner-Schlüssel zu speichern:
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;

        // Optional: Ausgabe zur Bestätigung
        if (pubKey == null || modulus == null) {
            System.out.println("Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
        } else {
            System.out.println("Öffentlicher Schlüssel des Partners gesetzt: e=" + pubKey + ", n=" + modulus);
        }
    }

    /**
     * Hauptprogramm: Demonstriert den Fall "Alice verschlüsselt, Bob entschlüsselt".
     * Es werden Zwischen­ausgaben angezeigt:
     * - Bobs öffentlicher Schlüssel
     * - Berechnete Blocklängen (für Verschlüsselung und CP437-Darstellung)
     * - Einzelne verschlüsselte Blöcke (als BigInteger und als CP437‑String)
     * - Gesamtes Chiffrat (als CP437‑String)
     * - Abschließend die von Bob entschlüsselte Nachricht und die Entschlüsselungszeit.
     */
    public static void main(String[] args) {
        RSAUTF8 rsa = new RSAUTF8(1024);

        String messageAliceToBob = "Seit 19 Jahren regiert das diktatorische Imperium mit eiserner Hand über die gesamte Galaxis. Mittlerweile hat das Imperium eine gigantische Raumstation konstruiert, den Todesstern, mit genug Feuerkraft, um einen ganzen Planeten zu vernichten. Doch der Rebellen-Allianz gelang es, an die geheimen Pläne des Todessterns zu gelangen. \n" +
                "Als Prinzessin Leia die geheimen Baupläne des Todessterns den Rebellen übergeben möchte, wird ihr Raumschiff von einem imperialen „Sternzerstörer“ unter dem Kommando von Darth Vader abgefangen. Bevor Leia gefangen genommen wird, speichert sie die Pläne und einen Hilferuf an Obi-Wan Kenobi in dem Droiden R2-D2 ab. Gemeinsam mit dem Protokolldroiden C-3PO (ein humanoider Roboter) flüchtet R2-D2. Doch auf dem Wüstenplaneten Tatooine werden sie von den Jawas, einer Schrotthändlerbande, gefangen genommen. Die Jawas verkaufen die beiden Droiden an Luke Skywalkers Onkel. R2-D2 macht sich, Prinzessin Leias Anweisungen folgend, auf die Suche nach Obi-Wan Kenobi, einem Jedi-Meister und alten Freund von Leias Vater. \n" +
                "Luke Skywalker folgt R2-D2. Als er ihn findet, wird er von Sandleuten, den heimischen Wüstenbewohnern, überfallen und schließlich von Obi-Wan Kenobi, den er bisher nur als Ben Kenobi kannte, gerettet. In dessen Behausung erfährt Luke Näheres über seinen Vater. Kenobi gibt Luke das Lichtschwert, welches einst seinem Vater gehörte. \n" +
                "R2-D2 spielt jetzt Leias Hilferuf ab, und Obi-Wan bittet Luke, ihn nach Alderaan, Leias Heimatplanet, zu begleiten. Doch sein Onkel würde das nie zulassen. Sie treffen auf den zerstörten Wagen der Jawas und finden sie alle tot vor. Obi-Wan vermutet dahinter imperiale Truppen, die die gestohlenen Pläne zurückholen wollen und auf der Suche nach den Droiden sind. Luke fürchtet nun um seine Familie, da die Jawas ihnen die Droiden verkauft hatten. Als er heimkehrt, findet er seinen Onkel und seine Tante tot vor. Jetzt hält Luke nichts mehr, und er geht mit Obi-Wan zum Raumhafen Mos Eisley. Eine Patrouille der imperialen Sturmtruppen kontrolliert sie, doch Obi-Wan erreicht die Erlaubnis zur Weiterfahrt durch eine Art Suggestion, welche ihm durch die Nutzung der Macht möglich ist. In einer Bar treffen sie auf die Schmuggler Han Solo und Chewbacca, einen Wookiee. Solo ist Pilot des Millennium Falken, des – laut seiner Aussage – schnellsten Raumschiffs der Galaxis. \n" +
                "Er willigt ein, die beiden und die Droiden nach Alderaan zu bringen. Allerdings kommt es zu einem Schusswechsel, nachdem Sturmtruppen durch einen Spion die Droiden entdeckt haben. Mit Hilfe des Falken können die Angreifer vertrieben werden. An Bord beginnt Luke unter Aufsicht Obi-Wans mit Übungen zum Erlernen der Macht. Inzwischen wird Prinzessin Leia mit Hilfe eines Folterdroiden auf dem Todesstern von Darth Vader befragt, um den Standort des Stützpunktes der Rebellen zu erfahren, doch sie gibt ihn nicht preis. \n" +
                "Bei einer weiteren Vernehmung durch den Großmoff der Station und Vader droht man allerdings mit der Zerstörung ihres Heimatplaneten. Sie gibt einen Stützpunkt an, aber Großmoff Tarkin lässt ihren Heimatplaneten trotzdem zerstören, um der Galaxie die Feuerkraft zu demonstrieren. Doch der angegebene Stützpunkt ist schon lange verlassen worden, daher ordnet der Großmoff ihren Tod an. \n" +
                "Als der Falke kurz darauf im Alderaan-System den Hyperraum verlässt, befindet sich dort anstatt des Planeten Alderaan lediglich ein Trümmerfeld aus den Überresten des zerstörten Planeten. Daraufhin wird der Falke von einem Traktorstrahl an Bord des Todessterns gezogen, der sich nach der Zerstörung Alderaans immer noch vor Ort befindet. Die Crew versteckt sich an Bord und flüchtet dann in ein Kontrollzentrum des Todessterns. Obi-Wan geht alleine los, um den Traktorstrahl des Todessterns zu deaktivieren. R2-D2 findet über den Stationscomputer heraus, dass die Prinzessin an Bord inhaftiert ist. Luke und Han verkleiden sich als imperiale Sturmtruppen, Chewbacca spielt einen Gefangenen, um in den Inhaftierungsblock zu gelangen und die Prinzessin befreien zu können. \n" +
                "Da ihnen im Inhaftierungsblock der Weg abgeschnitten wird, flüchten sie in einen Müllschacht. Obi-Wan Kenobi hat inzwischen den Traktorstrahl außer Kraft gesetzt und trifft, während er auf dem Rückweg zum Falken ist, auf Darth Vader, der ihn bereits erwartet hatte und kurz vor dem Eingang zum Hangar abfängt. Das Duell zwischen ihnen wird mit Lichtschwertern ausgetragen. Inzwischen hat sich die Müllpresse in Bewegung gesetzt. Im letzten Moment kann R2-D2 die Müllpresse, angewiesen durch Luke über sein Funkgerät, abstellen. Trotz Verfolgung gelangen sie zurück zum Falken. \n" +
                "Dort sehen sie Darth Vader und Obi-Wan im Zweikampf. Als Obi-Wan Luke sieht, lässt er es bewusst zu, dass Darth Vader ihn tötet. Als Luke geschockt vom Tod des alten Meisters wie wild auf die anwesenden Sturmtruppen feuert, hört er plötzlich Obi-Wans Stimme, die ihm befiehlt zu fliehen. \n" +
                "Luke, Leia, Han, Chewbacca und den beiden Droiden gelingt daraufhin die Flucht. Diese ist allerdings nur möglich, da das Imperium ein Komplott plant und sie deswegen entkommen lässt: Darth Vader hatte zuvor einen Sender am Millennium Falken anbringen lassen, um den Standort des verborgenen Stützpunktes der Rebellen zu erfahren. \n" +
                "Die Spur führt auf den Mond Yavin IV. Dort haben die Rebellen bereits die erbeuteten Pläne des Todessterns ausgewertet und eine Schwäche gefunden – einen Lüftungsschacht. Als es zur alles entscheidenden Schlacht kommen soll, entscheidet sich Han Solo, nicht daran teilzunehmen. Er nimmt die von Luke versprochene Belohnung zur Rettung der Prinzessin an und bereitet seine Abreise vor, da die bevorstehende Schlacht seiner Ansicht nach selbstmörderisch ist. Die Rebellen indes beginnen sofort mit dem Angriff in zahlreichen X-Flüglern, da der Todesstern sich Yavin IV nähert, um den Mond zu zerstören. Während der folgenden Raumschlacht signalisieren die militärischen Berater Großmoff Tarkin, dass Gefahr für die Station besteht. Jedoch ignoriert er diese Warnung, da er kleine Einmannjäger nicht als Bedrohung für den Todesstern ansieht. Darth Vader beschließt unterdessen, die Rebellen zusätzlich direkt und nicht mehr nur mit Stationsartillerie zu bekämpfen, und lässt dazu seinen Raumjäger startklar machen. \n" +
                "Folglich wird das Feuer in dem engen Flugschacht an der Todesstern-Oberfläche eingestellt und Abfangjäger starten unter Vaders Führung die Verfolgung der Rebellenschiffe. Diese versuchen, den Todesstern an seinem Schwachpunkt anzugreifen. Durch einen Treffer in den nur zwei Meter durchmessenden Luftschacht soll eine Kettenreaktion ausgelöst werden, die in der Lage ist, den Todesstern zu vernichten. Nach zwei missglückten Versuchen seitens der Rebellen beginnt Luke mit seinem Anflug im Graben des Todessterns. Darth Vader verfolgt ihn jedoch und ist kurz davor, Luke abzuschießen, als Han Solo mit dem Falken eingreift. Er setzt Vader und seine beiden Begleiter außer Gefecht und Luke hat Zeit, seine Torpedos abzufeuern. Dabei bekommt er von Obi-Wan Hilfe, der ihn anweist, der Macht zu vertrauen. Skywalker schaltet seinen Zielcomputer ab und vertraut seiner Intuition. Er feuert, trifft und der Todesstern wird zerstört. \n" +
                "Durch Solos Angriff wird Vaders Schiff in den freien Raum hinauskatapultiert und nach gelungener Stabilisierung des Raumgleiters gelingt Vader unbemerkt die Flucht. \n" +
                "Die Rebellenschiffe kehren zurück zur Hauptbasis auf Yavin IV. Luke und Han wird von der Prinzessin eine besondere Auszeichnung verliehen.";
        System.out.println("Bobs Schlüssel: " + RSAUtils.getBobPublicKey());

        // Ausgabe der Blocklängen für Bobs Schlüssel (Alice → Bob)
        int encryptionBlockLength = getEncryptionBlockSize(RSAUtils.getBobModulus());
        int decryptionBlockLength = getDecryptionBlockSize(RSAUtils.getBobModulus());
        System.out.println("Blocklänge (Verschlüsselung) = " + encryptionBlockLength + " Byte");
        System.out.println("Blocklänge (CP437)           = " + decryptionBlockLength + " Byte");

        // Verschlüsselung: Zeitmessung starten, Nachricht verschlüsseln, dann später ausgeben.
        long startEncrypt = System.currentTimeMillis();
        RSAResult result = rsa.encrypt(messageAliceToBob, true);
        // Ausgabe der verschlüsselten Blöcke (als BigInteger)
        System.out.println("\nVerschlüsselte Blöcke (als BigInteger):");
        for (BigInteger block : result.blocks) {
            System.out.println(block);
        }

        // Gesamtes Chiffrat als CP437-String
        String cp437String = blocksToCp437String(result.blocks, RSAUtils.getBobModulus());
        System.out.println("\nGesamtes Chiffrat (Alice→Bob, CP437):\n" + cp437String);
        long encryptionTime = System.currentTimeMillis() - startEncrypt;
        System.out.println("\nVerschlüsselungszeit: " + encryptionTime + " ms");

        // Entschlüsselung: Zeitmessung starten, dann Nachricht entschlüsseln, danach Zeit ausgeben.
        long startDecrypt = System.currentTimeMillis();
        List<BigInteger> recoveredBlocks = cp437StringToBlocks(cp437String, RSAUtils.getBobModulus());
        RSAResult recoveredResult = new RSAResult(recoveredBlocks, result.originalLength);
        String decrypted = rsa.decrypt(recoveredResult, false);
        System.out.println("\nBob entschlüsselt:\n" + decrypted);
        long decryptionTime = System.currentTimeMillis() - startDecrypt;
        System.out.println("\nEntschlüsselungszeit: " + decryptionTime + " ms");
    }
}
