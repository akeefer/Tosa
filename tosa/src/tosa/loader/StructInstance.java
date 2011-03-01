package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.gs.IGosuObject;

import java.io.Serializable;
import java.util.Map;

public class StructInstance implements IGosuObject{
  private Map _values;
  private StructType _type;

  public StructInstance(StructType structType, Map values) {
    _type = structType;
    _values = values;
  }

  public Object getValue(String name) {
    return _values.get(name);
  }

  @Override
  public IType getIntrinsicType() {
    return _type;
  }
}
