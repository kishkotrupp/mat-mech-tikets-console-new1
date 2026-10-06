package org.kishkotrupp;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConsoleIO implements IO {
    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    @Override
    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }
    @Override
    public void println(String text) {
        System.out.println(text);
    }
}