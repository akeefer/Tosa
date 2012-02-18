package tosa.loader.parser;

import tosa.loader.parser.tree.SQLParseError;

public class SQLParserBase implements SQLParserConstants {

  private Token _currentToken;


  public SQLParserBase(Token token) {
    _currentToken = token;
  }

  public Token getCurrentToken() {
    return _currentToken;
  }

  public boolean matchAny(String... tokens) {
    for (String s : tokens) {
      if (match(s)) {
        return true;
      }
    }
    return false;
  }

  public boolean match(String str) {
    if (getCurrentToken().match(str)) {
      _currentToken = _currentToken.nextToken();
      return true;
    } else {
      return false;
    }
  }

  public boolean match(String str1, String str2) {
    if (_currentToken.match(str1) && _currentToken.nextToken().match(str2)) {
      _currentToken = _currentToken.nextToken().nextToken();
      return true;
    } else {
      return false;
    }
  }

  public boolean peek(String str) {
    return _currentToken.match(str);
  }

  public boolean peek(String str1, String str2) {
    return _currentToken.match(str1) && _currentToken.nextToken().match(str2);
  }

  public boolean match(String str1, String str2, String str3) {
    if (_currentToken.match(str1) &&
      _currentToken.nextToken().match(str2) &&
      _currentToken.nextToken().nextToken().match(str3)) {
      _currentToken = _currentToken.nextToken().nextToken().nextToken();
      return true;
    } else {
      return false;
    }
  }

  public Token lastMatch() {
    return _currentToken.previous();
  }

  public boolean expect(String str) {
    if (match(str)) {
      return true;
    } else {
      _currentToken.addTemporaryError(new SQLParseError(_currentToken, _currentToken, "Expected " + str));
      _currentToken = _currentToken.nextToken();
      return false;
    }
  }

  public Token takeToken() {
    Token base = _currentToken;
    _currentToken = _currentToken.nextToken();
    return base;
  }

  public boolean isEOF() {
    return _currentToken == null || _currentToken.isEOF();
  }

}
