package tosa.loader;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import gw.lang.reflect.TypeSystem;
import tosa.CachedDBObject;
import tosa.dbmd.DBColumnImpl;
import tosa.db.execution.QueryExecutor;

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
  private final DBColumnImpl _column;

  public DBPropertyInfo(ITypeInfo container, DBColumnImpl column) {
    super(container);
    _column = column;

    String colName = column.getName();
    if (colName.endsWith("_id")) {
      // Anything ending in _id is considered an fk
      if (colName.substring(0, colName.length() - 3).contains("_")) {
        // If it's Employer_Company_id, we want the property to be named "Employer" and the target table is "Company"
        int underscorePos = colName.lastIndexOf('_', colName.length() - 4);
        _name = colName.substring(0, underscorePos);
      } else {
        // If it's Company_id, we want the property to be named "Company" and the target table is also "Company"
        _name = colName.substring(0, colName.length() - 3);
      }
      // TODO - AHK - Use some consistent method to transform a table into a type name
      _type = TypeSystem.getByFullName(column.getFKTarget().getDatabase().getNamespace() + "." + column.getFKTarget().getName());
    } else {
      _name = colName;
      _type = TypeSystem.getByFullName(column.getColumnType().getGosuTypeName());
    }
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
    return !_name.equals(DBTypeInfo.ID_COLUMN);
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
    return _column.getName();
  }

  private class DBPropertyAccessor implements IPropertyAccessor {
    @Override
      public void setValue(Object ctx, Object value) {
        if (_column.isFK() && value != null) {
          ((CachedDBObject) ctx).getColumns().put(getColumnName(), ((CachedDBObject) value).getColumns().get(DBTypeInfo.ID_COLUMN));
        } else {
          ((CachedDBObject) ctx).getColumns().put(getColumnName(), value);
        }
      }

      @Override
      public Object getValue(Object ctx) {
        Object columnValue = ((CachedDBObject) ctx).getColumns().get(getColumnName());
        if (_column.isFK() && columnValue != null) {
          try {
            Object resolvedFK = ((CachedDBObject) ctx).getCachedValues().get(getColumnName());
            if (resolvedFK == null) {
              resolvedFK = new QueryExecutor().selectById(getOwnersType().getName() + "." + getName(),
                  (IDBType) _type, columnValue);
              ((CachedDBObject) ctx).getCachedValues().put(getColumnName(), resolvedFK);
            }
            return resolvedFK;
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        } else {
          return columnValue;
        }
      }
  }

}
