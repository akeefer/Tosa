package tosa.impl

uses tosa.api.IDBObject
uses tosa.api.IDBColumn
uses tosa.loader.IDBType
uses tosa.loader.DBTypeInfo
uses java.lang.IllegalArgumentException
uses tosa.impl.query.SqlStringSubstituter

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/15/12
 * Time: 11:31 PM
 * To change this template use File | Settings | File Templates.
 */
class ReverseFkEntityCollectionImpl<T extends IDBObject> extends EntityCollectionImplBase<T> {

  private var _fkColumn : IDBColumn

  public construct(owner : IDBObject, fkType : IDBType, fkColumn : IDBColumn, queryExecutor : QueryExecutor) {
    super(owner, fkType, queryExecutor);
    _fkColumn = fkColumn;
  }

  override protected function issueCountQuery() : int{
    var text = SqlStringSubstituter.substitute("SELECT count(*) as count FROM :fkTable WHERE :fkColumn = :fkId",
        {"fkTable" -> _fkType.getTable(),
         "fkColumn" -> _fkColumn,
         "fkId" -> _owner.Id})
    return _queryExecutor.count("ReverseFkEntityCollectionImpl.size()", text.Sql, text.Params)
  }

  override protected function loadResults() : List<T> {
    var idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN);
    var sql = SqlStringSubstituter.substitute("SELECT * FROM :fkTable WHERE :fkColumn = :fkId ORDER BY :idColumn",
        {"fkTable" ->_fkColumn.getTable(),
         "fkColumn" -> _fkColumn,
         "idColumn" -> idColumn,
         "fkId" -> _owner.Id})
    return _queryExecutor.selectEntity("ReverseFkEntityCollectionImpl.loadResultsIfNecessary()", _fkType, sql.Sql, sql.Params) as List<T>
  }

  protected override function addImpl(element : T) {
    var existingId = element.getColumnValue(_fkColumn.getName());
    if (existingId == null) {
      // We always set the back-pointer and the column
      element.setFkValue(_fkColumn.getName(), _owner);
      if (element.isNew()) {
        // For newly-created elements, we insert them immediately
        element.update();
      } else {
        // For entities already in the database, we issue the update statement in the database directly
        var idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN);
        var updateSql = SqlStringSubstituter.substitute("UPDATE :fkTable SET :fkColumn = :fkId WHERE :idColumn = :id",
            {"fkTable" -> _fkColumn.getTable(),
             "fkColumn" -> _fkColumn,
             "idColumn" -> idColumn,
             "fkId" -> _owner.Id,
             "id" -> element.Id});
        _queryExecutor.update("ReverseFkEntityCollectionImpl.add()", updateSql.Sql, updateSql.Params);
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

  override protected function removeImpl(element : T) {
    var fkId = element.getColumnValue(_fkColumn.getName());
    if (!_owner.getId().equals(fkId)) {
      throw new IllegalArgumentException("The element with id " + element.getId() + " is not a member of the array on element " + _owner.getId());
    }

    element.setFkValue(_fkColumn.getName(), null);
    var idColumn = _fkColumn.getTable().getColumn(DBTypeInfo.ID_COLUMN)
    var updateSql = SqlStringSubstituter.substitute("UPDATE :fkTable SET :fkColumn = NULL WHERE :idColumn = :id",
        {"fkTable" -> _fkColumn.getTable(),
         "fkColumn" -> _fkColumn,
         "idColumn" -> idColumn,
         "id" -> element.Id})
    _queryExecutor.update("ReverseFkEntityCollectionImpl.remove()", updateSql.Sql, updateSql.Params);

    if (_cachedResults != null) {
      // The _cachedResults might contain a different pointer, so we have to match up by id
      for (i in 0..|_cachedResults.size()) {
        if (element.getId().equals(_cachedResults.get(i).getId())) {
          _cachedResults.remove(i);
          break;
        }
      }
    }
  }
}