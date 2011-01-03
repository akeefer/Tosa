package tosa.loader;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import gw.lang.reflect.TypeSystem;
import tosa.CachedDBObject;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBPropertyInfo extends PropertyInfoBase {

  private final String _name;
  private final IType _type;
  private final ColumnTypeData _column;

  public DBPropertyInfo(ITypeInfo container, ColumnTypeData column) {
    super(container);
    _column = column;
    _name = column.getPropertyName();
    _type = TypeSystem.getByFullName(_column.getPropertyTypeName());
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean isReadable() {
    return true;
  }

  @Override
  public boolean isWritable(IType whosAskin) {
    return !_name.equals("id");
  }

  @Override
  public IPropertyAccessor getAccessor() {
    return new DBPropertyAccessor();
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasAnnotation(IType type) {
    return false;
  }

  @Override
  public IType getFeatureType() {
    return _type;
  }

  public String getColumnName() {
    return _column.getColumnData().getName();
  }

  private class DBPropertyAccessor implements IPropertyAccessor {
    @Override
      public void setValue(Object ctx, Object value) {
        if (_column.isFK() && value != null) {
          ((CachedDBObject) ctx).getColumns().put(getColumnName(), ((CachedDBObject) value).getColumns().get("id"));
        } else {
          ((CachedDBObject) ctx).getColumns().put(getColumnName(), value);
        }
      }

      @Override
      public Object getValue(Object ctx) {
        Object columnValue = ((CachedDBObject) ctx).getColumns().get(getColumnName());
        if (_column.isFK() && columnValue != null) {
          try {
            return ((DBTypeInfo) _type.getTypeInfo()).selectById(columnValue);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        } else {
          return columnValue;
        }
      }
  }

}
