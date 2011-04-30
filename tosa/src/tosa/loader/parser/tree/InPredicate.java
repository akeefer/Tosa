package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;
import tosa.api.IDBColumnType;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;

import java.util.Map;

public class InPredicate extends SQLParsedElement{

  private SQLParsedElement _lhs;
  private SQLParsedElement _in;
  private boolean _not;

  public InPredicate(SQLParsedElement lhs, SQLParsedElement in, boolean not) {
    super(lhs, in);
    _lhs = lhs;
    _not = not;
    _in = in;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    setType(DBColumnTypeImpl.BOOLEAN);
  }

  @Override
  public IType getVarTypeForChild() {
    IDBColumnType type = _lhs.getDBType();
    if (type == null) {
      return IJavaType.LIST;
    } else {
      return IJavaType.LIST.getGenericType().getParameterizedType(type.getGosuType());
    }
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    if(_not) {
      sb.append(" NOT");
    }
    sb.append(" IN ");
    _in.toSQL(prettyPrint,indent, sb, values);
  }

  public SQLParsedElement getLHS() {
    return _lhs;
  }
}
