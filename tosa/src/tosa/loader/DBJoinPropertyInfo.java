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
import gw.util.GosuStringUtil;
import tosa.CachedDBObject;
import tosa.Join;
import tosa.JoinResult;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author kprevas
 */
public class DBJoinPropertyInfo extends PropertyInfoBase{

  private String _name;
  private IType _type;
  private IType _fkType;
  private Join _join;

  DBJoinPropertyInfo(ITypeInfo container, Join join) {
    super(container);
    _join = join;
    _name = _join.getPropName();
    String namespace = getOwnersType().getNamespace();
    _fkType = TypeSystem.getByFullName(namespace + "." + join.getTargetTable().getName());
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
    return new DBJoinPropertyAccessor();
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

  private class DBJoinPropertyAccessor implements IPropertyAccessor {
    @Override
    public Object getValue(Object ctx) {
      Object value = ((CachedDBObject) ctx).getColumns().get(_name);
      if (value == null) {
        String j = _join.getJoinTable().getName();
        String t = _join.getTargetTable().getName();
        String o = getOwnersType().getRelativeName();
        if (GosuStringUtil.equals(t, o)) {
          o += "_src";
          t += "_dest";
        }
        String id = ((CachedDBObject) ctx).getColumns().get(DBTypeInfo.ID_COLUMN).toString();
        try {
          List<CachedDBObject> result = ((DBTypeInfo) _fkType.getTypeInfo()).findFromSqlMutable(
                  "select * from \"" + _join.getTargetTable().getName() + "\", \"" + j + "\" as j where j.\"" + t + "_id\" = \"" + _join.getTargetTable().getName() + "\".\"id\" and j.\"" + o + "_id\" = " + id
          );
          value = new JoinResult(result, ((IDBType)getOwnersType()).getTable().getDatabase(), j, o, t, id);
          ((CachedDBObject) ctx).getColumns().put(_name, value);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      return value;
    }

    @Override
    public void setValue(Object ctx, Object value) {
      throw new UnsupportedOperationException();
    }
  }
}