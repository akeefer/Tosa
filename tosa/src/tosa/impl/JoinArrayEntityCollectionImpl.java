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
    //To change body of implemented methods use File | Settings | File Templates.
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
