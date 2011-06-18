package tosa.impl.md;

import tosa.api.IDBColumn;
import tosa.api.IDBFkArray;
import tosa.api.IDBTable;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/13/11
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBFkArrayImpl implements IDBFkArray {

  private String _propertyName;
  private IDBTable _ownerTable;
  private IDBTable _targetTable;
  private IDBColumn _fkColumn;

  public DBFkArrayImpl(String propertyName, IDBTable ownerTable, IDBTable targetTable, IDBColumn fkColumn) {
    _propertyName = propertyName;
    _ownerTable = ownerTable;
    _targetTable = targetTable;
    _fkColumn = fkColumn;
  }

  @Override
  public IDBColumn getFkColumn() {
    return _fkColumn;
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
