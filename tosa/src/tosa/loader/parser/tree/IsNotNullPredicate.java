package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class IsNotNullPredicate extends SQLParsedElement{
  private SQLParsedElement _lhs;
  private boolean _not;

  public IsNotNullPredicate(SQLParsedElement lhs, Token end, boolean not) {
    super(lhs, end);
    _lhs = lhs;
    _not = not;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    sb.append(" IS");
    if (_not) {
      sb.append(" NOT");
    }
    sb.append(" NULL ");
  }
}
