package tosa.impl

uses tosa.TosaDBTestBase
uses org.junit.Test
uses test.testdb.Foo
uses test.testdb.Baz
uses test.testdb.join_Foo_Baz
uses java.lang.IndexOutOfBoundsException
uses java.lang.UnsupportedOperationException
uses java.lang.IllegalStateException
uses tosa.api.EntityCollection
uses java.lang.IllegalArgumentException
uses java.util.HashSet
uses java.lang.Long
uses tosa.impl.query.SqlStringSubstituter

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
class JoinArrayEntityCollectionImplTest extends TosaDBTestBase {
  
  private function createAndCommitFoo() : Foo {
    var foo = new Foo()
    foo.update()
    return foo
  }

  private function createAndCommitBaz(foo : Foo) : Baz {
    var baz = new Baz()
    baz.update()
    
    // Explicitly create a join object, since we don't want to use the entity collection
    // to do that, since it's what we're testing
    var join = new join_Foo_Baz()
    join.Foo = foo
    join.Baz = baz
    join.update()

    return baz
  }

  private function insertSpy(foo : Foo) : QueryExecutorSpy {
    var spy = new QueryExecutorSpy(getDB())
    (foo.Bazs as JoinArrayEntityCollectionImpl).setQueryExecutor(spy)
    return spy
  }

  @Test
  function sanityCheck() {
    // These tests assume Foo.Bazs is a JoinArrayEntityCollectionImpl
    assertTrue(createAndCommitFoo().Bazs typeis JoinArrayEntityCollectionImpl)
  }

//   // ---------------------- Tests for size() -------------------------------------
//
  @Test
  function testSizeReturnsZeroIfArrayIsEmptyAndHasBeenLoaded() {
    var foo = createAndCommitFoo()
    var list = foo.Bazs
    list.load()
    assertEquals(0, list.size())
  }

  @Test
  function testSizeReturnsZeroIfArrayIsEmptyAndHasNotBeenLoaded() {
    var foo = createAndCommitFoo()
    assertEquals(0, foo.Bazs.size())
  }

  @Test
  function testSizeReturnsCorrectSizeForNonEmptyLoadedArray() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    var list = foo.Bazs
    list.load()
    assertEquals(2, list.size())
  }

  @Test
  function testSizeReturnsCorrectSizeForNonEmptyNonLoadedArray() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    assertEquals(2, foo.Bazs.size())
  }

  @Test
  function testSizeReturnsOriginalSizeForLoadedArrayAfterChangesInDB() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    var list = foo.Bazs
    list.load()
    assertEquals(2, list.size())
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    assertEquals(2, list.size())
  }

  @Test
  function testSizeReturnsCurrentSizeForNonLoadedArrayAfterChangesInDB() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    var list = foo.Bazs
    assertEquals(2, list.size())
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    assertEquals(4, list.size())
  }

  @Test
  function testSizeIssuesCountStarQueryIfArrayHasNotBeenLoaded() {
    var foo = createAndCommitFoo()
    var spy = insertSpy(foo)
    assertEquals(0, foo.Bazs.size())
    assertTrue(spy.countCalled())
    assertFalse(spy.selectCalled())
    assertFalse(spy.updateCalled())
  }

  @Test
  function testSizeDoesNotIssueQueriesIfArrayHasBeenLoaded() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    var spy = insertSpy(foo)
    foo.Bazs.load()
    spy.reset()
    assertEquals(2, foo.Bazs.size())
    assertFalse(spy.anyCalled())
  }

  // ----------------------------- Tests for get(int)

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsNegative() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    assertException(\ -> foo.Bazs.get(-1), IndexOutOfBoundsException)
  }

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsGreaterThanSizeOfCollection() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    assertException(\ -> foo.Bazs.get(2), IndexOutOfBoundsException)
  }

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsEqualToSizeOfCollection() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    assertException(\ -> foo.Bazs.get(1), IndexOutOfBoundsException)
  }

  @Test
  function testGetReturnsAppropriateElementIfArgumentIsValid() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    var bazFromDB = foo.Bazs.get(0)
    assertEquals(baz.Id, bazFromDB.Id)
  }

  // ------------------------------- Tests for iterator()

  @Test
  function testIteratorHasNextReturnsFalseForEmptyList() {
    var foo = createAndCommitFoo()
    assertFalse(foo.Bazs.iterator().hasNext())
  }

  @Test
  function testIteratorHasNextReturnsTrueAtStartOfNonEmptyList() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    assertTrue(foo.Bazs.iterator().hasNext())
  }

  @Test
  function testIteratorNextReturnsItemsInIdOrder() {
    var foo = createAndCommitFoo()
    var baz1 = createAndCommitBaz(foo)
    var baz2 = createAndCommitBaz(foo)
    var baz3 = createAndCommitBaz(foo)
    var it = foo.Bazs.iterator()
    assertTrue(it.hasNext())
    assertEquals(baz1.getId(), it.next().getId())
    assertTrue(it.hasNext())
    assertEquals(baz2.getId(), it.next().getId())
    assertTrue(it.hasNext())
    assertEquals(baz3.getId(), it.next().getId())
    assertFalse(it.hasNext())
  }

  @Test
  function testIteratorHasNextReturnsFalseAtEndOfList() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    var it = foo.Bazs.iterator()
    assertTrue(it.hasNext())
    it.next()
    assertFalse(it.hasNext())
  }

  @Test
  function testIteratorRemoveThrowsUnsupportedOperationException() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    var it = foo.Bazs.iterator()
    it.next()
    assertException(\ -> it.remove(), UnsupportedOperationException)
  }

  // TODO - AHK - Test for concurrent modification exceptions?

  // -------------------------------- Tests for add(T)

  @Test
  function testAddThrowsIllegalStateExceptionIfOwningEntityHasNotYetBeenCommitted() {
    var foo = new Foo()
    var baz = new Baz()
    assertException(\ -> foo.Bazs.add(baz), IllegalStateException)
  }

  @Test
  function testAddSetsArrayBackPointerAndArrayPointerIfEntityIsAlreadyInThisCollectionAndCollectionHasBeenLoaded() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    assertEquals(1, foo.Bazs.size())
    assertEquals(1, countMatchesInDB(foo, baz))
    foo.Bazs.load()
    foo.Bazs.add(baz)
    assertEquals(1, foo.Bazs.size())
    // TODO - AHK - Check that foo is in baz.Foos
    assertEquals(1, countMatchesInDB(foo, baz))
    assertSame(baz, foo.Bazs.get(0))
  }

  @Test
  function testAddSetsArrayBackPointerIfEntityIsAlreadyInThisCollectionAndCollectionHasNotBeenLoaded() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    assertEquals(1, foo.Bazs.size())
    assertEquals(1, countMatchesInDB(foo, baz))
    foo.Bazs.unload() // Sanity check - should be unnecessary, but just in case
    foo.Bazs.add(baz)
    assertEquals(1, foo.Bazs.size())
    // TODO - AHK - Check that foo is in baz.Foos
    assertEquals(1, countMatchesInDB(foo, baz))
  }

