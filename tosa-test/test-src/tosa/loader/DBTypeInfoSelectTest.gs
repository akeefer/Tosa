package tosa.loader

uses java.io.*
uses java.lang.*
uses java.util.Map
uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.features.PropertyReference
uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test
uses test.testdb.Bar
uses test.testdb.SortPage
uses test.testdb.Foo
uses test.testdb.Baz
uses gw.lang.reflect.TypeSystem
uses tosa.api.EntityCollection
uses tosa.api.QueryResult
uses tosa.impl.md.DatabaseImplSource
uses tosa.api.IDBObject
uses gw.lang.reflect.IType

class DBTypeInfoSelectTest {

  @BeforeClass
  static function beforeTestClass() {
    TosaTestDBInit.createDatabase()
  }

  @Before
  function beforeTestMethod() {
    deleteAllData()
  }

  private function deleteAllData() {
    // clearTable("SelfJoins_join_Baz_Baz")
    // clearTable("Relatives_join_Bar_Baz")
    // clearTable("join_Foo_Baz")
    // clearTable("Baz")
    clearTable("Foo")
    // clearTable("SortPage")
    clearTable("Bar")
  }

  private function clearTable(tableName : String) {
    var database = DatabaseImplSource.getInstance().getDatabase( "test.testdb" )
    var connection = database.Connection.connect()
    connection.createStatement().executeUpdate( "DELETE FROM \"${tableName}\"" )
    connection.close()
  }

  private function createBar(date : String, misc : String) : Bar {
    var bar = new Bar(){:Date = new java.util.Date(date), :Misc = misc}
    bar.update()
    return bar
  }

  private function createFoo(firstName : String, lastName : String) : Foo {
    var foo = new Foo(){:FirstName = firstName, :LastName = lastName}
    foo.update()
    return foo
  }

  private function assertMatch<T extends IDBObject>(expected : T, actual : T) {
    if (expected typeis Bar and actual typeis Bar) {
      Assert.assertEquals(expected.Id, actual.Id)
      Assert.assertEquals(expected.Date, actual.Date)
      Assert.assertEquals(expected.Misc, actual.Misc)
    } else if (expected typeis Foo and actual typeis Foo) {
      Assert.assertEquals(expected.Id, actual.Id)
      Assert.assertEquals(expected.FirstName, actual.FirstName)
      Assert.assertEquals(expected.LastName, actual.LastName)
    } else {
      Assert.fail("Unexpected object of type " + typeof(expected))
    }
  }

  private function assertException(callback : block(), exceptionType : IType, expectedMessage : String) {
    try {
      callback()
    } catch (e : Exception) {
      if (!exceptionType.isAssignableFrom(typeof(e))) {
        Assert.fail("Expected an exception of type " + exceptionType + " but got one of type " + typeof(e))
      }
      Assert.assertEquals(expectedMessage, e.Message)
    }
  }

  // ----------------------------------------
  // Tests for the select(String, Map) method
  // ----------------------------------------

