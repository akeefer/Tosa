package tosa.loader;

import gw.lang.reflect.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StructPropInfo extends PropertyInfoBase implements IPropertyInfo {
  private String _name;
  private IType _type;

  public StructPropInfo(ITypeInfo container, String name, IType type) {
    super(container);
    _name = name;
    _type = type;
  }

  @Override
  public boolean isReadable() {
    return true;
  }

  @Override
  public boolean isWritable(IType iType) {
    return false;
  }

  @Override
  public IPropertyAccessor getAccessor() {
    return new IPropertyAccessor() {
      @Override
      public Object getValue(Object ctx) {
        return ((StructInstance) ctx).getValue(_name);
      }

      @Override
      public void setValue(Object ctx, Object val) {
        throw new IllegalStateException("Cannot set values of structs!");
      }
    };
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public IType getFeatureType() {
    return _type;
  }
}
