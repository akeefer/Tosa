package tosa.dbmd;

import tosa.api.IDBColumn;
import tosa.api.IDBColumnType;
import tosa.api.IDBTable;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.DBTypeInfo;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        _fkTarget = colName.substring(underscorePos + 1, colName.length() - 3);
      } else {
        // If it's Company_id, we want the property to be named "Company" and the target table is also "Company"
        _fkTarget = colName.substring(0, colName.length() - 3);
      }
      _isFK = true;
    } else {
      _isFK = false;
      _fkTarget = null;
    }
  }

  @Override
  public String getName() {
    return _columnData.getName();
  }

  @Override
  public String getPossiblyQuotedName() {
      return _columnData.getPossiblyQuotedName();
  }

    @Override
  public boolean isFK() {
    return _isFK;
  }

  @Override
  public IDBTable getFKTarget() {
    if (_fkTarget == null) {
      return null;
    } else {
      return _table.getDatabase().getTable(_fkTarget);
    }
  }

  @Override
  public IDBColumnType getColumnType() {
    return _columnData.getColumnType();
  }

  @Override
  public IPreparedStatementParameter wrapParameterValue(final Object value) {
    return new IPreparedStatementParameter() {
      @Override
      public void setParameter(PreparedStatement statement, int index) throws SQLException {
        _columnData.getColumnType().setParameter(statement, index, value);
      }
    };
  }

  // TODO - AHK - It would be nice if this wasn't necessary
  String getFKTargetName() {
    return _fkTarget;
  }

  public boolean isIdColumn() {
    // TODO - AHK - Some day, this should perhaps check to make sure that the column has the right attributes
    return getName().equals(DBTypeInfo.ID_COLUMN);
  }

  @Override
  public IDBTable getTable() {
    return _table;
  }
}
