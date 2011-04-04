package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;
import tosa.loader.parser.Token;

import java.util.Map;

public class DerivedColumn extends SQLParsedElement {

  private SQLParsedElement _value;
  private Token _colName;

  public DerivedColumn(SQLParsedElement value, Token colName) {
    super(value, colName);
    _value = value;
    _colName = colName;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _value.toSQL(prettyPrint, indent, sb, values);
    sb.append(" AS ");
    sb.append(_colName.getValue());
  }

  public String getName() {
    return _colName.getValue();
  }

  public IType getGosuType() {
    return IJavaType.OBJECT;
  }
}
