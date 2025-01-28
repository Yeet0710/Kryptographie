package org.rsa;

public class euklidischerAlgorithmus {
    public static int berechnenGGT(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public static void main (String[] args) {
        int a = 252, b = 105;
        System.out.println("Größter gemeinsamer Teiler: " + berechnenGGT(a, b));
    }

}
