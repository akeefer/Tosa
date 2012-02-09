package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class QuantifierModifier extends SQLParsedElement{
  private Token _quantifier;

  public QuantifierModifier(Token token) {
    super(token);
    _quantifier = token;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_quantifier.toString()).append(" ");
  }
}
