package tosa.loader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;

import java.util.*;

public class StructTypeInfo extends TypeInfoBase implements ITypeInfo {
  private StructType _ownersType;
  private Map<CharSequence,IPropertyInfo> _properties;

  public StructTypeInfo(StructType structType) {
    _ownersType = structType;
    _properties = new HashMap<CharSequence, IPropertyInfo>();
    for (Map.Entry<String, IType> prop : _ownersType.getPropMap().entrySet()) {
      _properties.put(prop.getKey(), new StructPropInfo(this, prop.getKey(), prop.getValue()));
    }
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return new ArrayList<IPropertyInfo>(_properties.values());
  }

  @Override
  public IPropertyInfo getProperty(CharSequence charSequence) {
    return _properties.get(charSequence);
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence charSequence) {
    IPropertyInfo pi = _properties.get(charSequence);
    if (pi != null) {
      return pi.getName();
    } else {
      return null;
    }
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return IJavaType.OBJECT.getTypeInfo().getMethods();
  }

  @Override
  public List<? extends IConstructorInfo> getConstructors() {
    return Collections.emptyList();
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public IType getOwnersType() {
    return _ownersType;
  }
}
