package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class BooleanIsNotExpression extends SQLParsedElement {
  private Token _booleanValue;
  private SQLParsedElement _root;
  private boolean _not;

  public BooleanIsNotExpression(SQLParsedElement sqlParsedElement, Token last, boolean not) {
    super(sqlParsedElement, last);
    _root = sqlParsedElement;
    _booleanValue = last;
    _not = not;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _root.toSQL(prettyPrint, indent, sb, values);
    sb.append(" IS ");
    if(_not) sb.append("NOT ");
    sb.append(_booleanValue.getValue());
  }
}
