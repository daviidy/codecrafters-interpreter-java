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
      STAR
  }
  static boolean hadError = false;
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

    String fileContents = "";
    StringBuilder result = new StringBuilder();
    try {
        int lineNumber = 1;
        for (String line: Files.readAllLines(Path.of(filename))) {
          for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            TokenType tokenType = getTokenType(c, lineNumber);
            if (tokenType != null) {
                result.append(getTokenType(c, lineNumber)).append(" ").append(c).append(" ").append("null").append("\n");
            } else {
                hadError = true;
                result.append(String.format("[line %d] Error: Unexpected character: %c", lineNumber, c)).append("\n");
            }
          }
          lineNumber++;
        }

    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
      System.exit(1);
    }
    if(hadError) {
        System.err.print(result);
    } else {
        System.out.print(result);
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
          default -> null;
      };
  }
}
