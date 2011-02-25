package tosa.loader.parser;

import java.util.Arrays;
import java.util.Iterator;

public class Token {

  private static final Token EOF = new Token(null, 0, 0, 0, 0);
  static {
    EOF._next = EOF;
  }

  private String _value;
  private Token _next;
  private Token _previous;
  private int _line;
  private int _col;
  private int _start;
  private int _end;


  public Token(String value, int line, int col, int start, int end) {
    _value = value;
    _line = line;
    _col = col;
    _start = start;
    _end = end;
  }

  public void setNext(Token t) {
    _next = t;
    if (!isEOF()||!t.isEOF()) {
      t._previous = this;
    }
  }

  public String getValue() {
    return _value;
  }

  public boolean isEOF() {
    return this == EOF;
  }

  public Token nextToken() {
    return _next;
  }

  public boolean match(String value) {
    return _value != null && _value.equalsIgnoreCase(value);
  }

  public static Token tokenize(String contents) {
    Token first = null;
    Token previous = null;
    Tokenizer tokenizer = new Tokenizer(contents);
    while (tokenizer.hasMoreTokens()) {
      Token t = tokenizer.nextToken();
      if (previous != null) {
        previous.setNext(t);
      }
      if (first == null) {
        first = t;
      }
      previous = t;
    }
    if (previous != null) {
      previous.setNext(Token.EOF);
    }
    if (first == null) {
      first = Token.EOF;
    }
    return first;
  }

  @Override
  public String toString() {
    return getValue();
  }

  public String toStringForDebug() {
    return first().toStringForDebug(this);
  }

  private Token first() {
    if (_previous == null) {
      return this;
    } else {
      return _previous.first();
    }
  }

  private String toStringForDebug(Token current) {
    if (isEOF()) {
      if (this == current) {
        return "[|EOF]";
      } else {
        return "|EOF";
      }
    } else {
      String str = getValue();
      if (this == current) {
        str = "[" + str + "]";
      }
      return str + " " + _next.toStringForDebug(current);
    }
  }

  public Token previous() {
    return _previous;
  }

  public int getLine() {
    return _line;
  }

  public int getColumn() {
    return _col;
  }

  public int getStart() {
    return _start;
  }

  public int getEnd() {
    return _end;
  }

  public boolean isSymbol() {
    for (int i = 0; i < _value.length(); i++) {
      if (!Character.isLetter(_value.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean isNumber() {
    for (int i = 0; i < _value.length(); i++) {
      if (!Character.isDigit(_value.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean isString() {
    return _value.startsWith("\"") || _value.startsWith("\'");
  }
}
