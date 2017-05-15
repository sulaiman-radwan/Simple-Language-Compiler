package parser;

import lexer.*;
import symbol.Environment;
import symbol.Symbol;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Parser {
    public StringBuilder console;
    public ArrayList<Integer> errors = new ArrayList<>();
    private Environment top;
    private Token token;
    private Lexer lexer;
    private PrintWriter printWriter;
    private StringBuffer buffer;
    private boolean error;
    private String space = "\t";
    private StringBuffer javaBuffer;
    private int line;
    private int repeatIndex;


    public Parser(Lexer lexer, File output) throws IOException {
        this.lexer = lexer;
        printWriter = new PrintWriter(output);
        buffer = new StringBuffer();
        javaBuffer = new StringBuffer();
        console = new StringBuilder();
        nextToken();
    }

    public String program() throws IOException {
        top = null;
        javaBuffer.append("public class Code{\n");
        javaBuffer.append(methods());
        javaBuffer.append("}");

        printWriter.write(javaBuffer.toString());
        printWriter.close();

        appendLineToConsole(String.format("Done with %d %s", errors.size(), errors.size() <= 1 ? "error" : "errors"), errors.size() > 0);

        return javaBuffer.toString();

    }

    private String methods() throws IOException {
        StringBuilder methods = new StringBuilder();
        methods.append(method());
        while (token.tag != Tag.NULL) {
            methods.append(method());
        }
        return methods.toString();
    }

    private String method() throws IOException {
        Environment saved = top;
        top = new Environment(top);
        String tempString = space;
        space += '\t';
        boolean hasReturn;
        String name = match(Tag.ID);
        match('(');
        String params = optParams();
        match(')');
        match(Tag.RETURN);
        String returnType = "void";
        if (token.tag == Tag.TYPE) {
            returnType = toJavaLexeme(match(Tag.TYPE));
            if (token.tag == '[') {
                returnType += match('[');
                returnType += match(']');
            }
            hasReturn = true;
        } else if (token.tag == Tag.VOID) {
            match(Tag.VOID);
            hasReturn = false;
        } else {
            error(Tag.RETURN, "return");
            hasReturn = true;
        }
        match(Tag.BEGIN);
        String decelerations = decelerations();
        String statements = optStatements();

        String optReturn = "\t";
        if (hasReturn) {
            optReturn = space;
            optReturn += match(Tag.RETURN) + " ";
            optReturn += expr();
            optReturn += match(';');
            optReturn += "\n\t";
        } else if (token.tag == Tag.RETURN) {
            optReturn = space;
            optReturn += match(Tag.RETURN);
            optReturn += match(';');
            optReturn += "\n\t";
        }
        match(Tag.END);

        String template = "\tpublic %s %s(%s){\n%s%s%s}\n";
        top = saved;
        space = tempString;
        return String.format(template, returnType, name, params, decelerations, statements, optReturn);
    }

    private String optStatements() throws IOException {
        //statement();
        StringBuilder optStatements = new StringBuilder();
        while (token.tag != Tag.RETURN && token.tag != Tag.END && token.tag != Tag.NULL) {
            optStatements.append(statement());
            optStatements.append("\n");
        }
        return optStatements.toString();
    }

    private String optParams() throws IOException {
        String optParams = "";
        optParams += param();
        while (true) {
            if (token.tag == ',') {
                optParams += match(',');
                optParams += param();
            } else return optParams;
        }
    }

    private String decelerations() throws IOException {
        String decelerations = "";
        while (true) {
            if (token.tag == Tag.TYPE) decelerations += deceleration();
            else break;
        }
        return decelerations;
    }

    private String param() throws IOException {
        String param = "";
        if (token.tag == Tag.TYPE) {
            Symbol symbol = new Symbol();
            String type = match(Tag.TYPE);
            symbol.setType(type);
            param += toJavaLexeme(type);
            String id = match(Tag.ID);
            param += id;
            if (token.tag == '[') {
                param += match('[');
                param += match(']');
            }
            if (top.get(id) == null)
                top.put(id, symbol);
            else appendLineToConsole("Error: Variable ".concat(id).concat(" is already defined in the scope"), true);
        } else if (token.tag == Tag.LITERAl) {
            param += match(Tag.LITERAl);
        }
        return param;
    }

    private String deceleration() throws IOException {
        String decelration = "";
        Symbol symbol = new Symbol();
        String type = match(Tag.TYPE);
        symbol.setType(type);
        decelration += toJavaLexeme(type);
        String id = match(Tag.ID);
        decelration += id;
        if (token.tag == '[') {
            decelration += match('[');
            decelration += match(']');
        }
        if (token.tag == Tag.ASSIGN) {
            decelration += match(Tag.ASSIGN);
            String value = expr();
            decelration += value;
            symbol.setValue(value);
        }
        if (top.get(id) == null)
            top.put(id, symbol);
        else appendLineToConsole("Error: Variable ".concat(id).concat(" is already defined in the scope"), true);
        decelration += match(';');
        return space + decelration + "\n";
    }

    private String callParams() throws IOException {
        String callParams = "";
        if (token.tag == '(' || token.tag == Tag.NUM || token instanceof Word || token.tag == Tag.LITERAl) {
            callParams += expr();
            while (true) {
                if (token.tag == ',') {
                    callParams += match(',');
                    callParams += expr();
                } else break;
            }
        }
        return callParams;
    }

    private String statement() throws IOException {
        String tempString = space;
        String statement = "";
        if (token.tag == Tag.IF) {
            statement += match(Tag.IF);
            space += '\t';
            statement += '(';
            statement += booleanExpr();
            match(Tag.THEN);
            statement += ") {\n ";
            statement += optStatements();
            match(Tag.END);
            space = tempString;
            statement += space + '}';
            match(Tag.IF);
        } else if (token.tag == Tag.WHILE) {
            statement += match(Tag.WHILE);
            space += '\t';
            statement += '(';
            statement += booleanExpr();
            statement += ") {\n ";
            match(Tag.DO);
            statement += optStatements();
            match(Tag.END);
            space = tempString;
            statement += space + "}";
            match(Tag.WHILE);
        } else if (token.tag == Tag.REPEAT) {
            statement = "for(int i%d = 0;i%d < %s;i%d++){\n";
            space += '\t';
            match(Tag.REPEAT);
            statement = String.format(statement, repeatIndex, repeatIndex, expr(), repeatIndex);
            repeatIndex++;
            match(Tag.TIMES);
            statement += optStatements();
            match(Tag.END);
            match(Tag.REPEAT);
            space = tempString;
            statement += space + '}';
            repeatIndex--;
        } else if (token.tag == Tag.ID) {
            String id = match(Tag.ID);
            String value;
            statement += id;
            if (token.tag == '(') {
                statement += match('(');
                statement += callParams();
                statement += match(')');
                statement += match(';');
            } else {
                if (token.tag == '[') {
                    statement += match('[');
                    statement += expr();
                    statement += match(']');
                }
                statement += match(Tag.ASSIGN);
                value = expr();
                statement += value;
                Symbol symbol = top.get(id);
                if (symbol != null) {
                    symbol.setValue(value);
                } else {
                    appendLineToConsole(String.format("Error at line: %d Cannot resolve symbol %s", lexer.line, id), true);
                }
                statement += match(';');
            }
        } else if (token.tag == Tag.PRINT) {
            match(Tag.PRINT);
            statement += "System.out.print";
            statement += match('(');
            statement += expr();
            statement += match(')');
            statement += match(';');
        } else {
            appendLineToConsole("line: ".concat(String.valueOf(lexer.line)).concat(" Not A Statement"), true);
            nextToken();
        }
        error = false;

        return space + statement;
    }

    private String booleanExpr() throws IOException {
        String booleanExpr = "";
        booleanExpr += expr();
        if (token.tag == Tag.COMPARATOR) {
            booleanExpr += toJavaLexeme(match(Tag.COMPARATOR));
            booleanExpr += expr();
        }
        return booleanExpr;
    }

    private String expr() throws IOException {
        StringBuilder expr = new StringBuilder();
        expr.append(term());
        while (true) {
            if (token.tag == '+') {
                expr.append(match('+'));
                expr.append(term());
                if (!error) {
                    buffer.append("+ ");
                }
            } else if (token.tag == '-') {
                expr.append(match('-'));
                expr.append(term());
                if (!error) {
                    buffer.append("- ");
                }
            } else return expr.toString();
        }
    }

    private String term() throws IOException {
        StringBuilder term = new StringBuilder();
        term.append(factor());
        while (true) {
            if (token.tag == '*') {
                term.append(match('*'));
                term.append(factor());
                if (!error) {
                    buffer.append("* ");
                }
            } else if (token.tag == '/') {
                term.append(match('/'));
                term.append(factor());
                if (!error) {
                    buffer.append("/ ");
                }
            } else return term.toString();
        }
    }

    private String factor() throws IOException {
        String factor = "";
        if (token.tag == '(') {
            factor += match('(');
            factor += expr();
            factor += match(')');
        } else if (token instanceof Num) {
            buffer.append(((Num) token).value).append(' ');
            factor += match(Tag.NUM);
        } else if (token.tag == Tag.ID) {
            buffer.append(((Word) token).lexeme).append(' ');
            String id = match(Tag.ID);
            factor += id;
            if (token.tag == '_') {
                factor += toJavaLexeme(match('_'));
                factor += match(Tag.ID);
            }
            if (token.tag == '[') {
                factor += match('[');
                factor += expr();
                factor += match(']');
            }
            if (top.get(id) == null) {
                appendLineToConsole(String.format("Error at line: %d Cannot resolve symbol %s", lexer.line, id), true);
            }
        } else if (token.tag == Tag.LITERAl) {
            factor += match(Tag.LITERAl);
        } else {
//            error("NUM/Word/LITERAL");
            error(Tag.NUM, "NUM/Word/LITERAL");
        }
        return factor;
    }

    private String match(int t) throws IOException {
        if (token.tag == t) {
            Token temp = token;
            nextToken();
            return temp.stringify();
        } else {
            error(t, String.valueOf((char) t));
            return "";
        }
    }

    private void error(int tag, String expected) throws IOException {
        if (!error) {
            error = true;
            int last = buffer.lastIndexOf("\n");
            if (last >= 0) buffer.delete(last, buffer.length());
            appendLineToConsole("Error at line: ".concat(String.valueOf(lexer.line)).concat(" ").concat(expected).concat(" Expected"), true);
            skipError(new Token(tag));
            //nextToken();
        }
    }

    private void skipError(Token expected) throws IOException {
        if (!isEndToken(expected) && !isEndToken(token)) {
            do {
                nextToken();
            } while (!isEndToken(token));
        }
    }

    private void nextToken() throws IOException {
        do {
            token = lexer.scan();
        } while (token.tag == Tag.COMMENT);
        appendLineToConsole(token.toString(), false);
    }

    private boolean isEndToken(Token token) {
        return token.tag == ';' || token.tag == Tag.NULL || token.tag == Tag.END || token.tag == Tag.RETURN;
    }

    private String toJavaLexeme(String lexeme) {

        switch (lexeme) {
            case "Integer":
                return "int ";
            case "String":
                return "String ";
            case "Char":
                return "char ";
            case "Boolean":
                return "boolean ";
            case "<>":
                return "!=";
            case "_":
                return ".";
            default:
                return lexeme;
        }
    }

    private void appendLineToConsole(String string, boolean isError) {
        console.append(string).append("\n");
        if (isError) {
            errors.add(this.line);
        }
        this.line++;
    }
}