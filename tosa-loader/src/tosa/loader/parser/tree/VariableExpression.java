package tosa.loader.parser.tree;

import gw.lang.reflect.java.JavaTypes;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableExpression extends SQLParsedElement {

  private Token _name;
  private boolean _list;

  public VariableExpression(Token name) {
    super(name);
    _name = name;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    if (_list) {
      if (values == null) {
        sb.append("()");
      } else {
        Object val = values.get(getName());
        if (val == null) {
          sb.append("()");
        } else {
          sb.append("(");
          int size = ((List) val).size();
          for (int i = 0; i < size; i++) {
            if (i != 0) {
              sb.append(", ");
            }
            sb.append("?");
          }
          sb.append(")");
        }
      }
    } else {
      sb.append("?");
    }
  }

  @Override
  public void resolveVars(DBData dbData) {
    setType(getParent().getVarTypeForChild());
  }

  public String getName() {
    return _name.getValue();
  }

  public boolean isList() {
    return getDBType().isList();
  }

  public boolean shouldApply(HashMap<String, Object> args) {
    SQLOptionalExpression option = getAncestor(SQLOptionalExpression.class);
    return option == null || option.applies(args);
  }
}
