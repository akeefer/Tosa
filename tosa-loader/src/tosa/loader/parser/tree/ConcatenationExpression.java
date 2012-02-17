package tosa.loader.parser.tree;

import tosa.api.IDBColumnType;
import tosa.loader.data.DBColumnTypeImpl;

import java.util.Map;

public class ConcatenationExpression extends SQLParsedElement {

  private SQLParsedElement _lhs;
  private SQLParsedElement _rhs;

  public ConcatenationExpression(SQLParsedElement lhs, SQLParsedElement rhs) {
    super(lhs, rhs);
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  public IDBColumnType getVarTypeForChild() {
    return DBColumnTypeImpl.STRING;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    sb.append(" || ");
    _rhs.toSQL(prettyPrint, indent, sb, values);
  }
}
