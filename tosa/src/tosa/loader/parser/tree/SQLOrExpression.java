package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;

import java.util.Map;

public class SQLOrExpression extends SQLParsedElement {
  private SQLParsedElement _lhs;
  private SQLParsedElement _rhs;

  public SQLOrExpression(SQLParsedElement lhs, SQLParsedElement rhs) {
    super(lhs, rhs);
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    sb.append(" OR ");
    _rhs.toSQL(prettyPrint, indent, sb, values);
  }

  @Override
  public IType getVarTypeForChild() {
    return IJavaType.BOOLEAN;
  }
}
