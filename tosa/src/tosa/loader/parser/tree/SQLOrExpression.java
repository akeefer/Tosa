package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;

import java.util.Map;

public class SQLOrExpression extends SQLParsedElement implements IMightApply {
  private SQLParsedElement _lhs;
  private SQLParsedElement _rhs;

  public SQLOrExpression(SQLParsedElement lhs, SQLParsedElement rhs) {
    super(lhs, rhs);
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    boolean lhsApplies = true;
    if (_lhs instanceof IMightApply) {
      lhsApplies = ((IMightApply) _lhs).applies(values);
    }
    boolean rhsApplies = true;
    if (_rhs instanceof IMightApply) {
      rhsApplies = ((IMightApply) _rhs).applies(values);
    }

    if (lhsApplies) {
      _lhs.toSQL(prettyPrint, indent, sb, values);
    }
    if (lhsApplies && rhsApplies) {
      sb.append(" OR ");
    }
    if (rhsApplies) {
      _rhs.toSQL(prettyPrint, indent, sb, values);
    }
    if (!lhsApplies && !rhsApplies) {
      sb.append("(TRUE)");
    }
  }

  @Override
  public IType getVarTypeForChild() {
    return IJavaType.BOOLEAN;
  }

  @Override
  public boolean applies(Map<String, Object> values) {
    if (_lhs instanceof IMightApply && _rhs instanceof IMightApply) {
      return ((IMightApply) _lhs).applies(values) || ((IMightApply) _rhs).applies(values);
    }
    return true;
  }
}
