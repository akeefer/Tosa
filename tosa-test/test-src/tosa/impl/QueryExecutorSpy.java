package tosa.impl;

import tosa.api.IDBObject;
import tosa.api.IDatabase;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.IDBType;

import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: alan
* Date: 5/17/11
* Time: 9:37 AM
* To change this template use File | Settings | File Templates.
*/
class QueryExecutorSpy implements QueryExecutor {

  private QueryExecutorImpl _delegate;
  private String _count;
  private String _select;
  private String _update;
  private String _insert;
  private String _delete;

  QueryExecutorSpy(IDatabase db) {
    _delegate = new QueryExecutorImpl(db);
  }

  @Override
  public int count(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
    _count = sqlStatement;
    return _delegate.count(profilerTag, sqlStatement, parameters);
  }

  @Override
  public List<IDBObject> selectEntity(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters) {
    _select = sqlStatement;
    return _delegate.selectEntity(profilerTag, targetType, sqlStatement, parameters);
  }

  @Override
  public void update(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
    _update = sqlStatement;
    _delegate.update(profilerTag, sqlStatement, parameters);
  }

  @Override
  public void insert(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
    _insert = sqlStatement;
    _delegate.insert(profilerTag, sqlStatement, parameters);
  }

  @Override
  public void delete(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
    _delete = sqlStatement;
    _delegate.delete(profilerTag, sqlStatement, parameters);
  }

  public boolean countCalled() {
    return _count != null;
  }

  public boolean selectCalled() {
    return _select != null;
  }

  public boolean updateCalled() {
    return _update != null;
  }

  public boolean insertCalled() {
    return _insert != null;
  }

  public boolean deleteCalled() {
    return _delete != null;
  }

  public boolean anyCalled() {
    return countCalled() || selectCalled() || updateCalled() || insertCalled() || deleteCalled();
  }

  public void reset() {
    _count = null;
    _select = null;
    _update = null;
    _insert = null;
    _delete = null;
  }
}
