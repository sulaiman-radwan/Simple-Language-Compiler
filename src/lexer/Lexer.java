package lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

public class Lexer {

    public int line = 1;
    private char peek = ' ';
    private Hashtable words = new Hashtable();
    private FileInputStream fileInputStream;

    public Lexer(File input) {
        try {
            fileInputStream = new FileInputStream(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        reserve(new Word(Tag.LITERAl, "true"));
        reserve(new Word(Tag.LITERAl, "false"));
        reserve(new Word(Tag.TYPE, "Integer"));
        reserve(new Word(Tag.TYPE, "Char"));
        reserve(new Word(Tag.TYPE, "String"));
        reserve(new Word(Tag.TYPE, "Boolean"));
        reserve(new Word(Tag.BEGIN, "begin"));
        reserve(new Word(Tag.END, "end"));
        reserve(new Word(Tag.RETURN, "return"));
        reserve(new Word(Tag.WHILE, "while"));
        reserve(new Word(Tag.DO, "do"));
        reserve(new Word(Tag.IF, "if"));
        reserve(new Word(Tag.THEN, "then"));
        reserve(new Word(Tag.REPEAT, "repeat"));
        reserve(new Word(Tag.TIMES, "times"));
        reserve(new Word(Tag.VOID, "void"));
        reserve(new Word(Tag.PRINT, "print"));

    }

    private void reserve(Word t) {
        words.put(t.lexeme, t);
    }

    public Token scan() throws IOException {
        for (; ; peek = nextChar()) {
            if (peek == ' ' || peek == '\t' || peek == '\r') ;
            else if (peek == '\n') line = line + 1;
            else break;
        }

        if (peek == '/') {
            peek = nextChar();
            char prev = peek;
            if (peek == '/') {
                for (; ; peek = nextChar()) {
                    if (peek == '\n' || peek == Tag.NULL) {
                        peek = nextChar();
                        line = line + 1;
                        return new Token(Tag.COMMENT);
                    }
                }
            } else if (peek == '*') {
                peek = nextChar();
                for (; ; peek = nextChar()) {
                    if (peek == '\n') line = line + 1;
                    else if (peek == '*') {
                        peek = nextChar();
                        if (peek == '/') {
                            peek = nextChar();
                            return new Token(Tag.COMMENT);
                        }
                    }
                    if (peek == Tag.NULL) {
                        return new Token(Tag.COMMENT);
                    }
                }
            } else {
                peek = prev;
                return new Token('/');
            }
        }

        if (Character.isDigit(peek)) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                peek = nextChar();
            } while (Character.isDigit(peek));
            return new Num(v);
        }

        if (Character.isLetter(peek)) {
            StringBuilder builder = new StringBuilder();
            do {
                builder.append(peek);
                peek = nextChar();
            } while (Character.isLetterOrDigit(peek));

            String s = builder.toString();
            Word w = (Word) words.get(s);
            if (w != null) return w;
            w = new Word(Tag.ID, s);
            words.put(s, w);
            return w;
        }

        if (peek == ':') {
            char prev = peek;
            peek = nextChar();
            if (peek == '=') {
                peek = nextChar();
                return new Token(Tag.ASSIGN);
            } else peek = prev;
        }

        if (peek == '<') {
            peek = nextChar();
            if (peek == '>') {
                peek = nextChar();
                return new Operator("<>");
            } else if (peek == '=') {
                peek = nextChar();
                return new Operator("<=");
            } else {
                return new Operator("<");
            }
        }

        if (peek == '>') {
            peek = nextChar();
            if (peek == '=') {
                peek = nextChar();
                return new Operator(">=");
            } else {
                return new Operator(">");
            }
        }

        if (peek == '=') {
            peek = nextChar();
            return new Operator("==");
        }

        if (peek == '"') {
            StringBuilder builder = new StringBuilder();
            peek = nextChar();
            do {
                builder.append(peek);
                peek = nextChar();
            } while (peek != '"' && peek != '\n' && peek != Tag.NULL);
            peek = nextChar();
            return new Literal(builder.toString());
        }

        Token t = new Token(peek);
        peek = ' ';
        return t;
    }

    private char nextChar() throws IOException {
        return (char) fileInputStream.read();
    }
}