package tosa.loader

uses java.io.*
uses java.lang.*
uses java.util.Map
uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.features.PropertyReference
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
uses tosa.TosaDBTestBase

class DBTypeInfoSelectTest extends TosaDBTestBase {

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
      assertEquals(expected.Id, actual.Id)
      assertEquals(expected.Date, actual.Date)
      assertEquals(expected.Misc, actual.Misc)
    } else if (expected typeis Foo and actual typeis Foo) {
      assertEquals(expected.Id, actual.Id)
      assertEquals(expected.FirstName, actual.FirstName)
      assertEquals(expected.LastName, actual.LastName)
    } else {
      fail("Unexpected object of type " + typeof(expected))
    }
  }

  private function assertException(callback : block(), exceptionType : IType, expectedMessage : String) {
    try {
      callback()
    } catch (e : Exception) {
      if (!exceptionType.isAssignableFrom(typeof(e))) {
        fail("Expected an exception of type " + exceptionType + " but got one of type " + typeof(e))
      }
      assertEquals(expectedMessage, e.Message)
    }
  }

  // ----------------------------------------
  // Tests for the select(String, Map) method
  // ----------------------------------------

  @Test
  function testSelectAllBarsReturnsAllBars() {
    var bar = createBar("4/22/2009", "misc")

    var result = Bar.select("SELECT * FROM Bar")
    assertEquals(1, result.Count)
    assertEquals(bar.Id, result.get(0).Id)
    assertEquals("misc", result.get(0).Misc)
    assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectThatMatchesNothingReturnsEmptyQueryResult() {
    var bar = createBar("4/22/2009", "misc")

    var result = Bar.select("SELECT * FROM Bar WHERE Misc = 'nosuchvalue'")
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM Bar WHERE Misc = :arg", {"arg" -> "misc"})
    assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM Bar WHERE Misc = :arg OR Misc = :arg2", {"arg" -> "nothing", "arg2" -> "misc"})
    assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithTwoParamsReturningMultipleValues() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.select("SELECT * FROM Bar WHERE Misc = :arg OR Misc = :arg2", {"arg" -> "other", "arg2" -> "misc"})
    assertEquals(2, result.Count)
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

    var result = Bar.select("SELECT * FROM Bar INNER JOIN Foo ON Foo.Bar_id = Bar.id WHERE Foo.FirstName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM Foo WHERE FirstName = ':arg' AND LastName = ':arg'")
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.select("SELECT * FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arrg" -> "Bob"})
      fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testSelectWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("SELECT * FROM Foo WHERE FirstName = '\\:arg' AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithStringNotStartingWithSelectStartFromThrowsIllegalArgumentException() {
    try {
      Foo.select("SELECT id FROM Foo WHERE FirstName = 'Bob'")
    } catch (e : IllegalArgumentException) {
      assertEquals("The select(String, Map) method must always be called with 'SELECT * FROM' as the start of the statement.  The sql passed in was SELECT id FROM Foo WHERE FirstName = 'Bob'", e.Message)
    }
  }

  @Test
  function testSelectWithStringStartingWithSelectStarFromInOtherCaseIsFine() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.select("Select * From Foo WHERE FirstName = :arg AND LastName = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
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
    assertEquals(2, result.Count)
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
    assertEquals(2, result.Count)
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

    var result = Bar.selectWhere("Misc = 'nosuchvalue'")
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("Misc = :arg", {"arg" -> "misc"})
    assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWhereWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("Misc = :arg OR Misc = :arg2", {"arg" -> "nothing", "arg2" -> "misc"})
    assertEquals(1, result.Count)
    assertMatch(bar, result.get(0))
  }

  @Test
  function testSelectWhereWithTwoParamsReturningMultipleValues() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var result = Bar.selectWhere("Misc = :arg OR Misc = :arg2", {"arg" -> "other", "arg2" -> "misc"})
    assertEquals(2, result.Count)
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

    var result = Foo.selectWhere("FirstName = :arg AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWhereWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("FirstName = :arg AND LastName = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    assertEquals(1, result.Count)
    assertMatch(foo, result.get(0))
  }

  @Test
  function testSelectWhereWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("FirstName = ':arg' AND LastName = ':arg'")
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.selectWhere("FirstName = :arg AND LastName = :arg", {"arrg" -> "Bob"})
      fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testSelectWhereWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.selectWhere("FirstName = '\\:arg' AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(0, result.Count)
  }

  @Test
  function testSelectWhereStartingWithSelectThrowsIllegalArgumentException() {
    assertException(\ -> Foo.selectWhere("SELECT * FROM Foo WHERE FirstName = 'Bob'"),
        IllegalArgumentException,
        "The selectWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the select(String, Map) method instead.")
  }

  @Test
  function testSelectWhereStartingWithSelectInAlternateCaseThrowsIllegalArgumentException() {
    assertException(\ -> Foo.selectWhere("Select * from Foo WHERE FirstName = 'Bob'"),
        IllegalArgumentException,
        "The selectWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the select(String, Map) method instead.")
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
    assertEquals(2, Bar.count("SELECT count(*) as count FROM Bar"))
  }

  @Test
  function testCountThatMatchesNothingReturnsZero() {
    var bar = createBar("4/22/2009", "misc")
    assertEquals(0, Bar.count("SELECT count(*) as count FROM Bar WHERE Misc = 'nosuchvalue'"))
  }

  @Test
  function testCountWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(1, Bar.count("SELECT count(*) as count FROM Bar WHERE Misc = :arg", {"arg" -> "misc"}))
  }

  @Test
  function testCountWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(1, Bar.count("SELECT count(*) as count FROM Bar WHERE Misc = :arg OR Misc = :arg2", {"arg" -> "nothing", "arg2" -> "misc"}))
  }

  @Test
  function testCountWithJoin() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")

    var foo = new Foo(){:Bar = bar, :FirstName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:Bar = bar2, :FirstName = "Alice"}
    foo2.update()

    var result = Bar.count("SELECT count(*) as count FROM Bar INNER JOIN Foo ON Foo.Bar_id = Bar.id WHERE Foo.FirstName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result)
  }

  @Test
  function testCountWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result)
  }

  @Test
  function testCountWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    assertEquals(1, result)
  }

  @Test
  function testCountWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM Foo WHERE FirstName = ':arg' AND LastName = ':arg'")
    assertEquals(0, result)
  }

  @Test
  function testCountWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.count("SELECT count(*) as count FROM Foo WHERE FirstName = :arg AND LastName = :arg", {"arrg" -> "Bob"})
      fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testCountWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.count("SELECT count(*) as count FROM Foo WHERE FirstName = '\\:arg' AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(0, result)
  }

  @Test
  function testCountWithStringNotStartingWithSelectCountStarAsCountFromThrowsIllegalArgumentException() {
    try {
      Foo.count("SELECT id FROM Foo WHERE FirstName = 'Bob'")
    } catch (e : IllegalArgumentException) {
      assertEquals("The count(String, Map) method must always be called with 'SELECT count(*) as count FROM' as the start of the statement.  The sql passed in was SELECT id FROM Foo WHERE FirstName = 'Bob'", e.Message)
    }
  }

  // ---------------------------------------------
  // Tests for the countWhere(String, Map) method
  // ---------------------------------------------

  @Test
  function testCountWhereWithNullArgumentCountsAllElements() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(2, Bar.countWhere(null))
  }

  @Test
  function testCountWhereWithEmptyArgumentCountsAllElements() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(2, Bar.countWhere(""))
  }

  @Test
  function testCountWhereThatMatchesNothingReturnsZero() {
    var bar = createBar("4/22/2009", "misc")
    assertEquals(0, Bar.countWhere("Misc = 'nosuchvalue'"))
  }

  @Test
  function testCountWhereWithOneParam() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(1, Bar.countWhere("Misc = :arg", {"arg" -> "misc"}))
  }

  @Test
  function testCountWhereWithTwoParams() {
    var bar = createBar("4/22/2009", "misc")
    var bar2 = createBar("4/23/2009", "other")
    assertEquals(1, Bar.countWhere("Misc = :arg OR Misc = :arg2", {"arg" -> "nothing", "arg2" -> "misc"}))
  }

  @Test
  function testCountWhereWithMultipleUsesOfSameParam() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.countWhere("FirstName = :arg AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(1, result)
  }

  @Test
  function testCountWhereWithUnusedParametersIgnoresExtraParameters() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.countWhere("FirstName = :arg AND LastName = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    assertEquals(1, result)
  }

  @Test
  function testCountWhereWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.countWhere("FirstName = ':arg' AND LastName = ':arg'")
    assertEquals(0, result)
  }

  @Test
  function testCountWhereWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    try {
      var result = Foo.countWhere("FirstName = :arg AND LastName = :arg", {"arrg" -> "Bob"})
      fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testCountWhereWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = createFoo("Bob", "Bob")
    var foo2 = createFoo("Bob", "Other")

    var result = Foo.countWhere("FirstName = '\\:arg' AND LastName = :arg", {"arg" -> "Bob"})
    assertEquals(0, result)
  }

  @Test
  function testCountWhereWithStringStartingWithSelectThrowsIllegalArgumentException() {
    assertException(\ -> Foo.countWhere("SELECT count(*) as count FROM Foo WHERE FirstName = 'Bob'"),
        IllegalArgumentException,
        "The countWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the count(String, Map) method instead.")
  }

  @Test
  function testCountWhereWithStringStartingWithSelectInAlternateCaseThrowsIllegalArgumentException() {
    assertException(\ -> Foo.countWhere("select count(*) as count FROM Foo WHERE FirstName = 'Bob'"),
        IllegalArgumentException,
        "The countWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the count(String, Map) method instead.")
  }


  // TODO - AHK - Validate the SQL being spit out
  // Check that the query being issued contains a ?

}
