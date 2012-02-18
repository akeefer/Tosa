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
import tosa.api.EntityCollection;
import tosa.api.IDBArray;
import tosa.api.IDBObject;

import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author kprevas
 */
public class DBArrayPropertyInfo extends PropertyInfoBase {

  private final IType _type;
  private final IDBArray _dbArray;

  DBArrayPropertyInfo(ITypeInfo container, IDBArray dbArray) {
    super(container);
    _dbArray = dbArray;
    String namespace = getOwnersType().getNamespace();
    IType fkType = TypeSystem.getByFullName(namespace + "." + dbArray.getTargetTable().getName());
    _type = TypeSystem.get(EntityCollection.class).getGenericType().getParameterizedType(fkType);
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
    return _dbArray.getPropertyName();
  }

  @Override
  public IType getFeatureType() {
    return _type;
  }

  private class DBArrayPropertyAccessor implements IPropertyAccessor {
    @Override
    public Object getValue(Object ctx) {
      IDBObject dbObject = (IDBObject) ctx;
      return dbObject.getArray(_dbArray);
    }

    @Override
    public void setValue(Object ctx, Object value) {
      throw new UnsupportedOperationException();
    }
  }
}