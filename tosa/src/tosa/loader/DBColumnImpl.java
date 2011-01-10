package tosa.loader;

import tosa.api.IDBColumn;
import tosa.loader.data.ColumnData;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBColumnImpl implements IDBColumn {
  private final DBTableImpl _table;
  private final ColumnData _columnData;
  private final boolean _isFK;
  private final String _fkTarget;
  private final String _propertyName;
  private final String _propertyTypeName;

  public DBColumnImpl(DBTableImpl table, ColumnData columnData) {
    _table = table;
    _columnData = columnData;

    // If would be nice if we could refactor this isn't a separate method, but final variable assignment
    // rules won't allow that, and I'd rather make the variable
    String colName = _columnData.getName();
    // TODO - AHK - Also check the type to make sure it's appropriate for an fk, and warn if not
    if (colName.endsWith("_id")) {
      // Anything ending in _id is considered an fk
      if (colName.substring(0, colName.length() - 3).contains("_")) {
        // If it's Employer_Company_id, we want the property to be named "Employer" and the target table is "Company"
        int underscorePos = colName.lastIndexOf('_', colName.length() - 4);
        _propertyName = colName.substring(0, underscorePos);
        _fkTarget = colName.substring(underscorePos + 1, colName.length() - 3);
      } else {
        // If it's Company_id, we want the property to be named "Company" and the target table is also "Company"
        _propertyName = colName.substring(0, colName.length() - 3);
        _fkTarget = _propertyName;
      }
      _isFK = true;
      _propertyTypeName = _table.getDatabase().getNamespace() + "." + _fkTarget;
    } else {
      _isFK = false;
      _fkTarget = null;
      _propertyName = getColumnData().getName();
      _propertyTypeName = getColumnData().getColumnType().getGosuTypeName();
    }
  }

  @Override
  public String getName() {
    return _columnData.getName();
  }

  public ColumnData getColumnData() {
    return _columnData;
  }

  public boolean isFK() {
    return _isFK;
  }

  public String getFkTarget() {
    return _fkTarget;
  }

  public boolean isIdColumn() {
    // TODO - AHK - Some day, this should perhaps check to make sure that the column has the right attributes
    return getColumnData().getName().equals("id");
  }

  public DBTableImpl getTable() {
    return _table;
  }

  public String getPropertyName() {
    return _propertyName;
  }

  /**
   * The name of the type that this property should represent.  This is stored as a String
   * until the property info is actually constructed to minimize circular dependencies within
   * the type system.
   *
   * @return the fully-qualified name of the type this property should appear as
   */
  public String getPropertyTypeName() {
    return _propertyTypeName;
  }
}
