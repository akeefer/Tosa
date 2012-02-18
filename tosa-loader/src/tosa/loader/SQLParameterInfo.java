package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import gw.util.Pair;
import tosa.api.IDBColumnType;
import tosa.loader.parser.tree.VariableExpression;

import java.util.ArrayList;
import java.util.List;

public class SQLParameterInfo {

  private String _name;
  private IType _type;
  private List<VariableExpression> _varExpressions;

  public SQLParameterInfo(String name) {
    _name = name;
    _varExpressions = new ArrayList<VariableExpression>();
  }

  public String getName() {
    return _name;
  }

  public void addVariableExpression(VariableExpression var) {
    _varExpressions.add(var);
    if (_type == null && var.getDBType().getGosuType() != null) {
      _type = var.getDBType().getGosuType();
    } else if (_type != null && var.getDBType().getGosuType() != null) {
      if (!_type.equals(var.getDBType().getGosuType())) {
        _type = JavaTypes.OBJECT();
      }
    }
  }

  public IType getGosuType() {
    return _type == null ? JavaTypes.STRING() : _type;
  }
}
