package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class StringLiteralExpression extends SQLParsedElement{
  private Token _value;

  public StringLiteralExpression(Token str) {
    super(str);
    _value = str;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(_value);
  }
}
