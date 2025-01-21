package org.example;

public class erweiterterEuklid {
    public static int[] erweiterereGGT(int a, int b) {
        if(b == 0) {
            return new int[]{a, 1, 0}; // {GGT, x,y}
        }
        int[] werte = erweiterereGGT(b,a % b);
        int ggt = werte[0];
        int x = werte[2];
        int y = werte[1] - (a / b) * werte[2];
        return new int[]{ggt, x, y};
    }


    public static void main(String[] args) {
        int a = 252, b = 105;
        int[] ergebnis = erweiterereGGT(a, b);
        System.out.println("Größter gemeinsamer Teiler: " + ergebnis[0] + ", x: " + ergebnis[1] + ", y: " + ergebnis[2]);
    }
}
