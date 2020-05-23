package ru;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        String mess;
        try {
            mess = reader.readLine();
        }catch (IOException e) {
            writeMessage("Something wrong. Try enter the string again");
            mess = readString();
        }
        return mess;
    }

    public static int readInt() {
        int n;
        try {
            n = Integer.parseInt(readString());
        }catch (NumberFormatException e) {
            writeMessage("It's not a number. Try again");
            n = readInt();
        }
        return n;
    }
}
