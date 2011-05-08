package tosa.endtoend

uses test.testdb.Foo
uses test.testdb.Bar
uses tosa.loader.TosaTestDBInit

uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test

uses java.lang.IllegalStateException

class OverallCollectionAPITest {

  @BeforeClass
  static function beforeTestClass() {
    TosaTestDBInit.createDatabase()
  }

  @Test
  function testArrayAdditionToExistingObjectIsImmediateInTheDatabase() {
    var bar = new Bar()
    bar.update()

    var foo = new Foo()
    bar.Foos.add(foo)

    var queryResults = Foo.findWithSql("select * from \"Foo\" where \"Bar_id\" = ${bar.id}")
    Assert.assertEquals(1, queryResults.size())

    Assert.assertEquals(1, bar.Foos.size())
    Assert.assertSame(foo, bar.Foos.get(0))
  }

  @Test
  function testArrayAdditionToNotYetInsertedObjectThrowsException() {
    var bar = new Bar()
    var foo = new Foo()
    try {
      bar.Foos.add(foo)
      Assert.fail("Expected an IllegalStateException")
    } catch (e : IllegalStateException) {
      // Expected
    }
  }

  @Test
  function testArrayAdditionOfNotYetPersistedObjectInsertsIt() {
    var bar = new Bar()
    bar.update()

    var foo = new Foo()
    foo.FirstName = "First"
    bar.Foos.add(foo)
    foo.LastName = "Last"

    var queryResults = Foo.findWithSql("select * from \"Foo\" where \"Bar_id\" = ${bar.id}")
    Assert.assertEquals(1, queryResults.size())
    Assert.assertEquals("First", queryResults.get(0).FirstName)
    Assert.assertEquals(null, queryResults.get(0).LastName)

    Assert.assertEquals(1, bar.Foos.size())
    Assert.assertSame(foo, bar.Foos.get(0))
  }

  @Test
  function testArrayAdditionOfPersistedObjectOnlyUpdatesFKColumn() {
    var bar = new Bar()
    bar.update()

    var foo = new Foo()
    foo.update()
    foo.FirstName = "First"
    foo.LastName = "Last"
    bar.Foos.add(foo)

    var queryResults = Foo.findWithSql("select * from \"Foo\" where \"Bar_id\" = ${bar.id}")
    Assert.assertEquals(1, queryResults.size())
    Assert.assertEquals(null, queryResults.get(0).FirstName)
    Assert.assertEquals(null, queryResults.get(0).LastName)

    Assert.assertEquals(1, bar.Foos.size())
    Assert.assertSame(foo, bar.Foos.get(0))
  }

  @Test
  function testArrayRemovalFromObjectIsImmediateInTheDatabase() {
    var bar = new Bar()
    bar.update()

    var foo = new Foo()
    foo.update()
    bar.Foos.add(foo)

    var queryResults = Foo.findWithSql("select * from \"Foo\" where \"Bar_id\" = ${bar.id}")
    Assert.assertEquals(1, queryResults.size())

    Assert.assertEquals(1, bar.Foos.size())
    Assert.assertSame(foo, bar.Foos.get(0))

    bar.Foos.remove(foo)
    queryResults = Foo.findWithSql("select * from \"Foo\" where \"Bar_id\" = ${bar.id}")
    Assert.assertEquals(0, queryResults.size())

    Assert.assertEquals(0, bar.Foos.size())
  }

  @Test
  function testArrayRemovalFromObjectOnlyUpdatesFKColumn() {

  }

  @Test
  function testArrayBackpointerIsReadOnly() {

  }

  @Test
  function testArrayAddWhenObjectAlreadyInAnotherArrayThrowsException() {

  }

  @Test
  function testJoinArrayAdditionToExistingObjectIsImmediateInTheDatabase() {

  }

  @Test
  function testJoinArrayAdditionToNotYetInsertedObjectThrowsException() {

  }

  @Test
  function testJoinArrayAdditionOfNotYetPersistedObjectInsertsIt() {

  }

  @Test
  function testJoinArrayAdditionOfPersistedObjectOnlyInsertsTheJoinObject() {

  }

  @Test
  function testJoinArrayRemovalFromObjectIsImmediateInTheDatabase() {

  }

  @Test
  function testJoinArrayRemovalFromObjectOnlyDeletesFromTheJoinTable() {

  }

  @Test
  function testJoinArrayAddWhenObjectAlreadyInAnotherArrayThrowsException() {

  }

}