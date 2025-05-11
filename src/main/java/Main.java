import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
        BANG,
        BANG_EQUAL,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        SLASH,
        TAB,
        SPACE,
        STRING,
        NUMBER,
        ELSE,
        FALSE,
        FOR,
        FUN,
        IF,
        NIL,
        OR,
        PRINT,
        RETURN,
        SUPER,
        THIS,
        TRUE,
        VAR,
        WHILE,
        IDENTIFIER,
    }

    private static boolean hadError = false;
    private static int current = 0;
    private static int start = 0;
    private static String source;
    private static boolean isComment = false;
    private static int lineNumber = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.EQUAL);
        keywords.put("class", TokenType.EQUAL);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    private static StringBuilder numberBuilder = new StringBuilder();

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
            for (String line : Files.readAllLines(Path.of(filename))) {
                source = line;
                while (!isOutOfBounds(current)) {
                    char c = line.charAt(current);
                    TokenType tokenType = getTokenType(c, lineNumber);
                    if (tokenType == TokenType.STRING) {
                        String stringLiteral = getStringLiteral();
                        printOutput(tokenType, stringLiteral, lineNumber, stringLiteral);
                        continue;
                    } else if (tokenType == TokenType.NUMBER) {
                        getNumber();
                        double value = Double.parseDouble(String.valueOf(numberBuilder));
                        String lexeme = numberBuilder.toString();
                        printOutput(tokenType, lexeme, lineNumber, String.valueOf(value));
                        numberBuilder.setLength(0);
                        continue;
                    } else if (tokenType == TokenType.IDENTIFIER) {
                        String lexeme = source.substring(start, current);
                        printOutput(tokenType, lexeme, lineNumber, null);
                        continue;
                    } else if (!isComment && tokenType != TokenType.SPACE && tokenType != TokenType.TAB) {
                        printOutput(tokenType, String.valueOf(c), lineNumber, null);
                    }
                    if(isComment) {
                        isComment = false;
                    }
                    advance();
                }
                lineNumber++;
                current = 0;
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
            case '!' -> getBangType(c);
            case '<', '>' -> getOperatorType(c);
            case '/' -> handleCommentOrGetSlash(c);
            case '\t' -> TokenType.TAB;
            case ' ' -> TokenType.SPACE;
            case '"' -> TokenType.STRING;
            default -> {
                if (isAlpha(c)) {
                    start = current;
                    while (!isOutOfBounds(current) && isAlphaNumeric(source.charAt(current))) {
                        advance();
                    }
                    String lexeme = source.substring(start, current);
                    TokenType type = keywords.get(lexeme);
                    if (type != null) {
                        yield type;
                    } else {
                        yield TokenType.IDENTIFIER;
                    }
                } else if (isDigit(c)) {
                    getNumber();
                    yield TokenType.NUMBER;
                }
                yield null;
            }
        };
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static void getNumber() {
        while (!isOutOfBounds(current) && isDigit(source.charAt(current))) {
            numberBuilder.append(source.charAt(current));
            advance();
        }
        if(!isOutOfBounds(current) && !isOutOfBounds(current + 1) && source.charAt(current) == '.' && isDigit(source.charAt(current + 1)) ) {
            numberBuilder.append('.');
            advance();
            while (!isOutOfBounds(current) && isDigit(source.charAt(current))) {
                numberBuilder.append(source.charAt(current));
                advance();
            }
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String getStringLiteral() {
        advance();
        start = current;
        while (!isOutOfBounds(current) && source.charAt(current) != '"') {
            if (source.charAt(current) == '\n') {
                lineNumber++;
            }
            advance();
        }
        String stringBuilder = source.substring(start, current);
        if (!isOutOfBounds(current) && source.charAt(current) == '"') {
            advance();
        } else {
            return null;
        }
        return stringBuilder;
    }

    private static TokenType handleCommentOrGetSlash(char c) {
        if (!isOutOfBounds(current + 1) && source.charAt(current + 1) == '/') {
            while (!isOutOfBounds(current) && source.charAt(current) != '\n') {
                advance();
            }
            isComment = true;
        } else {
            return TokenType.SLASH;
        }
        return null;
    }

    private static TokenType getOperatorType(char c) {
        if (!isOutOfBounds(current + 1) && source.charAt(current + 1) == '=') {
            advance();
            return c == '>' ? TokenType.GREATER_EQUAL : TokenType.LESS_EQUAL;
        }
        return c == '>' ? TokenType.GREATER : TokenType.LESS;
    }

    private static TokenType getEqualType(char c) {
        if (!isOutOfBounds(current + 1) && source.charAt(current + 1) == '=') {
            advance();
            return TokenType.EQUAL_EQUAL;
        }
        return TokenType.EQUAL;
    }

    private static TokenType getBangType(char c) {
        if (!isOutOfBounds(current + 1) && source.charAt(current + 1) == '=') {
            advance();
            return TokenType.BANG_EQUAL;
        }
        return TokenType.BANG;
    }

    private static void advance() {
        current++;
    }

    private static boolean isOutOfBounds(int index) {
        return index >= source.length();
    }

    private static void printOutput(TokenType tokenType, String lexeme, int lineNumber, String literal) {
        if(tokenType == TokenType.STRING && lexeme == null) {
            System.err.printf("[line %d] Error: Unterminated string.%n", lineNumber);
            hadError = true;
        } else if (tokenType == TokenType.NUMBER && lexeme == null) {
            System.err.printf("[line %d] Error: Invalid number.%n", lineNumber);
            hadError = true;
        } else if (tokenType != null) {
            lexeme = tokenType == TokenType.EQUAL_EQUAL ? "==" : lexeme;
            lexeme = tokenType == TokenType.BANG_EQUAL ? "!=" : lexeme;
            lexeme = tokenType == TokenType.GREATER_EQUAL ? ">=" : lexeme;
            lexeme = tokenType == TokenType.LESS_EQUAL ? "<=" : lexeme;
            lexeme = tokenType == TokenType.STRING ? "\"" + lexeme + "\"" : lexeme;
            System.out.println(tokenType + " " + lexeme + " " + literal);
        } else {
            error(lineNumber, lexeme);
        }
    }

    private static void error(int lineNumber, String lexeme) {
        System.err.printf("[line %d] Error: Unexpected character: %s%n", lineNumber, lexeme);
        hadError = true;
    }
}
