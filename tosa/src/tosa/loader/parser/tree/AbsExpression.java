package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class AbsExpression extends SQLParsedElement {
  private SQLParsedElement _value;

  public AbsExpression(Token start, SQLParsedElement value, Token token) {
    super(start, value, token);
    _value = value;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("ABS(");
    _value.toSQL(prettyPrint, indent, sb, values);
    sb.append(")");
  }
}
