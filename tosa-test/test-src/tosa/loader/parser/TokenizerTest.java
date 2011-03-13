package tosa.loader.parser;

import static org.junit.Assert.*;
import org.junit.Test;

public class TokenizerTest {
  @Test
  public void bootstrapTokenizerTest() {
    assertMatches(Token.tokenize(""));
    assertMatches(Token.tokenize("."), ".");
    assertMatches(Token.tokenize("a"), "a");
    assertMatches(Token.tokenize("ab"), "ab");
    assertMatches(Token.tokenize("abc"), "abc");
    assertMatches(Token.tokenize("a b c"), "a", "b", "c");
    assertMatches(Token.tokenize("a     b      c"), "a", "b", "c");
    assertMatches(Token.tokenize("a     b      \nc"), "a", "b", "c");
    assertMatches(Token.tokenize("a   \t  b      \nc"), "a", "b", "c");
    assertMatches(Token.tokenize(" \"asdf\" "), "\"asdf\"");
    assertMatches(Token.tokenize("latin1"), "latin1");
  }

  @Test
  public void lineNumbersCorrect() {
    Token token = Token.tokenize("asdf");
    assertEquals(1, token.getLine());

    token = Token.tokenize("\nasdf");
    assertEquals(2, token.getLine());

    token = Token.tokenize("asdf\nasdf");
    assertEquals(1, token.getLine());
    assertEquals(2, token.nextToken().getLine());
  }

  @Test
  public void columnsNumbersCorrect() {
    Token token = Token.tokenize("asdf");
    assertEquals(1, token.getColumn());

    token = Token.tokenize("\nasdf");
    assertEquals(1, token.getColumn());

    token = Token.tokenize("asdf\nasdf");
    assertEquals(1, token.getColumn());
    assertEquals(1, token.nextToken().getColumn());

    token = Token.tokenize("  asdf\n    asdf");
    assertEquals(3, token.getColumn());
    assertEquals(5, token.nextToken().getColumn());
  }

  private void assertMatches(Token token, String... strs) {
    for (String str : strs) {
      assertTrue("Expected " + str + " but found " + token.getValue() + " in " + token.toStringForDebug(),
        token.match(str));
      token = token.nextToken();
    }
    assertTrue("Expected EOF at " + token.toStringForDebug(), token.isEOF());
  }
}
