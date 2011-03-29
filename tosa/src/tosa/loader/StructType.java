package tosa.loader;

import gw.config.CommonServices;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuObject;
import gw.lang.reflect.java.IJavaBackedType;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuClassUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructType extends TypeBase implements IType {

  private String _name;
  private Map<String,IType> _propMap;
  private ITypeLoader _loader;

  public StructType(ITypeLoader loader, String name, Map<String, IType> typeMap) {
    _loader = loader;
    _name = name;
    _propMap = typeMap;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getRelativeName() {
    return GosuClassUtil.getNameNoPackage(getName());
  }

  @Override
  public String getNamespace() {
    return GosuClassUtil.getPackage(getName());
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return _loader;
  }

  @Override
  public IType getSupertype() {
    return IJavaType.OBJECT;
  }

  @Override
  public List<? extends IType> getInterfaces() {
    return Collections.emptyList();
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return new StructTypeInfo(this);
  }

  public Map<String, IType> getPropMap() {
    return _propMap;
  }

  public StructInstance newInstance(Map values) {
    return new StructInstance(this, values);
  }
}
