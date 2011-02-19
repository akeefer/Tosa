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
import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.db.execution.QueryExecutor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author kprevas
 */
public class DBArrayPropertyInfo extends PropertyInfoBase {

  private IType _type;
  private IDBColumn _fkColumn;
  private IType _fkType;
  private String _name;

  DBArrayPropertyInfo(ITypeInfo container, IDBColumn fkColumn) {
    super(container);
    _fkColumn = fkColumn;
    // TODO - AHK - This algorithm probably needs to be a bit more complicated . . .
    _name = fkColumn.getTable().getName() + "s";
    String namespace = getOwnersType().getNamespace();
    _fkType = TypeSystem.getByFullName(namespace + "." + _fkColumn.getTable().getName());
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
      CachedDBObject dbObject = (CachedDBObject) ctx;
      try {
        Object value = dbObject.getCachedValues().get(_name);
        if (value == null) {
          Object id = dbObject.getColumns().get(DBTypeInfo.ID_COLUMN);
          value = new QueryExecutor().findFromSql(
              getOwnersType().getName() + "." + _name,
              (IDBType) _fkType,
              "select * from \"" + _fkColumn.getTable().getName() + "\" where \"" + _fkColumn.getName() + "\" = ?",
              Collections.singletonList(dbObject.getIntrinsicType().getTable().getColumn(DBTypeInfo.ID_COLUMN).wrapParameterValue(id)));
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