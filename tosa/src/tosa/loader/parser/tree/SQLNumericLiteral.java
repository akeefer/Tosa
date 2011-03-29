package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.math.BigDecimal;

public class SQLNumericLiteral extends SQLParsedElement {
  private Number _value;

  public SQLNumericLiteral(Token start, Number value) {
    super(start);
    _value = value;
  }

  public SQLNumericLiteral(Token start, Token end, BigDecimal value) {
    super(start, end);
    _value = value;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(_value);
  }
}
