package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import tosa.loader.parser.Token;

import java.util.Map;

public class SQLSignedExpression extends SQLParsedElement{
  private Token _op;
  private SQLParsedElement _rhs;

  public SQLSignedExpression(Token op, SQLParsedElement rhs) {
    super(op, rhs);
    _op = op;
    _rhs = rhs;
  }

  @Override
  public IType getVarTypeForChild() {
    return getParent().getVarTypeForChild();
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_op.getValue());
    _rhs.toSQL(prettyPrint, indent, sb, values);
  }
}
