package tosa.loader.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SQLTokenizer {

  private int _currentToken;
  private List<String> _tokens;

  public SQLTokenizer(String contents) {
    _currentToken = 0;
    _tokens = fillTokenBuffer(contents);
  }

  private List<String> fillTokenBuffer(String contents) {
    StringTokenizer tokenizer = new StringTokenizer(contents, " \n\t\r,;()", true);
    List<String> tokens = new ArrayList<String>();
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      // Ignore white space
      if (!token.trim().isEmpty()) {
        tokens.add(token);
      }
    }
    return tokens;
  }

  public String token() {
    // TODO - AHK - Throw if current token is too large?
    return _tokens.get(_currentToken);
  }

  public String consumeToken() {
    String result = _tokens.get(_currentToken);
    _currentToken++;
    return result;
  }

  public boolean acceptIgnoreCase(String... tokens) {
    if (matchIgnoreCase(tokens)) {
      _currentToken += tokens.length;
      return true;
    } else {
      return false;
    }
  }

  public boolean peekIgnoreCase(String... tokens) {
    return matchIgnoreCase(tokens);
  }

  private boolean matchIgnoreCase(String... tokens) {
    if (tokens == null) {
      throw new IllegalArgumentException();
    }
    for (String token : tokens) {
      if (token == null) {
        throw new IllegalArgumentException();
      }
    }

    if (_currentToken < _tokens.size() - tokens.length) {
      for (int i = 0; i < tokens.length; i++) {
        if (!tokens[i].equalsIgnoreCase(_tokens.get(_currentToken + i))) {
          return false;
        }
      }

      return true;
    } else {
      return false;
    }
  }

  public void expectIgnoreCase(String expected) {
    // TODO - AHK - Verify expected is non-null
    if (!expected.equalsIgnoreCase(token())) {
      throw new IllegalStateException();
    } else {
      _currentToken++;
    }
  }
}
