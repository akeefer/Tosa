package tosa.impl.md;

import tosa.api.IDBColumn;
import tosa.api.IDBJoinArray;
import tosa.api.IDBTable;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/13/11
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBJoinArrayImpl implements IDBJoinArray {

  private final String _propertyName;

  private final IDBTable _ownerTable;
  private final IDBTable _targetTable;

  private final IDBTable _joinTable;
  private final IDBColumn _srcColumn;
  private final IDBColumn _targetColumn;

  public DBJoinArrayImpl(String propertyName, IDBTable ownerTable, IDBTable targetTable, IDBTable joinTable, IDBColumn srcColumn, IDBColumn targetColumn) {
    // TODO - AHK - Validate that nothing is null, and other relationships
    _propertyName = propertyName;
    _ownerTable = ownerTable;
    _targetTable = targetTable;
    _joinTable = joinTable;
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
  }

  @Override
  public IDBTable getJoinTable() {
    return _joinTable;
  }

  @Override
  public IDBColumn getSrcColumn() {
    return _srcColumn;
  }

  @Override
  public IDBColumn getTargetColumn() {
    return _targetColumn;
  }

  @Override
  public String getPropertyName() {
    return _propertyName;
  }

  @Override
  public IDBTable getOwnerTable() {
    return _ownerTable;
  }

  @Override
  public IDBTable getTargetTable() {
    return _targetTable;
  }
}
