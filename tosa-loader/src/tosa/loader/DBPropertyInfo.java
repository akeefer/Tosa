package tosa.loader;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import gw.lang.reflect.TypeSystem;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.dbmd.DBColumnImpl;

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

  public IDBColumn getColumn() {
    return _column;
  }

  private class DBPropertyAccessor implements IPropertyAccessor {
    @Override
      public void setValue(Object ctx, Object value) {
        if (_column.isFK()) {
          ((IDBObject) ctx).setFkValue(getColumnName(), (IDBObject) value);
        } else {
          ((IDBObject) ctx).setColumnValue(getColumnName(), value);
        }
      }

      @Override
      public Object getValue(Object ctx) {
        if (_column.isFK()) {
          return ((IDBObject) ctx).getFkValue(getColumnName());
        } else {
          return ((IDBObject) ctx).getColumnValue(getColumnName());
        }
      }
  }

}
