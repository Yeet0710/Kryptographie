package org.ellipticCurveFinal;

import java.util.ArrayList;
import java.util.List;

public class ECCBenchmark {

    private static ECCApi api = ECCApi.getInstance(512, 20);

    public static void main(String[] args) {

        int iterationen = 20;
        String testTextSchwer = "In einer weit, weit entfernten Galaxis entfaltet sich eine epische Saga. Die Geschichte beginnt mit der tyrannischen Herrschaft des Imperiums, das unter der Führung von Darth Vader und dem finsteren Imperator über unzählige Welten herrscht. In dieser düsteren Zeit lebt Luke Skywalker, ein junger Farmer auf dem Wüstenplaneten Tatooine, der von einer geheimnisvollen Macht angezogen wird. Als er den alten Jedi-Meister Obi-Wan Kenobi trifft, erfährt er von seinem wahren Erbe und der Macht, die in ihm schlummert. Luke schließt sich einer kleinen, aber entschlossenen Rebellenallianz an, angeführt von Prinzessin Leia, deren Tapferkeit und Visionen von Freiheit die Hoffnung in den Herzen der Unterdrückten neu entfachen. Gemeinsam mit dem charmanten Schmuggler Han Solo und seinem treuen Co-Piloten Chewbacca begibt sich Luke auf eine gefährliche Mission: den Todesstern, eine gewaltige Raumstation mit der Fähigkeit, ganze Planeten zu vernichten, zu zerstören. Während die Rebellen hinter geheimen Plänen her sind, die die Schwachstelle des Todessterns enthüllen, entbrennt ein Kampf zwischen Licht und Dunkelheit. Darth Vader, einst ein vielversprechender Jedi, wurde von der dunklen Seite der Macht verführt und dient nun dem Imperium. In epischen Duellen, in denen Lichtschwerter aufeinanderprallen, wird der Glaube an die Macht auf die Probe gestellt. Im Laufe der Geschichte wachsen die Helden über sich hinaus und lernen, dass der Schlüssel zur Freiheit nicht nur in der Macht liegt, sondern auch im Mut, die Wahrheit zu suchen und für das Richtige zu kämpfen. Die Rebellion stellt sich den scheinbar unbezwingbaren Kräften des Imperiums entgegen und zeigt, dass selbst in den dunkelsten Zeiten das Licht der Hoffnung niemals erlischt. Diese Geschichte ist mehr als ein Kampf zwischen Gut und Böse – sie ist ein Zeugnis von Loyalität, Freundschaft und der unerschütterlichen Überzeugung, dass jeder Einzelne das Schicksal der Galaxis verändern kann.";
        String testTextChiffrat = api.encrypt(testTextSchwer);
        String testTextEinfach = "Das hier ist nur ein kurzer Text";
        String testTextEinfachChiffrat = api.encrypt(testTextEinfach);

        aufuehrung(iterationen, testTextEinfach, testTextEinfachChiffrat);

    }

    private static void aufuehrung(int iterationen, String text, String textChiffrat) {

        List<Long> encryptionTime = new ArrayList<Long>();
        List<Long> decryptionTime = new ArrayList<Long>();

        for (int i = 0; i < iterationen; i++) {
            // Verschlüsseln
            long start = System.currentTimeMillis();
            api.encrypt(text);
            long end = System.currentTimeMillis();
            encryptionTime.add(end - start);

            //Entschlüsseln
            start = System.currentTimeMillis();
            api.decrypt(textChiffrat);
            end = System.currentTimeMillis();
            decryptionTime.add(end - start);

            System.out.println(i);
        }

        double avgenc = encryptionTime.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgdec = decryptionTime.stream().mapToLong(Long::longValue).average().orElse(0);

        double stdEnc = Math.sqrt(encryptionTime.stream()
                .mapToDouble(t -> Math.pow(t - avgenc, 2)).average().orElse(0));
        double stdDec = Math.sqrt(decryptionTime.stream()
                .mapToDouble(t -> Math.pow(t - avgdec, 2)).average().orElse(0));

        System.out.println("=== Ergebnis bei 256 Bit ===");
        System.out.println("Avg EncryptionTime: " + avgenc + "ms (Standardabweichung: " + stdEnc + ")");
        System.out.println("Avg DecryptionTime: " + avgdec + "ms (Standardabweichung: " + stdDec + ")");
    }

}
