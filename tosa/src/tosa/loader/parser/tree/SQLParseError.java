package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class SQLParseError {

  private Token _start;
  private Token _end;
  private String _message;

  public SQLParseError(Token token, String message) {
    this(token, token, message);
  }

  public SQLParseError(Token start, Token end, String message) {
    _start = start;
    _end = end;
    _message = message;
  }

  public Token getStart() {
    return _start;
  }

  public Token getEnd() {
    return _end;
  }

  public String getMessage() {
    return _message;
  }
}
