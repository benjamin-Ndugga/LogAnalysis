/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * https://www.baeldung.com/java-8-streams
 *
 * @author Benjamin
 */
public class StreamsJava {

    public static void main(String[] args) throws IOException {

        Path path = Paths.get("file.csv");

        Stream<String> streamWithCharset = Files.lines(path, Charset.forName("UTF-8"));

        streamWithCharset
                .limit(100)
                //.filter((String t) -> t.startsWith("02"))
                //.map((String t) -> Integer.parseInt(t.subSequence(0, 2).toString()))
                .forEach(System.out::println);

    }

}
