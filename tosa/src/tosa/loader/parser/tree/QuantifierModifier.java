package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class QuantifierModifier extends SQLParsedElement{
  private Token _quantifier;

  public QuantifierModifier(Token token) {
    super(token);
    _quantifier = token;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(_quantifier.toString());
  }
}
