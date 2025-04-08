package org.ellipticCurveFinal;

import org.scrum1_6.RSAUTF8;
import org.scrum1_6.RSAUtils;
import org.scrum1_6.RSAUtils2047;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ECCBenchmark {

    public static void main(String[] args) {
        try {
            // Schlüssel aus Dateien laden
            RSAUtils.loadKeysFromFiles();
            RSAUtils2047.loadKeysFromFiles();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
            return;
        }

        // Testtext – hier ein Beispiel aus der Star-Wars-Thematik
        String testText = "In einer weit, weit entfernten Galaxis entfaltet sich eine epische Saga. Die Geschichte beginnt mit der tyrannischen Herrschaft des Imperiums, das unter der Führung von Darth Vader und dem finsteren Imperator über unzählige Welten herrscht. In dieser düsteren Zeit lebt Luke Skywalker, ein junger Farmer auf dem Wüstenplaneten Tatooine, der von einer geheimnisvollen Macht angezogen wird. Als er den alten Jedi-Meister Obi-Wan Kenobi trifft, erfährt er von seinem wahren Erbe und der Macht, die in ihm schlummert. Luke schließt sich einer kleinen, aber entschlossenen Rebellenallianz an, angeführt von Prinzessin Leia, deren Tapferkeit und Visionen von Freiheit die Hoffnung in den Herzen der Unterdrückten neu entfachen. Gemeinsam mit dem charmanten Schmuggler Han Solo und seinem treuen Co-Piloten Chewbacca begibt sich Luke auf eine gefährliche Mission: den Todesstern, eine gewaltige Raumstation mit der Fähigkeit, ganze Planeten zu vernichten, zu zerstören. Während die Rebellen hinter geheimen Plänen her sind, die die Schwachstelle des Todessterns enthüllen, entbrennt ein Kampf zwischen Licht und Dunkelheit. Darth Vader, einst ein vielversprechender Jedi, wurde von der dunklen Seite der Macht verführt und dient nun dem Imperium. In epischen Duellen, in denen Lichtschwerter aufeinanderprallen, wird der Glaube an die Macht auf die Probe gestellt. Im Laufe der Geschichte wachsen die Helden über sich hinaus und lernen, dass der Schlüssel zur Freiheit nicht nur in der Macht liegt, sondern auch im Mut, die Wahrheit zu suchen und für das Richtige zu kämpfen. Die Rebellion stellt sich den scheinbar unbezwingbaren Kräften des Imperiums entgegen und zeigt, dass selbst in den dunkelsten Zeiten das Licht der Hoffnung niemals erlischt. Diese Geschichte ist mehr als ein Kampf zwischen Gut und Böse – sie ist ein Zeugnis von Loyalität, Freundschaft und der unerschütterlichen Überzeugung, dass jeder Einzelne das Schicksal der Galaxis verändern kann.";

        int iterations = 500; // Anzahl der Wiederholungen

        List<Long> encryptionTimes = new ArrayList<>();
        List<Long> decryptionTimes = new ArrayList<>();

        // Erzeuge eine Instanz der RSAUTF8-Klasse (Schlüssel werden intern geladen)
        RSAUTF8 rsa = new RSAUTF8(2047);

        // Wir messen hier, wie lange es dauert, den Testtext zu verschlüsseln und wieder zu entschlüsseln.
        // In diesem Beispiel verschlüsselt Bob eine Nachricht für Alice:
        // -> Beim Verschlüsseln wird "true" übergeben, sodass intern Bobs öffentlicher Schlüssel verwendet wird.
        // -> Beim Entschlüsseln wird "false" übergeben, sodass Bobs privater Schlüssel genutzt wird.
        // (Die Schlüssel müssen natürlich stimmen!)
        for (int i = 0; i < iterations; i++) {
            // Verschlüsselung messen
            long startEnc = System.currentTimeMillis();
            RSAUTF8.RSAResult encResult = rsa.encrypt(testText, true);
            long endEnc = System.currentTimeMillis();
            encryptionTimes.add(endEnc - startEnc);

            // Um die Entschlüsselung zu testen, muss das verschlüsselte Chiffrat
            // in den ursprünglichen Block-Format (BigInteger-Liste) zurückkonvertiert werden.
            // Hier nutzen wir die Methode cp437StringToBlocks, die den String in Blöcke umwandelt.
            String cp437String = RSAUTF8.blocksToCp437String(encResult.blocks, RSAUtils.getBobModulus());
            List<BigInteger> recoveredBlocks = RSAUTF8.cp437StringToBlocks(cp437String, RSAUtils.getBobModulus());
            RSAUTF8.RSAResult recoveredResult = new RSAUTF8.RSAResult(recoveredBlocks);

            // Entschlüsselung messen
            long startDec = System.currentTimeMillis();
            String decryptedText = rsa.decrypt(recoveredResult, false);
            long endDec = System.currentTimeMillis();
            decryptionTimes.add(endDec - startDec);

            // Optional: Prüfen, ob die entschlüsselte Nachricht korrekt ist
            if (!decryptedText.equals(testText)) {
                System.err.println("Fehler in Iteration " + i + ": Entschlüsselte Nachricht stimmt nicht überein!");
            }
        }

        // Statistik berechnen
        double avgEnc = encryptionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgDec = decryptionTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        double stdEnc = Math.sqrt(encryptionTimes.stream()
                .mapToDouble(t -> Math.pow(t - avgEnc, 2)).average().orElse(0));
        double stdDec = Math.sqrt(decryptionTimes.stream()
                .mapToDouble(t -> Math.pow(t - avgDec, 2)).average().orElse(0));

        System.out.println("***************************");
        // Ergebnisse ausgeben
        System.out.println("Anzahl der Wiederholungen: " + iterations);
        System.out.println("Durchschnittliche Verschlüsselungszeit: " + avgEnc + " ms (Standardabweichung: " + stdEnc + ")");
        System.out.println("Durchschnittliche Entschlüsselungszeit: " + avgDec + " ms (Standardabweichung: " + stdDec + ")");
    }
}
