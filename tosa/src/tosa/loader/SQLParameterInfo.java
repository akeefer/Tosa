package tosa.loader;

import gw.lang.reflect.IType;

import java.util.ArrayList;
import java.util.List;

public class SQLParameterInfo {

  private String _name;
  private IType _type;
  private List<Integer> _indexes;

  public SQLParameterInfo(String name, IType type) {
    _name = name;
    _type = type;
    _indexes = new ArrayList<Integer>();
  }

  public String getName() {
    return _name;
  }

  public IType getType() {
    return _type;
  }

  public List<Integer> getIndexes() {
    return _indexes;
  }
}
