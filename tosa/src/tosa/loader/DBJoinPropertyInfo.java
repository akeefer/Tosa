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
import org.slf4j.profiler.Profiler;
import tosa.CachedDBObject;
import tosa.Join;
import tosa.JoinResult;
import tosa.api.IDatabase;
import tosa.db.execution.QueryExecutor;

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
  private IDBType _fkType;
  private Join _join;

  DBJoinPropertyInfo(ITypeInfo container, Join join) {
    super(container);
    _join = join;
    _name = _join.getPropName();
    String namespace = getOwnersType().getNamespace();
    _fkType = (IDBType) TypeSystem.getByFullName(namespace + "." + join.getTargetTable().getName());
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
      Object value = ((CachedDBObject) ctx).getCachedValues().get(_name);
      if (value == null) {
        String j = _join.getJoinTable().getName();
        String t = _join.getTargetTable().getName();
        String o = getOwnersType().getRelativeName();
        if (GosuStringUtil.equals(t, o)) {
          o += "_src";
          t += "_dest";
        }
        String id = ((CachedDBObject) ctx).getColumns().get(DBTypeInfo.ID_COLUMN).toString();
        String query = "select * from \"" + _join.getTargetTable().getName() + "\", \"" + j + "\" as j where j.\"" + t + "_id\" = \"" + _join.getTargetTable().getName() + "\".\"id\" and j.\"" + o + "_id\" = ?";
        Profiler profiler = Util.newProfiler(getOwnersType().getName() + "." + _name);
        profiler.start(query + " (" + id + ")");
        try {
          IDatabase db = _join.getTargetTable().getDatabase();
          List<CachedDBObject> result = db.getDBExecutionKernel().executeSelect(query, new QueryExecutor.CachedDBQueryResultProcessor(_fkType),
              db.wrapParameter(id, _join.getJoinTable().getColumn(o + "_id")));
          value = new JoinResult(result, _join.getJoinTable().getDatabase(), _join.getJoinTable(), _join.getJoinTable().getColumn(o + "_id"), _join.getJoinTable().getColumn(t + "_id"), id);
          ((CachedDBObject) ctx).getCachedValues().put(_name, value);
        } finally {
          profiler.stop();
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