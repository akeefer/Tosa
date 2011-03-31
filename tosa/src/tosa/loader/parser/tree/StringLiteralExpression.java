package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class StringLiteralExpression extends SQLParsedElement{
  private Token _value;

  public StringLiteralExpression(Token str) {
    super(str);
    _value = str;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_value);
  }
}
