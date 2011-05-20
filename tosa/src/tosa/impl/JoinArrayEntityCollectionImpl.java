package tosa.impl;

import gw.util.GosuExceptionUtil;
import tosa.api.*;
import tosa.loader.IDBType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/15/11
 * Time: 10:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoinArrayEntityCollectionImpl<T extends IDBObject> extends EntityCollectionImplBase<T> {

  private IDBColumn _srcColumn;
  private IDBColumn _targetColumn;

  public JoinArrayEntityCollectionImpl(IDBObject owner, IDBType fkType, IDBColumn srcColumn, IDBColumn targetColumn, QueryExecutor queryExecutor) {
    super(owner, fkType, queryExecutor);
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
  }

  @Override
  protected void removeImpl(T element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void addImpl(T element) {
    if (!isAlreadyInArray(element)) {

      // If the element hasn't yet been persisted, we have to persist it so that it has an id we can insert into the join table
      if (element.isNew()) {
        try {
          element.update();
        } catch (SQLException e) {
          GosuExceptionUtil.forceThrow(e);
        }
      }

      String sql = SimpleSqlBuilder.substitute("INSERT INTO ${joinTable} (${srcFk}, ${targetFk}) VALUES (?, ?)",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn,
          "targetFk", _targetColumn);
      IPreparedStatementParameter srcParam = _srcColumn.wrapParameterValue(_owner.getId());
      IPreparedStatementParameter destParam = _targetColumn.wrapParameterValue(element.getId());
      _queryExecutor.insert("JoinArrayEntityCollectionImpl.addImpl()", sql, srcParam, destParam);

      if (_cachedResults != null) {
        _cachedResults.add(element);
      }
    } else {
      // If the element is already in the array, and the results have been cached, we really want to update the pointers
      // so that the version in the array is the same pointer that we just got passed in.  That way "add" has the
      // same pointer semantics if the results have been loaded, regardless of whether or not the element is already
      // in the array:  in all cases, the element is now in the array
      if (_cachedResults != null) {
        for (int i = 0; i < _cachedResults.size(); i++) {
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

  private boolean isAlreadyInArray(T element) {
    if (_cachedResults != null) {
      for (T result : _cachedResults) {
        if (result.getId().equals(element.getId())) {
          return true;
        }
      }

      return false;
    } else {
      String sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM ${joinTable} WHERE ${srcFk} = ? AND ${targetFk} = ?",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn,
          "targetFk", _targetColumn);
      IPreparedStatementParameter srcFkParam = _srcColumn.wrapParameterValue(_owner.getId());
      IPreparedStatementParameter targetFkParam = _srcColumn.wrapParameterValue(element.getId());
      int numResults = _queryExecutor.count("JoinArrayEntityCollectionImpl.isAlreadyInArray()", sql, srcFkParam, targetFkParam);
      // TODO - AHK - Report an error if there's more than one result?
      return numResults > 0;
    }
  }

  @Override
  protected List<T> loadResults() {
    String sql = SimpleSqlBuilder.substitute("SELECT * FROM ${targetTable} INNER JOIN ${joinTable} as j ON j.${targetFk} = ${targetTable}.${id} WHERE j.${srcFk} = ?",
        "targetTable", _fkType.getTable(),
        "joinTable", _srcColumn.getTable(),
        "id", _fkType.getTable().getColumn("id"),
        "targetFk", _targetColumn,
        "srcFk", _srcColumn);
    IPreparedStatementParameter param = _srcColumn.wrapParameterValue(_owner.getId());
    return  (List<T>) _queryExecutor.selectEntity("JoinArrayEntityCollectionImpl.loadResultsIfNecessary()", _fkType, sql, param);
  }

  @Override
  protected int issueCountQuery() {
    String sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM ${joinTable} WHERE ${srcFk} = ?",
          "joinTable", _srcColumn.getTable(),
          "srcFk", _srcColumn);
    IPreparedStatementParameter param = _srcColumn.wrapParameterValue(_owner.getId());
    return _queryExecutor.count("JoinArrayEntityCollectionImpl.size()", sql, param);
  }
}
