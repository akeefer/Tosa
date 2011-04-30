package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class QuantifiedComparison extends SQLParsedElement {

  private Token _op;
  private Token _quantifier;
  private SQLParsedElement _lhs;
  private SQLParsedElement _subQuery;

  public QuantifiedComparison(Token op, Token quantifier, SQLParsedElement lhs, SQLParsedElement subQuery) {
    super(lhs, subQuery);
    _lhs = lhs;
    _subQuery = subQuery;
    _op = op;
    _quantifier = quantifier;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    sb.append(" ");
    sb.append(_op.getValue());
    sb.append(" ");
    sb.append(_quantifier.getValue());
    sb.append(" ");
    _subQuery.toSQL(prettyPrint, indent, sb, values);
  }
}