//  @Test
//  function testAddThrowsIllegalArgumentExceptionIfEntityIsAlreadyInAnotherCollection() {
//    IDBObject bar1 = createAndCommitBar()
//    IDBObject bar2 = createAndCommitBar()
//    IDBObject foo = createFoo()
//    setBarId(bar2, foo)
//    update(foo)
//
//    ReverseFkEntityCollectionImpl list = createList(bar1)
//    try {
//      list.add(foo)
//      fail("Expected add to throw an IllegalArgumentException")
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }

  @Test
  function testAddThrowsIllegalArgumentExceptionIfEntityIsOfWrongType() {
    var foo1 = createAndCommitFoo()
    var foo2 = createAndCommitFoo()
    var list : EntityCollection = foo1.Bazs
    assertException(\ -> list.add(foo2), IllegalArgumentException)
  }

  @Test
  function testAddInsertsNewObjectInDatabaseAndArrayBackPointerIfObjectIsNotYetCommitted() {
    var foo = createAndCommitFoo()
    var baz = new Baz()
    assertNull(baz.getId())
    assertTrue(baz.isNew())
    foo.Bazs.add(baz)
    assertNotNull(baz.getId())
    assertFalse(baz.isNew())
    // TODO - AHK - Test that baz.Foos contains Foo
    assertEquals(1, countMatchesInDB(foo, baz))
  }

  @Test
  function testAddInsertsJoinRowInDatabaseAndSetsArrayBackPointerIfObjectHasAlreadyBeenPersisted() {
    var foo = createAndCommitFoo()
    var baz = new Baz()
    baz.update()
    assertEquals(0, countMatchesInDB(foo, baz))
    foo.Bazs.add(baz)
    // TODO - AHK - Test that baz.Foos contains Foo
    assertEquals(1, countMatchesInDB(foo, baz))
  }

  @Test
  function testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
    var foo = createAndCommitFoo()
    var baz1 = createAndCommitBaz(foo)
    var baz2 = new Baz()
    baz2.update()
    var baz3 = createAndCommitBaz(foo)

    assertEquals(2, foo.Bazs.size())
    foo.Bazs.add(baz2)
    assertEquals(3, foo.Bazs.size())
    var ids = new HashSet<Long>()
    for (elem in foo.Bazs) {
      ids.add(elem.getId())
    }
    assertTrue(ids.contains(baz1.getId()))
    assertTrue(ids.contains(baz2.getId()))
    assertTrue(ids.contains(baz3.getId()))
    // TODO - AHK - Test that it's not the same object?
  }

  @Test
  function testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
    var foo = createAndCommitFoo()
    var baz1 = createAndCommitBaz(foo)
    var baz2 = new Baz()
    baz2.update()
    var baz3 = createAndCommitBaz(foo)

    assertEquals(2, foo.Bazs.size())
    foo.Bazs.load()
    foo.Bazs.add(baz2)
    assertEquals(3, foo.Bazs.size())
    assertEquals(baz1.getId(), foo.Bazs.get(0).getId())
    assertEquals(baz3.getId(), foo.Bazs.get(1).getId())
    assertEquals(baz2.getId(), foo.Bazs.get(2).getId())
    assertSame(baz2, foo.Bazs.get(2))
  }

  // ---------------------------- Tests for unload()

  @Test
  function testUnloadWillUnloadCachedData() {
    var foo = createAndCommitFoo()
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    foo.Bazs.load()
    assertEquals(2, foo.Bazs.size())
    createAndCommitBaz(foo)
    createAndCommitBaz(foo)
    assertEquals(2, foo.Bazs.size())
    foo.Bazs.unload()
    assertEquals(4, foo.Bazs.size())
    foo.Bazs.load()
    assertEquals(4, foo.Bazs.size())
  }

  // ----------------------- Tests for remove()

  @Test
  function testRemoveWillThrowIllegalArgumentExceptionIfElementIsNotAMemberOfThisArray() {
    var foo = createAndCommitFoo()
    var foo2 = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    assertException(\ -> foo2.Bazs.remove(baz), IllegalArgumentException)
  }

  @Test
  function testRemoveWillThrowIllegalStateExceptionIfOwnerIsNew() {
    var foo = new Foo()
    var baz = new Baz()
    assertException(\ -> foo.Bazs.remove(baz), IllegalStateException)
  }

  @Test
  function testRemoveWillThrowIllegalArgumentExceptionIfElementIsOfWrongType() {
    var foo = createAndCommitFoo()
    var foo2 = createAndCommitFoo()
    var list : EntityCollection = foo.Bazs
    assertException(\ -> list.remove(foo2), IllegalArgumentException)
  }

  @Test
  function testRemoveWillImmediatelyUpdateDatabaseIfArrayNotLoadedYet() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    foo.Bazs.unload()
    assertEquals(1, countMatchesInDB(foo, baz))
    foo.Bazs.remove(baz)
    assertEquals(0, countMatchesInDB(foo, baz))
  }

  @Test
  function testRemoveWillImmediatelyUpdateDatabaseAndRemoveFromCachedResultsIfArrayLoaded() {
    var foo = createAndCommitFoo()
    var baz = createAndCommitBaz(foo)
    foo.Bazs.load()
    assertEquals(1, countMatchesInDB(foo, baz))
    foo.Bazs.remove(baz)
    assertEquals(0, countMatchesInDB(foo, baz))
    assertEquals(0, foo.Bazs.size())
    assertFalse(foo.Bazs.iterator().hasNext())
  }

