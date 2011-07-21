package tosa.loader.parser;

import tosa.loader.parser.tree.SQLParseError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Token {

  private static final Token EOF = new Token(TokenType.EOF, null, 0, 0, 0, 0);
  static {
    EOF._next = EOF;
  }

  private TokenType _type;
  private String _value;
  private Token _next;
  private Token _previous;
  private int _line;
  private int _col;
  private int _start;
  private int _end;
  private List<SQLParseError> _temporaryErrors;

  public Token(TokenType type, String value, int line, int col, int start, int end) {
    _type = type;
    _value = value;
    _line = line;
    _col = col;
    _start = start;
    _end = end;
    _temporaryErrors = Collections.EMPTY_LIST;
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
    Tokenizer tokenizer = new Tokenizer(contents, SQLParserConstants.OPS.get());
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

  public Token removeTokens( TokenType... typesToRemove )
  {
    if( this == EOF )
    {
      return this;
    }
    for( TokenType tokenType : typesToRemove )
    {
      if( this._type == tokenType )
      {
        return nextToken().removeTokens( typesToRemove );
      }
    }
    Token copy = new Token( _type, _value, _line, _col, _start, _end );
    copy.setNext( nextToken().removeTokens( typesToRemove ) );
    return copy;
  }


  @Override
  public String toString() {
    return getValue();
  }

  public String toStringForDebug() {
    return first().toStringForDebug( this );
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
    return _type == TokenType.SYMBOL;
  }

  public boolean isNumber() {
    return _type == TokenType.NUMBER;
  }

  public boolean isString() {
    return _type == TokenType.STRING;
  }

  public boolean isComment()
  {
    return _type == TokenType.COMMENT;
  }

  public void addTemporaryError(SQLParseError error) {
    if (_temporaryErrors == Collections.EMPTY_LIST) {
      _temporaryErrors = new ArrayList<SQLParseError>();
    }
    _temporaryErrors.add(error);
  }

  public List<SQLParseError> collectTemporaryErrors(Token end) {
   List<SQLParseError> parseErrors = Collections.EMPTY_LIST;
   Token t = this;
    do {
      if (t._temporaryErrors != Collections.EMPTY_LIST) {
        if (parseErrors == Collections.EMPTY_LIST) {
          parseErrors = new ArrayList<SQLParseError>();
        }
        parseErrors.addAll(t._temporaryErrors);
        t._temporaryErrors = Collections.EMPTY_LIST;
      }
      t = t.nextToken();
    } while (t.previous() != end && t != EOF);
    return parseErrors;
  }

  public boolean endOf(String... tokenString) {
    Token current = this;
    for (int i = tokenString.length - 1; i >= 0; i--) {
      if (!current.match(tokenString[i])) {
        return false;
      }
      current = current.previous();
    }
    return true;
  }
}
