package org.kishkotrupp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        //russ out
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        //-------

        Content content = new FileContent();
        Collection collection = new Collection(content);

        IO io = new ConsoleIO();
        new Dialog(collection, io).run();
    }
}
