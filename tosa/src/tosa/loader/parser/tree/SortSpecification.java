package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class SortSpecification extends SQLParsedElement {

  private SQLParsedElement _value;
  private boolean _ascending;
  private boolean _descending;

  public SortSpecification(SQLParsedElement valueExpr, Token token, boolean ascending, boolean descending) {
    super(valueExpr.firstToken(), token, valueExpr);
    _value = valueExpr;
    _ascending = ascending;
    _descending = descending;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _value.toSQL(prettyPrint, indent, sb, values);
    if (_ascending) {
      sb.append(" ASC ");
    } else if (_descending) {
      sb.append(" DESC ");
    }
  }
}
