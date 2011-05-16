package tosa.impl;

import tosa.api.*;
import tosa.loader.IDBType;

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
public class JoinArrayEntityCollectionImpl<T extends IDBObject> implements EntityCollection<T> {

  private IDBObject _owner;
  private IDBType _fkType;
  private IDBColumn _srcColumn;
  private IDBColumn _targetColumn;
  private QueryExecutor _queryExecutor;
  private List<T> _cachedResults;

  public JoinArrayEntityCollectionImpl(IDBObject owner, IDBType fkType, IDBColumn srcColumn, IDBColumn targetColumn, QueryExecutor queryExecutor) {
    _owner = owner;
    _fkType = fkType;
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
    _queryExecutor = queryExecutor;
  }

  @Override
  public int size() {
    if (_cachedResults == null) {
      String sql = SimpleSqlBuilder.select("count(*) as count").from(_srcColumn.getTable()).where(_srcColumn, "=", "?").toString();
      IPreparedStatementParameter param = _srcColumn.wrapParameterValue(_owner.getId());
      return _queryExecutor.count("JoinArrayEntityCollectionImpl.size()", sql, param);
    } else {
      return _cachedResults.size();
    }
  }

  @Override
  public T get(int index) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void add(T element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void remove(T element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void load() {
    loadResultsIfNecessary();
  }

  @Override
  public void unload() {
    _cachedResults = null;
  }

  @Override
  public Iterator<T> iterator() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  // ---------------------------

  private void loadResultsIfNecessary() {
    if (_cachedResults == null) {
      // String query = "select * from \"" + _join.getTargetTable().getName() + "\", \"" + j + "\" as j where j.\"" + t + "_id\" = \"" + _join.getTargetTable().getName() + "\".\"id\" and j.\"" + o + "_id\" = ?";

      // SELECT * FROM ${targetTable}, ${joinTable} as j WHERE j.${targetFk} = ${targetTable}.${id} AND j.${srcFk} = ?
      IDBTable targetTable = _fkType.getTable();
      IDBTable joinTable = _srcColumn.getTable();
//      String sql = SimpleSqlBuilder.select("*").from(targetTable)._(", ")._(joinTable)._(" as j ").where()._("j.")._(_srcColumn)._("=")._(targetTable)._(targetId).and()._j
    }
  }
}
