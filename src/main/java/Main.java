import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    enum TokenType {
        LEFT_PAREN,
        RIGHT_PAREN,
        LEFT_BRACE,
        RIGHT_BRACE,
        COMMA,
        DOT,
        MINUS,
        PLUS,
        SEMICOLON,
        STAR,
        EQUAL,
        EQUAL_EQUAL,
    }

    static boolean hadError = false;
    private static int current = 0;
    private static String source;

    public static void main(String[] args) {
        System.err.println("Logs from your program will appear here!");

        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        if (!command.equals("tokenize")) {
            System.err.println("Unknown command: " + command);
            System.exit(1);
        }

        try {
            int lineNumber = 1;
            for (String line : Files.readAllLines(Path.of(filename))) {
                source = line;
                while (!isAtEnd()) {
                    char c = line.charAt(current);
                    TokenType tokenType = getTokenType(c, lineNumber);
                    printOutput(tokenType, String.valueOf(c), lineNumber);
                    advance();
                }
                lineNumber++;
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("EOF  null");
        if (hadError) {
            System.exit(65);
        }
    }

    private static TokenType getTokenType(char c, int lineNumber) {
        return switch (c) {
            case '(' -> TokenType.LEFT_PAREN;
            case ')' -> TokenType.RIGHT_PAREN;
            case '{' -> TokenType.LEFT_BRACE;
            case '}' -> TokenType.RIGHT_BRACE;
            case ',' -> TokenType.COMMA;
            case '.' -> TokenType.DOT;
            case '-' -> TokenType.MINUS;
            case '+' -> TokenType.PLUS;
            case ';' -> TokenType.SEMICOLON;
            case '*' -> TokenType.STAR;
            case '=' -> getEqualType(c);
            default -> null;
        };
    }

    private static TokenType getEqualType(char c) {
        if (current + 1 < source.length() && source.charAt(current + 1) == '=') {
            advance();
            return TokenType.EQUAL_EQUAL;
        }
        return TokenType.EQUAL;
    }

    private static void advance() {
        current++;
    }

    private static boolean isAtEnd() {
        return current >= source.length();
    }

    private static void printOutput(TokenType tokenType, String lexeme, int lineNumber) {
        if (tokenType != null) {
            lexeme = tokenType == TokenType.EQUAL_EQUAL ? "==" : lexeme;
            System.out.println(tokenType + " " + lexeme + " null");
        } else {
            System.err.println("Error: Unexpected character '" + lexeme + "' at line " + lineNumber);
            hadError = true;
        }
    }
}
