package tosa.loader.parser.tree;

import gw.lang.reflect.IProvidesCustomErrorInfo;
import tosa.loader.parser.Token;

public class SQLParseError extends IProvidesCustomErrorInfo.CustomErrorInfo {

  Token _start;

  public SQLParseError(Token token, String message, boolean warning) {
    this(token, token, message, warning);
  }

  public SQLParseError(Token token, String message) {
    this(token, token, message);
  }

  public SQLParseError(Token start, Token end, String message) {
    this(start, end, message,  false);    
  }
  
  public SQLParseError(Token start, Token end, String message, boolean warning) {
    super(warning ? IProvidesCustomErrorInfo.ErrorLevel.WARNING : IProvidesCustomErrorInfo.ErrorLevel.ERROR, message, start.getStart(), end.getEnd());
    _start = start;
  }

  public Token getStartToken() {
    return _start;
  }
}
