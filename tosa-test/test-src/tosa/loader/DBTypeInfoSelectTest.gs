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

  // ----------------------------------------
  // Tests for the select(String, Map) method
  // ----------------------------------------

  @Test
  function testSelectAllBarsReturnsAllBars() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()

    var result = Bar.select("SELECT * FROM \"Bar\"")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(bar.Id, result.get(0).Id)
    Assert.assertEquals("misc", result.get(0).Misc)
    Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectThatMatchesNothingReturnsEmptyQueryResult() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = 'nosuchvalue'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithOneParam() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()
    var bar2 = new Bar(){:Date = new java.util.Date("4/23/2009"), :Misc = "other"}
    bar2.update()

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg", {"arg" -> "misc"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(bar.Id, result.get(0).Id)
    Assert.assertEquals("misc", result.get(0).Misc)
    Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectWithTwoParams() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()
    var bar2 = new Bar(){:Date = new java.util.Date("4/23/2009"), :Misc = "other"}
    bar2.update()

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "nothing", "arg2" -> "misc"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(bar.Id, result.get(0).Id)
    Assert.assertEquals("misc", result.get(0).Misc)
    Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectWithTwoParamsReturningMultipleValues() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()
    var bar2 = new Bar(){:Date = new java.util.Date("4/23/2009"), :Misc = "other"}
    bar2.update()

    var result = Bar.select("SELECT * FROM \"Bar\" WHERE \"Misc\" = :arg OR \"Misc\" = :arg2", {"arg" -> "other", "arg2" -> "misc"})
    Assert.assertEquals(2, result.Count)
    if (result.get(0).Id == bar.Id) {
      Assert.assertEquals("misc", result.get(0).Misc)
      Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)

      Assert.assertEquals(bar2.Id, result.get(1).Id)
      Assert.assertEquals("other", result.get(1).Misc)
      Assert.assertEquals(new java.util.Date("4/23/2009"), result.get(1).Date)
    } else {
      Assert.assertEquals(bar2.Id, result.get(0).Id)
      Assert.assertEquals("other", result.get(0).Misc)
      Assert.assertEquals(new java.util.Date("4/23/2009"), result.get(1).Date)

      Assert.assertEquals(bar.Id, result.get(1).Id)
      Assert.assertEquals("misc", result.get(1).Misc)
      Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(1).Date)
    }
  }

  @Test
  function testSelectWithJoin() {
    var bar = new Bar(){:Date = new java.util.Date("4/22/2009"), :Misc = "misc"}
    bar.update()
    var bar2 = new Bar(){:Date = new java.util.Date("4/23/2009"), :Misc = "other"}
    bar2.update()

    var foo = new Foo(){:Bar = bar, :FirstName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:Bar = bar2, :FirstName = "Alice"}
    foo2.update()

    var result = Bar.select("SELECT * FROM \"Bar\" INNER JOIN \"Foo\" ON \"Foo\".\"Bar_id\" = \"Bar\".\"id\" WHERE \"Foo\".\"FirstName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(bar.Id, result.get(0).Id)
    Assert.assertEquals("misc", result.get(0).Misc)
    Assert.assertEquals(new java.util.Date("4/22/2009"), result.get(0).Date)
  }

  @Test
  function testSelectWithMultipleUsesOfSameParam() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(foo.Id, result.get(0).Id)
    Assert.assertEquals("Bob", result.get(0).FirstName)
    Assert.assertEquals("Bob", result.get(0).LastName)
  }

  @Test
  function testSelectWithUnusedParametersIgnoresExtraParameters() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(foo.Id, result.get(0).Id)
    Assert.assertEquals("Bob", result.get(0).FirstName)
    Assert.assertEquals("Bob", result.get(0).LastName)
  }

  @Test
  function testSelectWithMissingParametersDoesntSubstituteIfNoParamArgIsSpecified() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = ':arg' AND \"LastName\" = ':arg'")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithMissingParametersThrowsIfParamMapIsSpecified() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    try {
      var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arrg" -> "Bob"})
      Assert.fail("Expected an IllegalArgumentException")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("No value for the token arg was found in the map", e.Message)
    }
  }

  @Test
  function testSelectWithEscapedParametersDoesntSubstituteForEscapedParams() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    var result = Foo.select("SELECT * FROM \"Foo\" WHERE \"FirstName\" = '\\:arg' AND \"LastName\" = :arg", {"arg" -> "Bob"})
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSelectWithStringNotStartingWithSelectStartFromThrowsIllegalArgumentException() {
    try {
      Foo.select("SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'")
    } catch (e : IllegalArgumentException) {
      Assert.assertEquals("The select(String) method must always be called with 'SELECT * FROM' as the start of the statement.  The sql passed in was SELECT id FROM \"Foo\" WHERE \"FirstName\" = 'Bob'", e.Message)
    }
  }

  @Test
  function testSelectWithStringStartingWithSelectStarFromInOtherCaseIsFine() {
    var foo = new Foo(){:FirstName = "Bob", :LastName = "Bob"}
    foo.update()
    var foo2 = new Foo(){:FirstName = "Bob", :LastName = "Other"}
    foo2.update()

    var result = Foo.select("Select * From \"Foo\" WHERE \"FirstName\" = :arg AND \"LastName\" = :arg", {"arg" -> "Bob", "arg2" -> "Other", "arg3" -> "Hrm"})
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(foo.Id, result.get(0).Id)
    Assert.assertEquals("Bob", result.get(0).FirstName)
    Assert.assertEquals("Bob", result.get(0).LastName)
  }

  // Select that doesn't start with SELECT * FROM
  // Select that starts with Select * From
  // Check that the query being issued contains a ?

}
