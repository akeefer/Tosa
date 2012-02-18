package tosa.impl

uses tosa.loader.IDBType
uses tosa.api.IDBObject
uses tosa.api.EntityCollection
uses java.util.Iterator
uses java.lang.UnsupportedOperationException
uses java.lang.IndexOutOfBoundsException
uses java.lang.IllegalArgumentException
uses java.lang.IllegalStateException

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/15/12
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class EntityCollectionImplBase<T extends IDBObject> implements EntityCollection<T> {

  protected var _owner : IDBObject
  protected var _fkType : IDBType
  protected var _queryExecutor : QueryExecutor
  protected var _cachedResults : List<T>

  protected construct(owner : IDBObject, fkType : IDBType, queryExecutor : QueryExecutor) {
    _owner = owner;
    _fkType = fkType;
    _queryExecutor = queryExecutor;
  }

  override function size() : int {
    if (_cachedResults == null) {
      return issueCountQuery();
    } else {
      return _cachedResults.size();
    }
  }

  override function iterator() : Iterator<T> {
    loadResultsIfNecessary();
    return new EntityCollectionImplIterator(_cachedResults.iterator());
  }

  override function get(index : int) : T {
    loadResultsIfNecessary();
    if (index < 0 || index > _cachedResults.size() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is invalid for an ReverseFkEntityCollectionImpl of size " + _cachedResults.size());
    }
    return _cachedResults.get(index);
  }

  override function add(element : T) {
    if (!_fkType.isAssignableFrom(element.getIntrinsicType())) {
      throw new IllegalArgumentException("An element of type " + element.getIntrinsicType() + " cannot be added to a collection of type " + _fkType);
    }

    if (_owner.isNew()) {
      throw new IllegalStateException("The element cannot be added to the list, as the owner is not yet committed.  You must commit the owner prior to added anything to the list.");
    }

    addImpl(element);
  }

  override function remove(element : T) {
    if (!_fkType.isAssignableFrom(element.getIntrinsicType())) {
      throw new IllegalArgumentException("An element of type " + element.getIntrinsicType() + " cannot be added to a collection of type " + _fkType);
    }

    if (_owner.isNew()) {
      throw new IllegalStateException("Elements cannot be removed from an entity that has not yet been persisted");
    }

    removeImpl(element);
  }

  override function load() {
    loadResultsIfNecessary();
  }

  override function unload() {
    _cachedResults = null;
  }

  /**
   * This is an internal implementation method exposed only for the sake of testing
   * @param queryExecutor
   */
  function setQueryExecutor(queryExecutor : QueryExecutor) {
    _queryExecutor = queryExecutor;
  }

  // --------------- Abstract Protected Methods

  protected abstract function issueCountQuery() : int

  protected abstract function loadResults() : List<T>;

  protected abstract function addImpl(element : T);

  protected abstract function removeImpl(element : T);

  // -------------- Private Methods

  private function loadResultsIfNecessary() {
    if (_cachedResults == null) {
      _cachedResults = loadResults()
    }
  }

  private class EntityCollectionImplIterator implements Iterator<T> {
    delegate _wrappedIterator represents Iterator<T>

    private construct(wrappedIterator : Iterator<T>) {
      _wrappedIterator = wrappedIterator;
    }

    // AHK - This shouldn't be necessary, but there's a Gosu bug that appears to prevent it from being
    // delegated properly.  Note that hasNext() is delegated just fine
    override function next() : T {
      return _wrappedIterator.next()
    }

    override function remove() {
      throw new UnsupportedOperationException();
    }
  }

}