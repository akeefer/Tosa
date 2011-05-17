package tosa.impl;

import gw.lang.reflect.TypeSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.java2d.pipe.SpanShapeRenderer;
import test.TestEnv;
import tosa.CachedDBObject;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.api.IPreparedStatementParameter;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.DBTypeLoader;
import tosa.loader.IDBType;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoinArrayEntityCollectionImplTest {

  @BeforeClass
  static public void resetDB() {
    TestEnv.maybeInit();
    getDB().getDBUpgrader().recreateTables();
  }

  private static DatabaseImpl getDB() {
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    return dbTypeLoader.getTypeDataForNamespace("test.testdb");
  }

  private IDBObject createAndCommitFoo() {
    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
    update(bar);
    return bar;
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
    try {
      obj.update();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private JoinArrayEntityCollectionImpl createList(IDBObject foo) {
    return createList(foo, new QueryExecutorImpl(foo.getDBTable().getDatabase()));
  }

  private JoinArrayEntityCollectionImpl createList(IDBObject foo, QueryExecutor queryExecutor) {
    IDBType bazType = (IDBType) TypeSystem.getByFullName("test.testdb.Baz");
    IDBColumn srcColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Foo_id");
    IDBColumn targetColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Baz_id");
    return new JoinArrayEntityCollectionImpl(foo, bazType, srcColumn, targetColumn, queryExecutor);
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
    QueryExecutorSpy spy = new QueryExecutorSpy();
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
    QueryExecutorSpy spy = new QueryExecutorSpy();
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

  // -------------------------------- Tests for Add

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
  public void testAddSetsArrayBackPointerIfEntityIsAlreadyInThisCollection() {
    IDBObject foo = createAndCommitFoo();
    IDBObject baz = createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(1, list.size());
    assertEquals(1, countMatchesInDB(foo, baz));
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

//  @Test
//  public void testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    IDBObject foo2 = createFoo();
//    update(foo2);
//    IDBObject foo3 = createAndCommitFoo(bar);
//
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(2, list.size());
//    list.add(foo2);
//    assertEquals(3, list.size());
//    assertEquals(foo1.getColumnValue("id"), list.get(0).getColumnValue("id"));
//    assertEquals(foo2.getColumnValue("id"), list.get(1).getColumnValue("id"));
//    assertEquals(foo3.getColumnValue("id"), list.get(2).getColumnValue("id"));
//    // TODO - AHK - Test that it's not the same object?
//  }
//
//  @Test
//  public void testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    IDBObject foo2 = createFoo();
//    update(foo2);
//    IDBObject foo3 = createAndCommitFoo(bar);
//
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(2, list.size());
//    list.load();
//    list.add(foo2);
//    assertEquals(3, list.size());
//    assertEquals(foo1.getColumnValue("id"), list.get(0).getColumnValue("id"));
//    assertEquals(foo3.getColumnValue("id"), list.get(1).getColumnValue("id"));
//    assertEquals(foo2.getColumnValue("id"), list.get(2).getColumnValue("id"));
//    assertSame(foo2, list.get(2));
//  }
//
//  @Test
//  public void testUnloadWillUnloadCachedData() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.load();
//    assertEquals(2, list.size());
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    assertEquals(2, list.size());
//    list.unload();
//    assertEquals(4, list.size());
//    list.load();
//    assertEquals(4, list.size());
//  }
//
//  // ----------------------- Tests for remove()
//
//  @Test
//  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsNotAMemberOfThisArray() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    try {
//      createList(bar2).remove(foo);
//      fail("Expected an IllegalArgumentException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillThrowIllegalStateExceptionIfOwnerIsNew() {
//    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Bar"), true);
//    IDBObject foo = createFoo();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.remove(foo);
//      fail("Expected add to throw an IllegalStateException");
//    } catch (IllegalStateException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsOfWrongType() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.remove(bar2);
//      fail("Expected remove to throw an IllegalStateException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillImmediatelyUpdateDatabaseIfArrayNotLoadedYet() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.unload();
//    assertEquals(1, countMatchesInDB(foo, bar));
//    list.remove(foo);
//    assertEquals(0, countMatchesInDB(foo, bar));
//  }
//
//  @Test
//  public void testRemoveWillImmediatelyUpdateDatabaseAndRemoveFromCachedResultsIfArrayLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.load();
//    assertEquals(1, countMatchesInDB(foo, bar));
//    list.remove(foo);
//    assertEquals(0, countMatchesInDB(foo, bar));
//    assertEquals(0, list.size());
//    assertFalse(list.iterator().hasNext());
//  }
//
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

  private static class QueryExecutorSpy implements QueryExecutor {

    private QueryExecutorImpl _delegate;
    private String _count;
    private String _select;
    private String _update;

    private QueryExecutorSpy() {
      _delegate = new QueryExecutorImpl(getDB());
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
      update(profilerTag, sqlStatement, parameters);
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

    public boolean anyCalled() {
      return countCalled() || selectCalled() || updateCalled();
    }

    public void reset() {
      _count = null;
      _select = null;
      _update = null;
    }
  }
}
