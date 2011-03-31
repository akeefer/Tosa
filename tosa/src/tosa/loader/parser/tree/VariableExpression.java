package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class VariableExpression extends SQLParsedElement {
  private Token _name;
  private boolean _isList;
  private Object _;

  public VariableExpression(Token name) {
    super(name);
    _name = name;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    if (isList()) {
      if (values == null) {
        sb.append("()");
      } else {
        Object val = values.get(getName());
        if (val instanceof List) {
          sb.append("(");
          int size = ((List) val).size();
          for (int i = 0; i < size; i++) {
            if (i != 0) {
              sb.append(", ");
            }
            sb.append("?");
          }
          sb.append(")");
        } else {
          sb.append("()");
        }
      }
    } else {
      sb.append("?");
    }
  }

  public String getName() {
    return _name.getValue();
  }

  public void setList(boolean b) {
    _isList = true;
  }

  public boolean isList() {
    return _isList;
  }
}
