package org.scrum1_10;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DateitypCip {

    /**
     * Methode zum Schreiben einer Datei mit der Endung .cip
     * @param text
     */
        public static void write(String text, String name) {

            String filepath = "src/main/resources/" + name + ".cip";

            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(text.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    /**
     * Methode zum Lesen einer Datei mit der Endung .cip
     * @param
     */
    public static String read(String name) {
        String filepath = "src/main/resources/" + name + ".cip";
        String result = "";
        try (FileInputStream fis = new FileInputStream(filepath)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            result = new String(data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
            write("Hallo", "test");
            System.out.println(read("test"));
        }

}

