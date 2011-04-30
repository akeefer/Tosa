package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.Map;

public class ComparisonPredicate extends SQLParsedElement{
  private SQLParsedElement _lhs;
  private Token _op;
  private SQLParsedElement _rhs;

  public ComparisonPredicate(SQLParsedElement lhs, Token op, SQLParsedElement rhs) {
    super(lhs, rhs);
    _lhs = lhs;
    _op = op;
    _rhs = rhs;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    setType(DBColumnTypeImpl.BOOLEAN);
  }

  @Override
  public IType getVarTypeForChild() {
    if (_lhs.getDBType() != null) {
      return _lhs.getDBType().getGosuType();
    } else if (_rhs.getDBType() != null) {
      return _rhs.getDBType().getGosuType();
    } else {
      return null;
    }
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    sb.append(" ");
    sb.append(_op.getValue());
    sb.append(" ");
    _rhs.toSQL(prettyPrint, indent, sb, values);
  }

}
