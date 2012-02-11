package tosa.impl;

import gw.lang.reflect.TypeSystem;
import org.junit.Ignore;
import org.junit.Test;
import tosa.CachedDBObject;
import tosa.TosaDBTestBase;
import tosa.api.DBLocator;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.IDBType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class JoinArrayEntityCollectionImplTest extends TosaDBTestBase {

  private IDBObject createAndCommitFoo() {
    IDBObject foo = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
    update(foo);
    return foo;
  }

  private IDBObject createAndCommitBaz(IDBObject foo) {
    IDBObject baz = createBaz();
    update(baz);
    IDBObject join = createJoin();
    join.setColumnValue("Foo_id", foo.getId());
    join.setColumnValue("Baz_id", baz.getId());
    update(join);
    return baz;
  }

  private IDBObject createBaz() {
    return new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Baz"), true);
  }

  private IDBObject createJoin() {
    return new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.join_Foo_Baz"), true);
  }

  private void update(IDBObject obj) {
    obj.update();
  }

  private JoinArrayEntityCollectionImpl<IDBObject> createList(IDBObject foo) {
    return createList(foo, new QueryExecutorImpl(foo.getDBTable().getDatabase()));
  }

  private JoinArrayEntityCollectionImpl<IDBObject> createList(IDBObject foo, QueryExecutor queryExecutor) {
    IDBType bazType = (IDBType) TypeSystem.getByFullName("test.testdb.Baz");
    IDBColumn srcColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Foo_id");
    IDBColumn targetColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Baz_id");
    return new JoinArrayEntityCollectionImpl<IDBObject>(foo, bazType, srcColumn, targetColumn, queryExecutor);
  }

   // ---------------------- Tests for size() -------------------------------------

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(0, list.size());
  }

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasNotBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(0, list.size());
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyLoadedArray() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(2, list.size());
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyNonLoadedArray() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(2, list.size());
  }

  @Test
  public void testSizeReturnsOriginalSizeForLoadedArrayAfterChangesInDB() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(2, list.size());
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    assertEquals(2, list.size());
  }

  @Test
  public void testSizeReturnsCurrentSizeForNonLoadedArrayAfterChangesInDB() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(2, list.size());
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    assertEquals(4, list.size());
  }

  @Test
  public void testSizeIssuesCountStarQueryIfArrayHasNotBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    QueryExecutorSpy spy = new QueryExecutorSpy(getDB());
    JoinArrayEntityCollectionImpl list = createList(foo, spy);
    assertEquals(0, list.size());
    assertTrue(spy.countCalled());
    assertFalse(spy.selectCalled());
    assertFalse(spy.updateCalled());
  }

  @Test
  public void testSizeDoesNotIssueQueriesIfArrayHasBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    QueryExecutorSpy spy = new QueryExecutorSpy(getDB());
    JoinArrayEntityCollectionImpl list = createList(foo, spy);
    list.load();
    spy.reset();
    assertEquals(2, list.size());
    assertFalse(spy.anyCalled());
  }

  // ----------------------------- Tests for get(int)

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsNegative() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    try {
      list.get(-1);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsGreaterThanSizeOfCollection() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    try {
      list.get(2);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsEqualToSizeOfCollection() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    try {
      list.get(1);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetReturnsAppropriateElementIfArgumentIsValid() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    IDBObject bazFromDB = list.get(0);
    assertEquals(baz.getId(), bazFromDB.getId());
  }

  // ------------------------------- Tests for iterator()

  @Test
  public void testIteratorHasNextReturnsFalseForEmptyList() {
    IDBObject foo = createAndCommitFoo();
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertFalse(list.iterator().hasNext());
  }

  @Test
  public void testIteratorHasNextReturnsTrueAtStartOfNonEmptyList() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertTrue(list.iterator().hasNext());
  }

  @Test
  public void testIteratorNextReturnsItemsInIdOrder() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz1 = createAndCommitBaz(foo);
    IDBObject baz2 = createAndCommitBaz(foo);
    IDBObject baz3 = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    Iterator<IDBObject> it = list.iterator();
    assertTrue(it.hasNext());
    assertEquals(baz1.getId(), it.next().getId());
    assertTrue(it.hasNext());
    assertEquals(baz2.getId(), it.next().getId());
    assertTrue(it.hasNext());
    assertEquals(baz3.getId(), it.next().getId());
    assertFalse(it.hasNext());
  }

  @Test
  public void testIteratorHasNextReturnsFalseAtEndOfList() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    Iterator<IDBObject> it = list.iterator();
    assertTrue(it.hasNext());
    it.next();
    assertFalse(it.hasNext());
  }

  @Test
  public void testIteratorRemoveThrowsUnsupportedOperationException() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    Iterator<IDBObject> it = list.iterator();
    it.next();
    try {
      it.remove();
      fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  // TODO - AHK - Test for concurrent modification exceptions?

  // -------------------------------- Tests for add(T)

  @Test
  public void testAddThrowsIllegalStateExceptionIfOwningEntityHasNotYetBeenCommitted() {
    IDBObject foo = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
    IDBObject baz = createBaz();
    JoinArrayEntityCollectionImpl list = createList(foo);
    try {
      list.add(baz);
      fail("Expected add to throw an IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void testAddSetsArrayBackPointerAndArrayPointerIfEntityIsAlreadyInThisCollectionAndCollectionHasBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(1, list.size());
    assertEquals(1, countMatchesInDB(foo, baz));
    list.load();
    list.add(baz);
    assertEquals(1, list.size());
    // TODO - AHK - Check that foo is in baz.Foos
    assertEquals(1, countMatchesInDB(foo, baz));
    assertSame(baz, list.get(0));
  }

  @Test
  public void testAddSetsArrayBackPointerIfEntityIsAlreadyInThisCollectionAndCollectionHasNotBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(1, list.size());
    assertEquals(1, countMatchesInDB(foo, baz));
    list.unload(); // Sanity check - should be unnecessary, but just in case
    list.add(baz);
    assertEquals(1, list.size());
    // TODO - AHK - Check that foo is in baz.Foos
    assertEquals(1, countMatchesInDB(foo, baz));
  }

//  @Test
//  public void testAddThrowsIllegalArgumentExceptionIfEntityIsAlreadyInAnotherCollection() {
//    IDBObject bar1 = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    IDBObject foo = createFoo();
//    setBarId(bar2, foo);
//    update(foo);
//
//    ReverseFkEntityCollectionImpl list = createList(bar1);
//    try {
//      list.add(foo);
//      fail("Expected add to throw an IllegalArgumentException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }

  @Test
  public void testAddThrowsIllegalArgumentExceptionIfEntityIsOfWrongType() {
    IDBObject foo1 = createAndCommitFoo();
    IDBObject foo2 = createAndCommitFoo();

    JoinArrayEntityCollectionImpl list = createList(foo1);
    try {
      list.add(foo2);
      fail("Expected add to throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testAddInsertsNewObjectInDatabaseAndArrayBackPointerIfObjectIsNotYetCommitted() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createBaz();
    assertNull(baz.getId());
    assertTrue(baz.isNew());
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.add(baz);
    assertNotNull(baz.getId());
    assertFalse(baz.isNew());
    // TODO - AHK - Test that baz.Foos contains Foo
    assertEquals(1, countMatchesInDB(foo, baz));
  }

  @Test
  public void testAddInsertsJoinRowInDatabaseAndSetsArrayBackPointerIfObjectHasAlreadyBeenPersisted() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createBaz();
    update(baz);
    assertEquals(0, countMatchesInDB(foo, baz));
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.add(baz);
    // TODO - AHK - Test that baz.Foos contains Foo
    assertEquals(1, countMatchesInDB(foo, baz));
  }

  @Test
  public void testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz1 = createAndCommitBaz(foo);
    IDBObject baz2 = createBaz();
    update(baz2);
    IDBObject baz3 = createAndCommitBaz(foo);

    JoinArrayEntityCollectionImpl<IDBObject> list = createList(foo);
    assertEquals(2, list.size());
    list.add(baz2);
    assertEquals(3, list.size());
    Set<Long> ids = new HashSet<Long>();
    for (IDBObject elem : list) {
      ids.add(elem.getId());
    }
    assertTrue(ids.contains(baz1.getId()));
    assertTrue(ids.contains(baz2.getId()));
    assertTrue(ids.contains(baz3.getId()));
    // TODO - AHK - Test that it's not the same object?
  }

  @Test
  public void testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz1 = createAndCommitBaz(foo);
    IDBObject baz2 = createBaz();
    update(baz2);
    IDBObject foo3 = createAndCommitBaz(foo);

    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(2, list.size());
    list.load();
    list.add(baz2);
    assertEquals(3, list.size());
    assertEquals(baz1.getId(), list.get(0).getId());
    assertEquals(foo3.getId(), list.get(1).getId());
    assertEquals(baz2.getId(), list.get(2).getId());
    assertSame(baz2, list.get(2));
  }

  // ---------------------------- Tests for unload()

  @Test
  public void testUnloadWillUnloadCachedData() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(2, list.size());
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    assertEquals(2, list.size());
    list.unload();
    assertEquals(4, list.size());
    list.load();
    assertEquals(4, list.size());
  }

  // ----------------------- Tests for remove()

  @Test
  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsNotAMemberOfThisArray() {
    IDBObject foo = createAndCommitFoo();
    IDBObject foo2 = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    try {
      createList(foo2).remove(baz);
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testRemoveWillThrowIllegalStateExceptionIfOwnerIsNew() {
    IDBObject foo = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
    IDBObject baz = createBaz();
    try {
      createList(foo).remove(baz);
      fail("Expected add to throw an IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsOfWrongType() {
    IDBObject foo = createAndCommitFoo();
    IDBObject foo2 = createAndCommitFoo();
    try {
      createList(foo).remove(foo2);
      fail("Expected remove to throw an IllegalStateException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testRemoveWillImmediatelyUpdateDatabaseIfArrayNotLoadedYet() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.unload();
    assertEquals(1, countMatchesInDB(foo, baz));
    list.remove(baz);
    assertEquals(0, countMatchesInDB(foo, baz));
  }

  @Test
  public void testRemoveWillImmediatelyUpdateDatabaseAndRemoveFromCachedResultsIfArrayLoaded() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(1, countMatchesInDB(foo, baz));
    list.remove(baz);
    assertEquals(0, countMatchesInDB(foo, baz));
    assertEquals(0, list.size());
    assertFalse(list.iterator().hasNext());
  }

//  @Test
//  public void testRemoveWillNullOutFkColumnOnElement() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(bar.getId(), foo.getColumnValue("Bar_id"));
//    list.remove(foo);
//    assertNull(foo.getColumnValue("Bar_id"));
//  }
//
//  @Test
//  public void testRemoveWillNullOutCachedFkObjectOnElement() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertNotNull(foo.getFkValue("Bar_id"));
//    list.remove(foo);
//    assertNull(foo.getFkValue("Bar_id"));
//  }

  // ----------------------------- Helper Methods/Classes

  private int countMatchesInDB(IDBObject foo, IDBObject baz) {
    IDBTable joinTable = getDB().getTable("join_Foo_Baz");
    String sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM ${joinTable} WHERE ${fooColumn} = ${fooId} AND ${bazColumn} = ${bazId}",
        "joinTable", joinTable,
        "fooColumn", joinTable.getColumn("Foo_id"),
        "fooId", foo.getId(),
        "bazColumn", joinTable.getColumn("Baz_id"),
        "bazId", baz.getId());
    return new QueryExecutorImpl(getDB()).count("", sql);
  }

}
