package main;

import lexer.Lexer;
import parser.Parser;

import java.io.File;
import java.io.IOException;

public class SimpleLanguage {

    public static void main(String[] args) throws IOException {

        File input = new File("input.txt");
        File output = new File("output.txt");
        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer, output);
            parser.program();
            System.out.println("Number of lines: " + lexer.line);
        } catch (Error e) {
            e.printStackTrace();
        }
    }
}