  @Test
  function testSelectAllBarsReturnsAllBars() {
    var bar = createBar("4/22/2009", "misc")

    var result = Bar.select("SELECT * FROM \"Bar\"")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(bar.Id, result.get(0).Id)
    Assert.assertEquals("misc", result.get(0).Misc)
    Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectThatMatchesNothingReturnsEmptyQueryResult() {
    var bar = createBar("4/22/2009", "misc")

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = 'nosuchvalue'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg", {"arg" -> "misc"})
    Assert.assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "nothing", "arg2" -> "misc"})
    Assert.assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithTwoParamsReturningMultipleValues() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "other", "arg2" -> "misc"})
    Assert.assertEquals(2, result.Count)
    if (result.get(0).Id == bar.Id) {
      assertMatch(bar, result.get(0))
      assertMatch(bar2, result.get(1))
    } else {
      assertMatch(bar2, result.get(0))
      assertMatch(bar, result.get(1))
    }
  }

  @Test
  function testSelectWithJoin() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var foo = new Foo(){:Bar = bar, :FirstName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:Bar = bar2, :FirstName = "Alice"}
    foo2.update()

    var result = Bar.select("SELECT * FROM \"Bar\" INNER JOIN \"Foo\" ON \"Foo\".\"Bar_id\" = \"Bar\".\"id\" WHERE \"Foo\".\"FirstName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    Assert.assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = ':arg' AND \"LastName\" = ':arg'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arrg" -> "Bob"})
      Assert.fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testSelectWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = '\\:arg' AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithStringNotStartingWithSelectStartFromThrowsIllegalArgumentException() {
    try {
      Foo.select("SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("The select(String, Map) method must always be called with 'SELECT * FROM' as the start of the statement.  The sql passed in was SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'", e.Message)
    }
  }

  @Test
  function testSelectWithStringStartingWithSelectStarFromInOtherCaseIsFine() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("Select * From \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    assertMatch(foo, result.get(0))
  }

  // TODO - AHK - Validate the SQL being spit out
  // Check that the query being issued contains a ?

  // ---------------------------------------------
  // Tests for the selectWhere(String, Map) method
  // ---------------------------------------------

  // Select that starts with "SELECT" throws an error

  @Test
  function testSelectWhereWithNullArgReturnsAllBars() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere(null)
    Assert.assertEquals(2, result.Count)
    if (result.get(0).Id == bar.Id) {
      assertMatch(bar, result.get(0))
      assertMatch(bar2, result.get(1))
    } else {
      assertMatch(bar2, result.get(0))
      assertMatch(bar, result.get(1))
    }
  }

  @Test
  function testSelectWhereWithEmptyArgReturnsAllBars() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("")
    Assert.assertEquals(2, result.Count)
    if (result.get(0).Id == bar.Id) {
      assertMatch(bar, result.get(0))
      assertMatch(bar2, result.get(1))
    } else {
      assertMatch(bar2, result.get(0))
      assertMatch(bar, result.get(1))
    }
  }

  @Test
  function testSelectWhereThatMatchesNothingReturnsEmptyQueryResult() {
    var bar = createBar("4/22/2009", "misc")

    var result = Bar.selectWhere("\"Misc\" = 'nosuchvalue'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("\"Misc\" = :arg", {"arg" -> "misc"})
    Assert.assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWhereWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("\"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "nothing", "arg2" -> "misc"})
    Assert.assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWhereWithTwoParamsReturningMultipleValues() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("\"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "other", "arg2" -> "misc"})
    Assert.assertEquals(2, result.Count)
    if (result.get(0).Id == bar.Id) {
      assertMatch(bar, result.get(0))
      assertMatch(bar2, result.get(1))
    } else {
      assertMatch(bar2, result.get(0))
      assertMatch(bar, result.get(1))
    }
  }

  @Test
  function testSelectWhereWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("\"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWhereWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("\"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    Assert.assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWhereWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("\"FirstName\" = ':arg' AND \"LastName\" = ':arg'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.selectWhere("\"FirstName\" = :arg AND \"LastName\" = :arg", {"arrg" -> "Bob"})
      Assert.fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testSelectWhereWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("\"FirstName\" = '\\:arg' AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereStartingWithSelectThrowsIllegalArgumentException() {
    assertException(\ -> Foo.selectWhere("SELECT * FROM \"Foo\" WHERE \"FirstName\" = 'Bob'"),
        IllegalArgumentException,
        "The selectWhere(String, Map) method should only be caused with the WHERE clause of a query.  To specify the full SQL for the query, use the select(String, Map) method instead.")
  }

  @Test
  function testSelectWhereStartingWithSelectInAlternateCaseThrowsIllegalArgumentException() {
    assertException(\ -> Foo.selectWhere("Select * from \"Foo\" WHERE \"FirstName\" = 'Bob'"),
        IllegalArgumentException,
        "The selectWhere(String, Map) method should only be caused with the WHERE clause of a query.  To specify the full SQL for the query, use the select(String, Map) method instead.")
  }
  // TODO - AHK - Error cases


  // TODO - AHK - Error cases passing null in for a prepared statement value

  // ---------------------------------------------
  // Tests for the count(String, Map) method
  // ---------------------------------------------

  @Test
  function testCountWithNoWhereClauseCountsAllBars() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    Assert.assertEquals(2, Bar.count("SELECT count(*) as count FROM \"Bar\""))
  }

  @Test
  function testCountThatMatchesNothingReturnsZero() {
    var bar = createBar("4/22/2009", "misc")
    Assert.assertEquals(0, Bar.count("SELECT count(*) as count FROM \"Bar\" WHERE \"Misc\" = 'nosuchvalue'"))
  }

  @Test
  function testCountWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    Assert.assertEquals(1, Bar.count("SELECT count(*) as count FROM \"Bar\" WHERE \"Misc\" = :arg", {"arg" -> "misc"}))
  }

  @Test
  function testCountWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    Assert.assertEquals(1, Bar.count("SELECT count(*) as count FROM \"Bar\" WHERE \"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "nothing", "arg2" -> "misc"}))
  }

  @Test
  function testCountWithJoin() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var foo = new Foo(){:Bar = bar, :FirstName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:Bar = bar2, :FirstName = "Alice"}
    foo2.update()

    var result = Bar.count("SELECT count(*) as count FROM \"Bar\" INNER JOIN \"Foo\" ON \"Foo\".\"Bar_id\" = \"Bar\".\"id\" WHERE \"Foo\".\"FirstName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result)
  }

  @Test
  function testCountWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result)
  }

  @Test
  function testCountWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    Assert.assertEquals(1, result)
  }

  @Test
  function testCountWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM \"Foo\" WHERE \"FirstName\" = ':arg' AND \"LastName\" = ':arg'")
    Assert.assertEquals(0, result)
  }

  @Test
  function testCountWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.count("SELECT count(*) as count FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arrg" -> "Bob"})
      Assert.fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testCountWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM \"Foo\" WHERE \"FirstName\" = '\\:arg' AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(0, result)
  }

  @Test
  function testSelectWithStringNotStartingWithSelectCountStarAsCountFromThrowsIllegalArgumentException() {
    try {
      Foo.count("SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("The count(String, Map) method must always be called with 'SELECT count(*) as count FROM' as the start of the statement.  The sql passed in was SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'", e.Message)
    }
  }
  // TODO - AHK - Validate the SQL being spit out
  // Check that the query being issued contains a ?

}
