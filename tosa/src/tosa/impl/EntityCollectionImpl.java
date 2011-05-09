package tosa.impl;

import gw.internal.gosu.parser.GosuExceptionInfo;
import gw.util.GosuExceptionUtil;
import tosa.api.EntityCollection;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - AHK - This name pretty much sucks
public class EntityCollectionImpl<T extends IDBObject> implements EntityCollection<T> {

  private IDBObject _owner;
  private IDBType _fkType;
  private IDBColumn _fkColumn;
  private SimpleQueryExecutor _queryExecutor;
  private List<T> _cachedResults;

  public EntityCollectionImpl(IDBObject owner, IDBType fkType, IDBColumn fkColumn, SimpleQueryExecutor queryExecutor) {
    _owner = owner;
    _fkType = fkType;
    _fkColumn = fkColumn;
    _queryExecutor = queryExecutor;
  }

  /*Object id = dbObject.getColumns().get(DBTypeInfo.ID_COLUMN);
value = new QueryExecutor().findFromSql(
  getOwnersType().getName() + "." + _name,
  (IDBType) _fkType,
  "select * from \"" + _fkColumn.getTable().getName() + "\" where \"" + _fkColumn.getName() + "\" = ?",
  Collections.singletonList(dbObject.getIntrinsicType().getTable().getColumn(DBTypeInfo.ID_COLUMN).wrapParameterValue(id)));*/

  @Override
  public int size() {
    if (_cachedResults == null) {
      // TODO - AHK - Always quote?  Never quote?
      // TODO - AHK - Better debug name here
      return _queryExecutor.countWhere("EntityCollectionImpl.size()", _fkType, "\"" + _fkColumn.getName() + "\" = ?", _fkColumn.wrapParameterValue(_owner.getColumnValue(DBTypeInfo.ID_COLUMN)));
    } else {
      return _cachedResults.size();
    }
  }



  @Override
  public Iterator<T> iterator() {
    loadResultsIfNecessary();
    return new EntityCollectionImplIterator(_cachedResults.iterator());
  }

  @Override
  public T get(int index) {
    loadResultsIfNecessary();
    if (index < 0 || index > _cachedResults.size() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is invalid for an EntityCollectionImpl of size " + _cachedResults.size());
    }
    return _cachedResults.get(index);
  }

  @Override
  public void add(T element) {
    if (!_fkType.isAssignableFrom(element.getIntrinsicType())) {
      throw new IllegalArgumentException("An element of type " + element.getIntrinsicType() + " cannot be added to a collection of type " + _fkType);
    }

    if (_owner.isNew()) {
      throw new IllegalStateException("The element cannot be added to the list, as the owner is not yet committed.  You must commit the owner prior to added anything to the list.");
    }

    Object existingId = element.getColumnValue(_fkColumn.getName());
    if (existingId == null) {
      if (element.isNew()) {
        element.setColumnValue(_fkColumn.getName(), element.getColumnValue(DBTypeInfo.ID_COLUMN));
        try {
          element.update();
        } catch (SQLException e) {
          GosuExceptionUtil.forceThrow(e);
        }
      } else {
        // Issue the DB update
      }
      // TODO - Set the back-pointer
      // TODO - AHK - Unclear if the list should be re-sorted, or if it should be added in insertion order
      if (_cachedResults != null) {
        _cachedResults.add(element);
      }
    } else if (existingId.equals(_owner.getColumnValue(DBTypeInfo.ID_COLUMN))) {
      // That's fine, it's a no-op
    } else {
      throw new IllegalArgumentException("The element with id " + element.getColumnValue(DBTypeInfo.ID_COLUMN) + " is already attached to another owner, with id " + existingId);
    }
  }

  @Override
  public void remove(T element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private void loadResultsIfNecessary() {
    if (_cachedResults == null) {
      // TODO - AHK - Profiler tag
      // TODO - AHK - Constant for the id column name
      _cachedResults = (List<T>) _queryExecutor.find("TODO", _fkType, "SELECT * FROM \"" + _fkColumn.getTable().getName() + "\" WHERE \"" + _fkColumn.getName() + "\" = ? ORDER BY \"id\"", _fkColumn.wrapParameterValue(_owner.getColumnValue(DBTypeInfo.ID_COLUMN)));
    }
  }

  private class EntityCollectionImplIterator implements Iterator<T> {
    Iterator<T> _wrappedIterator;

    private EntityCollectionImplIterator(Iterator<T> wrappedIterator) {
      _wrappedIterator = wrappedIterator;
    }

    @Override
    public boolean hasNext() {
      return _wrappedIterator.hasNext();
    }

    @Override
    public T next() {
      return _wrappedIterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
