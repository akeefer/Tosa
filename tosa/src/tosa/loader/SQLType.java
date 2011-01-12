package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuClassUtil;

import java.util.Collections;
import java.util.List;

public class SQLType extends TypeBase implements IType {
  private SQLTypeData _data;
  private DBTypeLoader _typeLoader;
  private SQLTypeInfo _ti;

  public SQLType(SQLTypeData data, DBTypeLoader loader) {
    _data = data;
    _typeLoader = loader;
    _ti = new SQLTypeInfo(this);
  }

  @Override
  public String getName() {
    return _data.getTypeName();
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
  public DBTypeLoader getTypeLoader() {
    return _typeLoader;
  }

  @Override
  public IType getSupertype() {
    return IJavaType.OBJECT;
  }

  @Override
  public List<? extends IType> getInterfaces() {
    return Collections.emptyList();
  }

  public SQLTypeData getData() {
    return _data;
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _ti;
  }
}
