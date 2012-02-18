package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import tosa.api.IDBColumnType;
import tosa.loader.IDBType;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.Map;

public class SQLMultiplicitiveExpression extends SQLParsedElement{
  private SQLParsedElement _lhs;
  private Token _op;
  private SQLParsedElement _rhs;

  public SQLMultiplicitiveExpression(SQLParsedElement lhs, Token op, SQLParsedElement rhs) {
    super(lhs, rhs);
    _lhs = lhs;
    _op = op;
    _rhs = rhs;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    if (_rhs.getDBType() == null && _lhs.getDBType() == null) {
      setType(DBColumnTypeImpl.INT);
    } else if (_lhs.getDBType() != null) {
      setType(_lhs.getDBType());
    } else {
      setType(_rhs.getDBType());
    }
  }

  @Override
  public IDBColumnType getVarTypeForChild() {
    return getDBType();
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
