/**
 * Created by IntelliJ IDEA.
 * User: kprevas
 * Date: 2/5/11
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */

package tosa.loader;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import tosa.CachedDBObject;
import tosa.api.IDBTable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author kprevas
 */
public class DBArrayPropertyInfo extends PropertyInfoBase{

  private IType _type;
  private IType _fkType;
  private String _name;

  DBArrayPropertyInfo(ITypeInfo container, IDBTable fkTable) {
    super(container);
    _name = fkTable.getName() + "s";
    String namespace = getOwnersType().getNamespace();
    _fkType = TypeSystem.getByFullName(namespace + "." + fkTable.getName());
    _type = IJavaType.LIST.getGenericType().getParameterizedType(_fkType);
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
    return new DBArrayPropertyAccessor();
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

  private class DBArrayPropertyAccessor implements IPropertyAccessor {
    @Override
    public Object getValue(Object ctx) {
      try {
        Object value = ((CachedDBObject) ctx).getCachedValues().get(_name);
        if (value == null) {
          value = ((DBTypeInfo) _fkType.getTypeInfo()).findInDb(getOwnersType().getName() + "." + _name,
                  Arrays.asList(_fkType.getTypeInfo().getProperty(getOwnersType().getRelativeName())), ctx);
          ((CachedDBObject) ctx).getCachedValues().put(_name, value);
        }
        return value;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void setValue(Object ctx, Object value) {
      throw new UnsupportedOperationException();
    }
  }
}