package org.ellipticCurveFinal;

import java.util.Scanner;

/**
 * Konsolenanwendung zur Demonstration der ECC-ElGamal-Verschlüsselung und -Entschlüsselung.
 * Bietet folgende Funktionen:
 * 1) Anzeige der Domain-Parameter (p, q, Generator G)
 * 2) Anzeige des aktuellen Schlüsselpaares (privat & öffentlich)
 * 3) Verschlüsselung eines Klartext-Strings
 * 4) Entschlüsselung eines Base64-Chiffretexts
 * 5) Beenden
 */
public class ECCConsoleApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ECCApi api = ECCApi.getInstance();

        boolean running = true;
        while (running) {
            System.out.println("\n=== ECC-ElGamal Konsole ===");
            System.out.println("1) Domain-Parameter anzeigen");
            System.out.println("2) Schlüsselpaar anzeigen");
            System.out.println("3) Verschlüsseln");
            System.out.println("4) Entschlüsseln");
            System.out.println("5) Beenden");
            System.out.print("Auswahl: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("--- Domain-Parameter ---");
                    System.out.println(api.getDomainParametersDisplay());
                    break;
                case "2":
                    System.out.println("--- Schlüsselpaar ---");
                    System.out.println(api.getPublicKeyDisplay());
                    System.out.println(api.getPrivateKeyDisplay());
                    break;
                case "3":
                    System.out.print("Klartext eingeben: ");
                    String plaintext = scanner.nextLine();
                    String cipherText = api.encrypt(plaintext);
                    System.out.println("Chiffretext (Base64): \n" + cipherText);
                    break;
                case "4":
                    System.out.print("Chiffretext (Base64) eingeben: ");
                    String input = scanner.nextLine();
                    try {
                        String decrypted = api.decrypt(input);
                        System.out.println("Entschlüsselter Klartext: \n" + decrypted);
                    } catch (Exception e) {
                        System.out.println("Fehler bei der Entschlüsselung: " + e.getMessage());
                    }
                    break;
                case "5":
                    running = false;
                    System.out.println("Beende Anwendung. Auf Wiedersehen!");
                    break;
                default:
                    System.out.println("Ungültige Auswahl. Bitte 1-5 wählen.");
            }
        }
        scanner.close();
    }
}
