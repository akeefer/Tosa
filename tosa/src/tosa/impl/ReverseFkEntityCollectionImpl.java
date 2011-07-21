package tosa.impl;

import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - AHK - This name pretty much sucks
public class ReverseFkEntityCollectionImpl<T extends IDBObject> extends EntityCollectionImplBase<T> {

  private IDBColumn _fkColumn;

  public ReverseFkEntityCollectionImpl(IDBObject owner, IDBType fkType, IDBColumn fkColumn, QueryExecutor queryExecutor) {
    super(owner, fkType, queryExecutor);
    _fkColumn = fkColumn;
  }


  @Override
  protected int issueCountQuery() {
    String text = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM ${fkTable} WHERE ${fkColumn} = ?",
        "fkTable", _fkType.getTable(),
        "fkColumn", _fkColumn);
    IPreparedStatementParameter param = _fkColumn.wrapParameterValue(_owner.getColumnValue(DBTypeInfo.ID_COLUMN));
    return _queryExecutor.count("ReverseFkEntityCollectionImpl.size()", text, param);
  }

  @Override
  protected List<T> loadResults() {
    IDBColumn idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN);
    String sql = SimpleSqlBuilder.substitute("SELECT * FROM ${fkTable} WHERE ${fkColumn} = ? ORDER BY ${idColumn}",
        "fkTable", _fkColumn.getTable(),
        "fkColumn", _fkColumn,
        "idColumn", idColumn);
    IPreparedStatementParameter param = _fkColumn.wrapParameterValue(_owner.getColumnValue(DBTypeInfo.ID_COLUMN));
    return (List<T>) _queryExecutor.selectEntity("ReverseFkEntityCollectionImpl.loadResultsIfNecessary()", _fkType, sql, param);
  }

  @Override
  protected void addImpl(T element) {
    Object existingId = element.getColumnValue(_fkColumn.getName());
    if (existingId == null) {
      // We always set the back-pointer and the column
      element.setFkValue(_fkColumn.getName(), _owner);
      if (element.isNew()) {
        // For newly-created elements, we insert them immediately
        element.update();
      } else {
        // For entities already in the database, we issue the update statement in the database directly
        IDBColumn idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN);
        String updateSql = SimpleSqlBuilder.substitute("UPDATE ${fkTable} SET ${fkColumn} = ? WHERE ${idColumn} = ?",
            "fkTable", _fkColumn.getTable(),
            "fkColumn", _fkColumn,
            "idColumn", idColumn);
        IPreparedStatementParameter fkParam = idColumn.wrapParameterValue(_owner.getColumnValue(DBTypeInfo.ID_COLUMN));
        IPreparedStatementParameter idParam = idColumn.wrapParameterValue(element.getColumnValue(DBTypeInfo.ID_COLUMN));
        _queryExecutor.update("ReverseFkEntityCollectionImpl.add()", updateSql, fkParam, idParam);
      }
      if (_cachedResults != null) {
        // TODO - AHK - Unclear if the list should be re-sorted, or if it should be added in insertion order
        _cachedResults.add(element);
      }
    } else if (existingId.equals(_owner.getColumnValue(DBTypeInfo.ID_COLUMN))) {
      // That's fine, it's a no-op, but we still want to set the fk value so you get the right pointer back when you reference the fk
      element.setFkValue(_fkColumn.getName(), _owner);
    } else {
      throw new IllegalArgumentException("The element with id " + element.getColumnValue(DBTypeInfo.ID_COLUMN) + " is already attached to another owner, with id " + existingId);
    }
  }

  @Override
  protected void removeImpl(T element) {
    Object fkId = element.getColumnValue(_fkColumn.getName());
    if (!_owner.getId().equals(fkId)) {
      throw new IllegalArgumentException("The element with id " + element.getId() + " is not a member of the array on element " + _owner.getId());
    }

    element.setFkValue(_fkColumn.getName(), null);
    IDBColumn idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN);
    String updateSql = SimpleSqlBuilder.substitute("UPDATE ${fkTable} SET ${fkColumn} = NULL WHERE ${idColumn} = ?",
        "fkTable", _fkColumn.getTable(),
        "fkColumn", _fkColumn,
        "idColumn", idColumn);
    IPreparedStatementParameter idParam = idColumn.wrapParameterValue(element.getColumnValue(DBTypeInfo.ID_COLUMN));
    _queryExecutor.update("ReverseFkEntityCollectionImpl.remove()", updateSql, idParam);

    if (_cachedResults != null) {
      // The _cachedResults might contain a different pointer, so we have to match up by id
      for (int i = 0; i < _cachedResults.size(); i++) {
        if (element.getId().equals(_cachedResults.get(i).getId())) {
          _cachedResults.remove(i);
          break;
        }
      }
    }
  }
}
