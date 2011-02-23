package tosa.loader;

import gw.lang.reflect.IType;
import gw.util.Pair;
import tosa.api.IDBColumnType;

import java.util.ArrayList;
import java.util.List;

public class SQLParameterInfo {

  private String _name;
  private List<Pair<Integer, IDBColumnType>> _indexes;

  public SQLParameterInfo(String name) {
    _name = name;
    _indexes = new ArrayList();
  }

  public String getName() {
    return _name;
  }

  public List<Pair<Integer, IDBColumnType>> getIndexes() {
    return _indexes;
  }
}
