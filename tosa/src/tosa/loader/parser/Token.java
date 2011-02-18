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

  public Token next(String... tokens) {
    return _next;
  }

  public boolean match(String value) {
    return _value != null && _value.equalsIgnoreCase(value);
  }

  public boolean match(String... tokens) {
    return next(Arrays.asList(tokens).iterator()) != null;
  }

  private Token next(Iterator<String> stringIterator) {
    if (stringIterator.hasNext()) {
      if (match(stringIterator.next())) {
        return _next.next(stringIterator);
      } else {
        return null;
      }
    } else {
      return this;
    }
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
    return first;
  }

  public boolean matchAny(String... strings) {
    for (String s : strings) {
      if (match(s)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }
  
  public String toString(boolean isRoot) {
    if (isEOF()) {
      if (isRoot) {
        return "[|EOF]";
      } else {
        return "|EOF";
      }
    } else {
      String str = getValue();
      if (isRoot) {
        str = "[" + str + "]";
      }
      return str + " " + _next.toString(false);
    }
  }

  public Token previous() {
    return _previous;
  }
}
