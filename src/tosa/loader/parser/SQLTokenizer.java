package tosa.loader.parser;

import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SQLTokenizer {

  private StringTokenizer _tokenizer;
  private String _lastToken;

  public SQLTokenizer(String contents) {
    _tokenizer = new StringTokenizer(contents, " \n\t\r,;()", true);
  }

  public String token() {
    return _lastToken;
  }

  public String consumeToken() {
    String result = _lastToken;
    nextToken();
    return result;
  }

  public void nextToken() {
    if (!_tokenizer.hasMoreTokens()) {
      // TODO - AHK - Error handling
      throw new IllegalStateException();
    }

    _lastToken = _tokenizer.nextToken();

    // Skip white space
    if (_lastToken.trim().isEmpty()) {
      nextToken();
    }
  }

  public boolean acceptIgnoreCase(String potentialMatch) {
    // TODO - AHK - verify potentialMatch is non-null
    if (potentialMatch.equalsIgnoreCase(_lastToken)) {
      nextToken();
      return true;
    } else {
      return false;
    }
  }

  public void expectIgnoreCase(String expected) {
    // TODO - AHK - Verify expected is non-null
    if (!expected.equalsIgnoreCase(_lastToken)) {
      throw new IllegalStateException();
    } else {
      nextToken();
    }
  }
}
