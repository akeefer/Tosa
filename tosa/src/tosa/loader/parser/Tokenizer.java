package tosa.loader.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Tokenizer {

  private static final Set<String> OPERATORS = new HashSet<String>(Arrays.asList(".", "+", "(", ")"));

  private String _currentStringValue;
  private String _contents;
  private int _line;
  private int _col;
  private int _offset;
  private int _currentStartOffset;
  private int _currentEndOffset;
  

  public Tokenizer(String contents) {
    _contents = contents;
    _line = 1;
    _col = 0;
    _offset = 0;
    _currentStartOffset = 0;
    _currentEndOffset = 0;
    _currentStringValue = null;
  }

  public boolean hasMoreTokens() {
    return moveToNextToken();
  }

  private boolean moveToNextToken() {
    eatWhitespace();

    if (stringIsConsumed()) {
      return false;
    }

    _currentStartOffset = _offset;

    if (!consumeOperator()) {
      if (!consumeSymbol()) {
        if (!consumeString()) {
          if (!consumeNumber()) {
            consumeChar();
          }
        }
      }
    }
    _currentEndOffset = _offset;
    _currentStringValue = _contents.substring(_currentStartOffset, _currentEndOffset);

    return true;
  }

  private void consumeChar() {
    _offset++;
  }

  private boolean consumeNumber() {
    if (Character.isDigit(currentChar())) {
      while (!stringIsConsumed() && Character.isDigit(currentChar())) {
        _offset++;
      }
      if (!stringIsConsumed() && currentChar() == '.') {
        if (canPeek(2) && Character.isDigit(_contents.charAt(_offset+1))) {
          _offset++;
          while (!stringIsConsumed() && Character.isDigit(currentChar())) {
            _offset++;
          }
        }
      }
      return true;
    }
    return false;
  }

  private boolean consumeString() {
    if ('\'' == currentChar() || '"' == currentChar()) {
      char initial = currentChar();
      char previous = initial;
      _offset++;
      while (!stringIsConsumed() && currentChar() != '\n') {
        char current = currentChar();
        if (current == initial && previous != '\\') {
          break;
        } else {
          previous = current;
          _offset++;
        }
      }
      return true;
    }
    return false;
  }

  private boolean consumeSymbol() {
    if (Character.isLetter(currentChar()) || ':' == currentChar()) {
      _offset++;
      while (!stringIsConsumed() && Character.isLetter(currentChar())) {
        _offset++;
      }
      return true;
    }
    return false;
  }

  private boolean consumeOperator() {
    if (OPERATORS.contains(Character.toString(currentChar()))) {
      return true;
    } else if (canPeek(2) && OPERATORS.contains(peek(2))) {
      return true;
    }
    return false;
  }

  private String peek(int i) {
    return _contents.substring(_offset, _offset + i);
  }

  private boolean canPeek(int count) {
    return _offset + count - 1 >= _contents.length();
  }


  private boolean stringIsConsumed() {
    return _offset >= _contents.length();
  }

  private void eatWhitespace() {
    while (!stringIsConsumed() && Character.isWhitespace(currentChar())) {
      _offset++;
      if ('\n' == currentChar()) {
        _line++;
        _col = 0;
      }
    }
  }

  private char currentChar() {
    return _contents.charAt(_offset);
  }

  public Token nextToken() {
    return new Token(_currentStringValue, _line, _col, _currentStartOffset, _currentEndOffset);
  }
}
