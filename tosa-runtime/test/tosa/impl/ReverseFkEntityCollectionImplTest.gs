package tosa.impl

uses tosa.TosaDBTestBase
uses test.testdb.Bar
uses test.testdb.Foo
uses org.junit.Test
uses java.lang.IndexOutOfBoundsException
uses java.lang.UnsupportedOperationException
uses java.lang.IllegalStateException
uses java.lang.IllegalArgumentException
uses tosa.api.EntityCollection

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
class ReverseFkEntityCollectionImplTest extends TosaDBTestBase {

  @Test
  function sanityCheck() {
    assertTrue(createAndCommitBar().Foos typeis ReverseFkEntityCollectionImpl)
  }

  @Test
  function testSizeReturnsZeroIfArrayIsEmptyAndHasBeenLoaded() {
    var bar = createAndCommitBar();
    bar.Foos.load();
    assertEquals(0, bar.Foos.size());
  }

  @Test
  function testSizeReturnsZeroIfArrayIsEmptyAndHasNotBeenLoaded() {
    var bar = createAndCommitBar();
    assertEquals(0, bar.Foos.size());
  }

  @Test
  function testSizeReturnsCorrectSizeForNonEmptyLoadedArray() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    bar.Foos.load();
    assertEquals(2, bar.Foos.size());
  }

  @Test
  function testSizeReturnsCorrectSizeForNonEmptyNonLoadedArray() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(2, bar.Foos.size());
  }

  @Test
  function testSizeReturnsOriginalSizeForLoadedArrayAfterChangesInDB() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    bar.Foos.load();
    assertEquals(2, bar.Foos.size());
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(2, bar.Foos.size());
  }

  @Test
  function testSizeReturnsCurrentSizeForNonLoadedArrayAfterChangesInDB() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(2, bar.Foos.size());
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(4, bar.Foos.size());
  }

  @Test
  function testSizeIssuesCountStarQueryIfArrayHasNotBeenLoaded() {
    var bar = createAndCommitBar();
    var spy = insertSpy(bar)
    assertEquals(0, bar.Foos.size());
    assertTrue(spy.countCalled());
    assertFalse(spy.selectCalled());
    assertFalse(spy.updateCalled());
  }

  @Test
  function testSizeDoesNotIssueQueriesIfArrayHasBeenLoaded() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    var spy = insertSpy(bar)
    bar.Foos.load();
    spy.reset();
    assertEquals(2, bar.Foos.size());
    assertFalse(spy.anyCalled());
  }

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsNegative() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    assertException(\ -> bar.Foos.get(-1), IndexOutOfBoundsException)
  }

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsGreaterThanSizeOfCollection() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    assertException(\ -> bar.Foos.get(2), IndexOutOfBoundsException)
  }

  @Test
  function testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsEqualToSizeOfCollection() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    assertException(\ -> bar.Foos.get(1), IndexOutOfBoundsException)
  }

  @Test
  function testGetReturnsAppropriateElementIfArgumentIsValid() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    var fooFromDB = bar.Foos.get(0);
    assertEquals(foo.getColumnValue("id"), fooFromDB.getColumnValue("id"));
  }

  @Test
  function testIteratorHasNextReturnsFalseForEmptyList() {
    var bar = createAndCommitBar();
    assertFalse(bar.Foos.iterator().hasNext());
  }

  @Test
  function testIteratorHasNextReturnsTrueAtStartOfNonEmptyList() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    assertTrue(bar.Foos.iterator().hasNext());
  }

  @Test
  function testIteratorNextReturnsItemsInIdOrder() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    var foo2 = createAndCommitFoo(bar);
    var foo3 = createAndCommitFoo(bar);
    var it = bar.Foos.iterator();
    assertTrue(it.hasNext());
    assertEquals(foo1.getColumnValue("id"), it.next().getColumnValue("id"));
    assertTrue(it.hasNext());
    assertEquals(foo2.getColumnValue("id"), it.next().getColumnValue("id"));
    assertTrue(it.hasNext());
    assertEquals(foo3.getColumnValue("id"), it.next().getColumnValue("id"));
    assertFalse(it.hasNext());
  }

  @Test
  function testIteratorHasNextReturnsFalseAtEndOfList() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    var it = bar.Foos.iterator();
    assertTrue(it.hasNext());
    it.next();
    assertFalse(it.hasNext());
  }

  @Test
  function testIteratorRemoveThrowsUnsupportedOperationException() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    var it = bar.Foos.iterator();
    it.next();
    assertException(\ -> it.remove(), UnsupportedOperationException)
  }

  // TODO - AHK - Test for concurrent modification exceptions?

  @Test
  function testAddThrowsIllegalStateExceptionIfOwningEntityHasNotYetBeenCommitted() {
    var bar = new Bar()
    var foo = new Foo()
    assertException(\ -> bar.Foos.add(foo), IllegalStateException)
  }

  @Test
  function testAddSetsFkPointerIfEntityIsAlreadyInThisCollection() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    assertEquals(1, bar.Foos.size());
    bar.Foos.add(foo);
    assertEquals(1, bar.Foos.size());
    assertSame(bar, foo.getFkValue("Bar_id"));
  }

  @Test
  function testAddThrowsIllegalArgumentExceptionIfEntityIsAlreadyInAnotherCollection() {
    var bar1 = createAndCommitBar();
    var bar2 = createAndCommitBar();
    var foo = new Foo(){ :Bar = bar2 }
    foo.update()
    assertException(\ -> bar1.Foos.add(foo), IllegalArgumentException)
  }

  @Test
  function testAddThrowsIllegalArgumentExceptionIfEntityIsOfWrongType() {
    var bar1 = createAndCommitBar();
    var bar2 = createAndCommitBar();
    var list : EntityCollection = bar1.Foos
    assertException(\ -> list.add(bar2), IllegalArgumentException)
  }

  @Test
  function testAddInsertsNewObjectInDatabaseAndSetsFkBackPointerIfObjectIsNotYetCommitted() {
    var bar = createAndCommitBar();
    var foo = new Foo()
    assertNull(foo.Id);
    assertTrue(foo.isNew());
    bar.Foos.add(foo);
    assertNotNull(foo.Id);
    assertFalse(foo.isNew());
    assertSame(bar, foo.Bar);
    assertEquals(1, countMatchesInDB(foo, bar));
  }

  @Test
  function testAddUpdatesFkInDatabaseAndSetsFkBackPointerIfObjectHasAlreadyBeenPersisted() {
    var bar = createAndCommitBar();
    var foo = new Foo()
    foo.update()
    assertEquals(0, countMatchesInDB(foo, bar));
    bar.Foos.add(foo);
    assertEquals(bar.Id, foo.Bar.Id);
    assertSame(bar, foo.Bar);
    assertEquals(1, countMatchesInDB(foo, bar));
  }

  @Test
  function testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    var foo2 = new Foo()
    foo2.update()
    var foo3 = createAndCommitFoo(bar);

    assertEquals(2, bar.Foos.size());
    bar.Foos.add(foo2);
    assertEquals(3, bar.Foos.size());
    assertEquals(foo1.Id, bar.Foos.get(0).Id);
    assertEquals(foo2.Id, bar.Foos.get(1).Id);
    assertEquals(foo3.Id, bar.Foos.get(2).Id);
    // TODO - AHK - Test that it's not the same object?
  }

  @Test
  function testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
    var bar = createAndCommitBar();
    var foo1 = createAndCommitFoo(bar);
    var foo2 = new Foo()
    foo2.update()
    var foo3 = createAndCommitFoo(bar);

    assertEquals(2, bar.Foos.size());
    bar.Foos.load();
    bar.Foos.add(foo2);
    assertEquals(3, bar.Foos.size());
    assertEquals(foo1.Id, bar.Foos.get(0).Id);
    assertEquals(foo3.Id, bar.Foos.get(1).Id);
    assertEquals(foo2.Id, bar.Foos.get(2).Id);
    assertSame(foo2, bar.Foos.get(2));
  }

  @Test
  function testUnloadWillUnloadCachedData() {
    var bar = createAndCommitBar();
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    bar.Foos.load();
    assertEquals(2, bar.Foos.size());
    createAndCommitFoo(bar);
    createAndCommitFoo(bar);
    assertEquals(2, bar.Foos.size());
    bar.Foos.unload();
    assertEquals(4, bar.Foos.size());
    bar.Foos.load();
    assertEquals(4, bar.Foos.size());
  }

  // ----------------------- Tests for remove()

  @Test
  function testRemoveWillThrowIllegalArgumentExceptionIfElementIsNotAMemberOfThisArray() {
    var bar = createAndCommitBar();
    var bar2 = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    assertException(\ -> bar2.Foos.remove(foo), IllegalArgumentException)
  }

  @Test
  function testRemoveWillThrowIllegalStateExceptionIfOwnerIsNew() {
    var bar = new Bar()
    var foo = new Foo()
    assertException(\ -> bar.Foos.remove(foo), IllegalStateException)
  }

  @Test
  function testRemoveWillThrowIllegalArgumentExceptionIfElementIsOfWrongType() {
    var bar = createAndCommitBar();
    var bar2 = createAndCommitBar();
    var list : EntityCollection = bar.Foos
    assertException(\ -> list.remove(bar2), IllegalArgumentException)
  }

  @Test
  function testRemoveWillImmediatelyUpdateDatabaseIfArrayNotLoadedYet() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    bar.Foos.unload();
    assertEquals(1, countMatchesInDB(foo, bar));
    bar.Foos.remove(foo);
    assertEquals(0, countMatchesInDB(foo, bar));
  }

  @Test
  function testRemoveWillImmediatelyUpdateDatabaseAndRemoveFromCachedResultsIfArrayLoaded() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    bar.Foos.load();
    assertEquals(1, countMatchesInDB(foo, bar));
    bar.Foos.remove(foo);
    assertEquals(0, countMatchesInDB(foo, bar));
    assertEquals(0, bar.Foos.size());
    assertFalse(bar.Foos.iterator().hasNext());
  }

  @Test
  function testRemoveWillNullOutFkColumnOnElement() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    assertEquals(bar.Id, foo.Bar.Id);
    bar.Foos.remove(foo);
    assertNull(foo.Bar);
  }

  @Test
  function testRemoveWillNullOutCachedFkObjectOnElement() {
    var bar = createAndCommitBar();
    var foo = createAndCommitFoo(bar);
    assertNotNull(foo.Bar);
    bar.Foos.remove(foo);
    assertNull(foo.Bar);
  }

  // ======================= Private Helper Methods ==================

  private function createAndCommitBar() : Bar {
    var bar = new Bar()
    bar.update()
    return bar;
  }

  private function createAndCommitFoo(bar : Bar) : Foo {
    var foo = new Foo()
    foo.Bar = bar
    foo.update()
    return foo;
  }

  private function insertSpy(bar : Bar) : QueryExecutorSpy {
    var spy = new QueryExecutorSpy(getDB())
    (bar.Foos as ReverseFkEntityCollectionImpl).setQueryExecutor(spy)
    return spy
  }

  private function countMatchesInDB(foo : Foo, bar : Bar) : int {
    var sql = SimpleSqlBuilder.substitute("SELECT count(*) as count FROM \${fooTable} WHERE \${idColumn} = \${fooId} AND \${barColumn} = \${barId}",
      "fooTable", foo.getDBTable(),
      "idColumn", "id",
      "fooId", foo.Id,
      "barColumn", "Bar_id",
      "barId", bar.Id);
    return new QueryExecutorImpl(getDB()).count("", sql, {});
  }
}