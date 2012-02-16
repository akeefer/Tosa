package tosa.impl

uses tosa.api.IDBObject
uses tosa.api.IDBColumn
uses tosa.loader.IDBType
uses java.lang.IllegalArgumentException

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/15/12
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
class JoinArrayEntityCollectionImpl<T extends IDBObject> extends EntityCollectionImplBase<T> {
   
  private var _srcColumn : IDBColumn
  private var _targetColumn : IDBColumn

  public construct(owner : IDBObject, fkType : IDBType, srcColumn : IDBColumn, targetColumn : IDBColumn, queryExecutor : QueryExecutor) {
    super(owner, fkType, queryExecutor);
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
  }

  protected override function removeImpl(element : T) {
    if (!isAlreadyInArray(element)) {
      throw new IllegalArgumentException("The element " + element.getDBTable().getName() + "(" + element.getId() +
              ") cannot be removed from the join array on " + _owner.getDBTable().getName() + "(" + _owner.getId() + ") as it's not currently in the array");
    }

    var sql = SimpleSqlBuilder.substitute("DELETE FROM \${joinTable} WHERE \${srcFk} =? AND \${targetFk} = ?",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn,
          "targetFk", _targetColumn);

    var srcParam = _srcColumn.wrapParameterValue(_owner.getId());
    var targetParam = _targetColumn.wrapParameterValue(element.getId());
    _queryExecutor.delete("JoinArrayEntityCollectionImpl.removeImpl()", sql, {srcParam, targetParam});

    // If the results have already been loaded, we need to remove the element.  We can't do just .equals() or a pointer compare,
    // since the version in there might be different, so instead we want to compare ids
    if (_cachedResults != null) {
      for ( i in 0..|_cachedResults.size()) {
        if (_cachedResults.get(i).getId().equals(element.getId())) {
          _cachedResults.remove(i);
          break;
        }
      }
    }
  }

  protected override function addImpl(element : T) {
    if (!isAlreadyInArray(element)) {

      // If the element hasn't yet been persisted, we have to persist it so that it has an id we can insert into the join table
      if (element.isNew()) {
        element.update();
      }

      var sql = SimpleSqlBuilder.substitute("INSERT INTO \${joinTable} (\${srcFk}, \${targetFk}) VALUES (?, ?)",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn,
          "targetFk", _targetColumn);
      var srcParam = _srcColumn.wrapParameterValue(_owner.getId());
      var targetParam = _targetColumn.wrapParameterValue(element.getId());
      _queryExecutor.insert("JoinArrayEntityCollectionImpl.addImpl()", sql, {srcParam, targetParam});

      if (_cachedResults != null) {
        _cachedResults.add(element);
      }
    } else {
      // If the element is already in the array, and the results have been cached, we really want to update the pointers
      // so that the version in the array is the same pointer that we just got passed in.  That way "add" has the
      // same pointer semantics if the results have been loaded, regardless of whether or not the element is already
      // in the array:  in all cases, the element is now in the array
      if (_cachedResults != null) {
        for (i in 0..|_cachedResults.size()) {
          if (_cachedResults.get(i).getId().equals(element.getId())) {
            _cachedResults.set(i, element);
            break; // There should only ever be one match, so stop iterating
          }
        }
      }
    }

    // Set the array back-pointer
    // TODO - AHK
  }

  private function isAlreadyInArray(element : T) : boolean {
    if (_cachedResults != null) {
      for (result in _cachedResults) {
        if (result.getId().equals(element.getId())) {
          return true;
        }
      }

      return false;
    } else {
      var sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM \${joinTable} WHERE \${srcFk} = ? AND \${targetFk} = ?",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn,
          "targetFk", _targetColumn);
      var srcFkParam = _srcColumn.wrapParameterValue(_owner.getId());
      var targetFkParam = _srcColumn.wrapParameterValue(element.getId());
      var numResults = _queryExecutor.count("JoinArrayEntityCollectionImpl.isAlreadyInArray()", sql, {srcFkParam, targetFkParam});
      // TODO - AHK - Report an error if there's more than one result?
      return numResults > 0;
    }
  }

  override protected function loadResults() : List<T> {
    var sql = SimpleSqlBuilder.substitute("SELECT * FROM \${targetTable} INNER JOIN \${joinTable} as j ON j.\${targetFk} = \${targetTable}.\${id} WHERE j.\${srcFk} = ?",
        "targetTable", _fkType.getTable(),
        "joinTable", _srcColumn.getTable(),
        "id", _fkType.getTable().getColumn("id"),
        "targetFk", _targetColumn,
        "srcFk", _srcColumn);
    var param = _srcColumn.wrapParameterValue(_owner.getId());
    return _queryExecutor.selectEntity("JoinArrayEntityCollectionImpl.loadResultsIfNecessary()", _fkType, sql, {param}) as List<T>
  }

  protected override function issueCountQuery() : int {
    var sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM \${joinTable} WHERE \${srcFk} = ?",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn);
    var param = _srcColumn.wrapParameterValue(_owner.getId());
    return _queryExecutor.count("JoinArrayEntityCollectionImpl.size()", sql, {param});
  }

}