//  @Test
//  function testRemoveWillNullOutFkColumnOnElement() {
//    IDBObject bar = createAndCommitBar()
//    IDBObject foo = createAndCommitFoo(bar)
//    ReverseFkEntityCollectionImpl list = createList(bar)
//    assertEquals(bar.getId(), foo.getColumnValue("Bar_id"))
//    list.remove(foo)
//    assertNull(foo.getColumnValue("Bar_id"))
//  }
//
//  @Test
//  function testRemoveWillNullOutCachedFkObjectOnElement() {
//    IDBObject bar = createAndCommitBar()
//    IDBObject foo = createAndCommitFoo(bar)
//    ReverseFkEntityCollectionImpl list = createList(bar)
//    assertNotNull(foo.getFkValue("Bar_id"))
//    list.remove(foo)
//    assertNull(foo.getFkValue("Bar_id"))
//  }

//  // ----------------------------- Helper Methods/Classes
//
  private function countMatchesInDB(foo : Foo, baz : Baz) : int {
    var joinTable = getDB().getTable("join_Foo_Baz")
    var sql = SqlStringSubstituter.substitute("SELECT count(*) as count FROM :joinTable WHERE :fooColumn = :fooId AND :bazColumn = :bazId",
        {"joinTable" -> joinTable,
        "fooColumn" -> joinTable.getColumn("Foo_id"),
        "fooId" -> foo.getId(),
        "bazColumn" -> joinTable.getColumn("Baz_id"),
        "bazId" -> baz.getId()})
    return new QueryExecutorImpl(getDB()).count("", sql.Sql, sql.Params)
  }
}