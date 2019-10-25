package Client;

import java.util.Random;

public class SaltUtil {


    private static final Integer SALT_SIZE = 8;

    public static int getEncryptTimes(){
        int encryptTimes = (int) (1 + Math.random() * 4);
        return encryptTimes;
    }

    public static String generateSalt() {
        String s = "";
        Random r = new Random();
        for (int i = 0; i < SALT_SIZE; i++) {
            s += (char) (48 + r.nextInt(    43));
        }
        return s.toLowerCase();
    }

    public static String generateSalt(int length) {
        String s = "";
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            s += (char) (48 + r.nextInt(    43));
        }
        return s.toLowerCase();
    }

    public static void main(String[] args) {
        System.out.println(generateSalt());
    }


}
