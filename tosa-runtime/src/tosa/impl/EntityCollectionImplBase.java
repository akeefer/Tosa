package tosa.impl;

import tosa.api.EntityCollection;
import tosa.api.IDBObject;
import tosa.loader.IDBType;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EntityCollectionImplBase<T extends IDBObject> implements EntityCollection<T> {

  protected IDBObject _owner;
  protected IDBType _fkType;
  protected QueryExecutor _queryExecutor;
  protected List<T> _cachedResults;

  protected EntityCollectionImplBase(IDBObject owner, IDBType fkType, QueryExecutor queryExecutor) {
    _owner = owner;
    _fkType = fkType;
    _queryExecutor = queryExecutor;
  }

  @Override
  public int size() {
    if (_cachedResults == null) {
      return issueCountQuery();
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
      throw new IndexOutOfBoundsException("Index " + index + " is invalid for an ReverseFkEntityCollectionImpl of size " + _cachedResults.size());
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

    addImpl(element);
  }

  @Override
  public void remove(T element) {
    if (!_fkType.isAssignableFrom(element.getIntrinsicType())) {
      throw new IllegalArgumentException("An element of type " + element.getIntrinsicType() + " cannot be added to a collection of type " + _fkType);
    }

    if (_owner.isNew()) {
      throw new IllegalStateException("Elements cannot be removed from an entity that has not yet been persisted");
    }

    removeImpl(element);
  }

  @Override
  public void load() {
    loadResultsIfNecessary();
  }

  @Override
  public void unload() {
    _cachedResults = null;
  }

  /**
   * This is an internal implementation method exposed only for the sake of testing
   * @param queryExecutor
   */
  public void setQueryExecutor(QueryExecutor queryExecutor) {
    _queryExecutor = queryExecutor;
  }

  // --------------- Abstract Protected Methods

  protected abstract int issueCountQuery();

  protected abstract List<T> loadResults();

  protected abstract void addImpl(T element);

  protected abstract void removeImpl(T element);

  // -------------- Private Methods

  private void loadResultsIfNecessary() {
    if (_cachedResults == null) {
      _cachedResults = loadResults();
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
