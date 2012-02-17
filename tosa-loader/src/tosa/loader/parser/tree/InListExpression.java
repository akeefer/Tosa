package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import tosa.api.IDBColumnType;
import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class InListExpression extends SQLParsedElement {
  private List<SQLParsedElement> _values;

  public InListExpression(Token first, List<SQLParsedElement> values) {
    super(first, values);
    _values = values;
  }

  @Override
  public IDBColumnType getVarTypeForChild() {
    InPredicate parent = (InPredicate) getParent();
    IDBColumnType type = parent.getLHS().getDBType();
    if (type != null) {
      return type;
    } else {
      return null;
    }
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("( ");
    for (int i = 0; i < _values.size(); i++) {
      SQLParsedElement sqlParsedElement = _values.get(i);
      if (i != 0) {
        sb.append(", ");
      }
      sqlParsedElement.toSQL(prettyPrint, indent, sb, values);
    }
    sb.append(" )");
  }
}
