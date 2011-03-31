package tosa.loader;

import gw.lang.reflect.IType;
import gw.util.Pair;
import tosa.api.IDBColumnType;
import tosa.loader.parser.tree.VariableExpression;

import java.util.ArrayList;
import java.util.List;

public class SQLParameterInfo {

  private String _name;
  private IDBColumnType _type;
  private List<VariableExpression> _varExpressions;

  public SQLParameterInfo(String name,IDBColumnType type) {
    _name = name;
    _type = type;
    _varExpressions = new ArrayList<VariableExpression>();
  }

  public String getName() {
    return _name;
  }

  public IDBColumnType getType() {
    return _type;
  }

  public void addVariableExpression(VariableExpression var) {
    _varExpressions.add(var);
  }

  // TODO, we really should be setting the type instead
  public boolean isList() {
    for (VariableExpression varExpression : _varExpressions) {
      if (varExpression.isList()) {
        return true;
      }
    }
    return false;
  }
}
