package tosa.loader;

import gw.lang.reflect.IType;
import gw.util.Pair;
import tosa.loader.data.ColumnType;

import java.util.ArrayList;
import java.util.List;

public class SQLParameterInfo {

  private String _name;
  private List<Pair<Integer, ColumnType>> _indexes;

  public SQLParameterInfo(String name) {
    _name = name;
    _indexes = new ArrayList();
  }

  public String getName() {
    return _name;
  }

  public List<Pair<Integer, ColumnType>> getIndexes() {
    return _indexes;
  }
}
