package tosa.impl;

import gw.lang.reflect.TypeSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import test.TestEnv;
import tosa.CachedDBObject;
import tosa.api.IDBObject;
import tosa.api.IPreparedStatementParameter;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.DBTypeInfo;
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
public class EntityCollectionImplTest {

  @BeforeClass
  static public void resetDB() {
    TestEnv.maybeInit();
    getDB().getDBUpgrader().recreateTables();
  }

  private static DatabaseImpl getDB() {
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    return dbTypeLoader.getTypeDataForNamespace("test.testdb");
  }

  private IDBObject createAndCommitBar() {
    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Bar"), true);
    update(bar);
    return bar;
  }

  private IDBObject createAndCommitFoo(IDBObject bar) {
    IDBObject foo = createFoo();
    setBarId(bar, foo);
    update(foo);
    return foo;
  }

  private void setBarId(IDBObject bar, IDBObject foo) {
    foo.setColumnValue("Bar_id", bar.getColumnValue(DBTypeInfo.ID_COLUMN));
  }

  private IDBObject createFoo() {
    return new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
  }

  private void update(IDBObject obj) {
    try {
      obj.update();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private EntityCollectionImpl createList(IDBObject bar) {
    return createList(bar, new SimpleQueryExecutorImpl());
  }

  private EntityCollectionImpl createList(IDBObject bar, SimpleQueryExecutor queryExecutor) {
    IDBType fooType = (IDBType) TypeSystem.getByFullName("test.testdb.Foo");
    EntityCollectionImpl foos = new EntityCollectionImpl(bar, fooType, fooType.getTable().getColumn("Bar_id"), queryExecutor);
    return foos;
  }

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasBeenLoaded() {
    // TODO
  }

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasNotBeenLoaded() {
    IDBObject bar = createAndCommitBar();
    EntityCollectionImpl list = createList(bar);
    assertEquals(0, list.size());
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyLoadedArray() {
   // TODO
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyNonLoadedArray() {
    IDBObject bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    assertEquals(2, list.size());
  }

  @Test
  public void testSizeReturnsOriginalSizeForLoadedArrayAfterChangesInDB() {
    // TODO
  }

  @Test
  public void testSizeReturnsCurrentSizeForNonLoadedArrayAfterChangesInDB() {
    IDBObject bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    assertEquals(2, list.size());
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(4, list.size());
  }

  @Test
  public void testSizeIssuesCountStarQueryIfArrayHasNotBeenLoaded() {
    IDBObject bar = createAndCommitBar();
    EntityCollectionImpl list = createList(bar, new SimpleQueryExecutor() {
      @Override
      public int countWhere(String profilerTag, IDBType targetType, String whereClause, IPreparedStatementParameter... parameters) {
        return 42;
      }

      @Override
      public int count(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<IDBObject> find(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void update(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
        throw new UnsupportedOperationException();
      }
    });
    assertEquals(42, list.size());
  }

  @Test
  public void testSizeDoesNotIssueQueriesIfArrayHasBeenLoaded() {
    // TODO
  }

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsNegative() {
    IDBObject bar = createAndCommitBar();
    createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    try {
      list.get(-1);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsGreaterThanSizeOfCollection() {
    IDBObject bar = createAndCommitBar();
    createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    try {
      list.get(2);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsEqualToSizeOfCollection() {
    IDBObject bar = createAndCommitBar();
    createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    try {
      list.get(1);
      fail("Expected an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // Expected
    }
  }

  @Test
  public void testGetReturnsAppropriateElementIfArgumentIsValid() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    IDBObject fooFromDB = list.get(0);
    assertEquals(foo.getColumnValue("id"), fooFromDB.getColumnValue("id"));
  }

  @Test
  public void testIteratorHasNextReturnsFalseForEmptyList() {
    IDBObject bar = createAndCommitBar();
    EntityCollectionImpl list = createList(bar);
    assertFalse(list.iterator().hasNext());
  }

  @Test
  public void testIteratorHasNextReturnsTrueAtStartOfNonEmptyList() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    assertTrue(list.iterator().hasNext());
  }

  @Test
  public void testIteratorNextReturnsItemsInIdOrder() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    IDBObject foo2 = createAndCommitFoo(bar);
    IDBObject foo3 = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    Iterator<IDBObject> it = list.iterator();
    assertTrue(it.hasNext());
    assertEquals(foo1.getColumnValue("id"), it.next().getColumnValue("id"));
    assertTrue(it.hasNext());
    assertEquals(foo2.getColumnValue("id"), it.next().getColumnValue("id"));
    assertTrue(it.hasNext());
    assertEquals(foo3.getColumnValue("id"), it.next().getColumnValue("id"));
    assertFalse(it.hasNext());
  }

  @Test
  public void testIteratorHasNextReturnsFalseAtEndOfList() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    Iterator<IDBObject> it = list.iterator();
    assertTrue(it.hasNext());
    it.next();
    assertFalse(it.hasNext());
  }

  @Test
  public void testIteratorRemoveThrowsUnsupportedOperationException() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
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

  @Test
  public void testAddThrowsIllegalStateExceptionIfOwningEntityHasNotYetBeenCommitted() {
    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Bar"), true);
    IDBObject foo = createFoo();
    EntityCollectionImpl list = createList(bar);
    try {
      list.add(foo);
      fail("Expected add to throw an IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void testAddIsANoOpIfEntityIsAlreadyInThisCollection() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo = createAndCommitFoo(bar);
    EntityCollectionImpl list = createList(bar);
    assertEquals(1, list.size());
    list.add(foo);
    assertEquals(1, list.size());
  }

  @Test
  public void testAddThrowsIllegalArgumentExceptionIfEntityIsAlreadyInAnotherCollection() {
    IDBObject bar1 = createAndCommitBar();
    IDBObject bar2 = createAndCommitBar();
    IDBObject foo = createFoo();
    setBarId(bar2, foo);
    update(foo);

    EntityCollectionImpl list = createList(bar1);
    try {
      list.add(foo);
      fail("Expected add to throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testAddThrowsIllegalArgumentExceptionIfEntityIsOfWrongType() {
    IDBObject bar1 = createAndCommitBar();
    IDBObject bar2 = createAndCommitBar();

    EntityCollectionImpl list = createList(bar1);
    try {
      list.add(bar2);
      fail("Expected add to throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testAddInsertsNewObjectInDatabaseIfObjectIsNotYetCommitted() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo = createFoo();
    assertNull(foo.getColumnValue("id"));
    assertTrue(foo.isNew());
    EntityCollectionImpl list = createList(bar);
    list.add(foo);
    assertNotNull(foo.getColumnValue("id"));
    assertFalse(foo.isNew());
    // TODO - AHK - Actually query for it in the DB?
  }

  @Test
  public void testAddUpdatesFkInDatabaseIfObjectHasAlreadyBeenPersisted() {
    // TODO - AHK
  }

  @Test
  public void testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    IDBObject foo2 = createFoo();
    update(foo2);
    IDBObject foo3 = createAndCommitFoo(bar);

    EntityCollectionImpl list = createList(bar);
    assertEquals(2, list.size());
    // TODO - AHK - Verify that the results haven't been loaded somehow
    list.add(foo2);
    assertEquals(3, list.size());
    assertEquals(foo1.getColumnValue("id"), list.get(0));
    assertEquals(foo3.getColumnValue("id"), list.get(1));
    assertEquals(foo2.getColumnValue("id"), list.get(2));
  }

  @Test
  public void testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
    IDBObject bar = createAndCommitBar();
    IDBObject foo1 = createAndCommitFoo(bar);
    IDBObject foo2 = createFoo();
    update(foo2);
    IDBObject foo3 = createAndCommitFoo(bar);

    EntityCollectionImpl list = createList(bar);
    assertEquals(2, list.size());
    // TODO - AHK - For the load of the cached result set
    list.add(foo2);
    assertEquals(3, list.size());
    assertEquals(foo1.getColumnValue("id"), list.get(0));
    assertEquals(foo3.getColumnValue("id"), list.get(1));
    assertEquals(foo2.getColumnValue("id"), list.get(2));
  }

  @Test
  public void testAddSetsFkBackPointerOnAddedObject() {
   // TODO - AHK - This isn't really exposed to Java-land right now . . .
  }
}
