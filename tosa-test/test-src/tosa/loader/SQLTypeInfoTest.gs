package tosa.loader

uses java.io.*
uses java.lang.*
uses java.util.*
uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.features.PropertyReference
uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test
uses org.junit.Ignore
uses test.testdb.*
uses test.query.*
uses gw.lang.reflect.TypeSystem

class SQLTypeInfoTest {

  @BeforeClass
  static function beforeTestClass() {
    print("*** Before class")
    TosaTestDBInit.createDatabase()
  }

  private function deleteAllData() {
    clearTable("Bar")
    clearTable("Foo")
    clearTable("ForOrderByTests")
    clearTable("ForGroupByTests")
    clearTable("ForNumericTests")
  }

  private function clearTable(tableName : String) {
    print("Clearing table ${tableName}")
    var dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader)
    var dbTypeData = dbTypeLoader.getTypeDataForNamespace( "test.testdb" )
    var connection = dbTypeData.Connection.connect()
    connection.createStatement().executeUpdate( "DELETE FROM \"${tableName}\"" )
    connection.close()
  }

  private function importSampleData() {
    var bar = new Bar(){:Date = sqlDate("4/22/2009"), :Misc = "misc"}
    bar.update()
    var foo = new Foo(){:Bar = bar, :FirstName="First", :LastName="Bar"}
    foo.update()
    var foo2 = new Foo(){:FirstName="First2", :LastName="Bar2"}
    foo2.update()

    var orderBys = {
      new ForOrderByTests() { :Number=1, :Date=sqlDate("4/22/2009"), :Str="g", :Str2="a" },
      new ForOrderByTests() { :Number=2, :Date=sqlDate("4/22/2008"), :Str="z", :Str2="a" },
      new ForOrderByTests() { :Number=3, :Date=sqlDate("4/22/2007"), :Str="a", :Str2="a" }
    }
    orderBys*.update()

    var groupBys = {
      new ForGroupByTests() { :Number=1, :Date=sqlDate("4/22/2009"), :Str="a", :Str2="a" },
      new ForGroupByTests() { :Number=2, :Date=sqlDate("4/22/2008"), :Str="b", :Str2="a" },
      new ForGroupByTests() { :Number=3, :Date=sqlDate("4/22/2007"), :Str="c", :Str2="b" }
    }
    groupBys*.update()

    var forNumericTests = {
      new ForNumericTests() { :Number=1 },
      new ForNumericTests() { :Number=2 },
      new ForNumericTests() { :Number=3 }
    }
    forNumericTests*.update()
  }

  private function sqlDate( s : String ) : java.sql.Date {
    return new java.sql.Date(new java.util.Date(s).Time)
  }

  @Before
  function beforeTestMethod() {
    deleteAllData()
    importSampleData()
  }

  @Test
  function testTypesCreated() {
    var types = gw.lang.reflect.TypeSystem.getAllTypeNames()
    Assert.assertTrue(types.contains("test.query.SampleQuery"))
  }

  @Test
  function testBasicSelectWorks() {
    var result = test.query.SampleQuery.select()
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    print(statictypeof result)
  }

  @Test
  function testBasicColumnComparisonWorks() {
    var result = test.query.SampleComparisonQuery.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testBasicColumnComparisonIsFalseWorks() {
    var result = test.query.SampleComparisonIsFalseQuery.select()
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testBasicColumnComparisonIsTrueWorks() {
    var result = test.query.SampleComparisonIsTrueQuery.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testBasicBetweenComparisonWorks() {
    var result = test.query.SampleBetweenQuery.select("1/1/2001".toDate(), "1/1/2101".toDate())
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleBetweenQuery.select("1/1/2101".toDate(), "1/1/2101".toDate())
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testBasicInComparisonWorks() {
    var result = test.query.SampleInQuery.select("blah", "misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleInQuery.select("misc", "blah")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleInQuery.select("blah", "blah")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testInComparisonWithListArgWorks() {
    var result = test.query.SampleInQueryWithList.select({"blah", "misc"})
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleInQueryWithList.select({"misc", "blah"})
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleInQueryWithList.select({"blah", "blah"})
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testInComparisonWithNullListArgWorks() {
    var result = test.query.SampleInQueryWithList.select(null)
    Assert.assertEquals(0, result.Count)
  }
  
  @Test
  function testInComparisonWithNamedArgWorks() {
    var result = test.query.SampleInQueryWithList.select(:lst = null)
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testInComparisonWithSubSelectWorks() {
    var result = test.query.SampleInQueryWithSubSelect.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testQuantifiedComparisonWithSomeWorks() {
    var result = test.query.SampleQueryWithQuantifiedSomeComparison.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testQuantifiedComparisonWithAllWorks() {
    var result = test.query.SampleQueryWithQuantifiedAllComparison.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testBasicVariableWorks() {
    var result = test.query.SampleComparisonQueryWithVar.select( "1/1/2001".toDate() )
    Assert.assertEquals(1, result.Count)
    result = test.query.SampleComparisonQueryWithVar.select( "1/1/2101".toDate() )
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testBasicFieldSelectionWorks() {
    var result = test.query.SampleQueryWithSpecificCols.select()
    Assert.assertEquals(sqlDate("4/22/2009"), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testBasicJoinWorks() {
    var result = test.query.SampleJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(sqlDate("4/22/2009"), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
  }

  @Test
  function testBasicInnerJoinWorks() {
    var result = test.query.SampleInnerJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(sqlDate("4/22/2009"), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
  }

  @Test
  function testBasicLeftOuterJoinWorks() {
    var result = test.query.SampleLeftOuterJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(sqlDate("4/22/2009"), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
  }

  @Test
  function testBasicRightOuterJoinWorks() {
    var result = test.query.SampleRightOuterJoinQuery.select("First")
    Assert.assertEquals(2, result.Count)
    Assert.assertTrue( result.hasMatch( \ f -> f.Misc == "misc" ) )
    Assert.assertTrue( result.hasMatch( \ f -> f.Date == null and f.Misc == null ) )
  }

  @Test @Ignore("H2 does not implement FULL OUTER JOIN" )
  function testBasicFullOuterJoinWorks() {
    var result = test.query.SampleFullOuterJoinQuery.select()
    Assert.assertEquals(2, result.Count)
  }

  @Test
  function testBasicOrderByWorks() {
    var result = test.query.SampleSimpleOrderByQuery.select()
    Assert.assertEquals(3, result.Count)
    Assert.assertEquals(new ArrayList(){1, 2, 3}, result.map( \ elt -> elt.Number ) )
  }

  @Test
  function testBasicOrderDescByWorks() {
    var result = test.query.SampleSimpleOrderByDescQuery.select()
    Assert.assertEquals(3, result.Count)
    Assert.assertEquals(new ArrayList(){3, 2, 1}, result.map( \ elt -> elt.Number ) )
  }

  @Test
  function testBasicOrderByWithWhereWorks() {
    var result = test.query.SampleSimpleOrderByWithWhereQuery.select("bad")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleSimpleOrderByWithWhereQuery.select("a")
    Assert.assertEquals(3, result.Count)
    Assert.assertEquals(new ArrayList(){1, 2, 3}, result.map( \ elt -> elt.Number ) )
  }

  @Test
  function testBasicOrderByWithMultipleColsWorks() {
    var result = test.query.SampleSimpleOrderByMultipleColsQuery.select()
    Assert.assertEquals(3, result.Count)
    Assert.assertEquals(new ArrayList(){"z", "g", "a"}, result.map( \ elt -> elt.Str ) )
  }

  @Test
  function testBasicGroupByWorks() {
    var result = test.query.SampleGroupByQuery.select()
    Assert.assertEquals(2, result.Count)
  }

  @Test
  function testBasicGroupByWithCountWorks() {
    var result = test.query.SampleGroupByCountQuery.select()
    Assert.assertEquals(2, result.Count)
    Assert.assertTrue( result.hasMatch( \ r -> r.Str2 == "a" and r.Cnt == 2L ))
    Assert.assertTrue( result.hasMatch( \ r -> r.Str2 == "b" and r.Cnt == 1L ))
  }

  @Test
  function testBasicFunctionCallInSelectListWorks() {
    var result = test.query.SampleFunctionCallInSelectListQuery.select()
    Assert.assertEquals(3, result.Count)
    Assert.assertEquals(new ArrayList(){"Wednesday", "Tuesday", "Sunday"},
                        result.map( \ r -> r.Day ))
  }

  @Test
  function testBasicFunctionCallInWhereClauseWorks() {
    var result = test.query.SampleFunctionCallInWhereQuery.select("Tuesday")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(2, result.first().Number)
  }

  @Test
  function testExistsSelectWorks() {
    var result = test.query.SampleExistsQuery.select("blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleExistsQuery.select("misc")
    Assert.assertEquals(1, result.Count)
  }

  @Test @Ignore("H2 does not implement UNIQUE" )
  function testUniqueSelectWorks() {
    var result = test.query.SampleUniqueQuery.select("blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleUniqueQuery.select("misc")
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testLikeSelectWorks() {
    var result = test.query.SampleLikeQuery.select("blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleLikeQuery.select("misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleLikeQuery.select("mi%")
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testLikeSelectWithConcatWorks() {
    var result = test.query.SampleLikeWithConcatQuery.select("blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleLikeWithConcatQuery.select("misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleLikeWithConcatQuery.select("mi")
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testAdd() {
    var result = test.query.SampleAddQuery.select(0)
    Assert.assertEquals(2, result.Count)

    result = test.query.SampleAddQuery.select(1)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleAddQuery.select(2)
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testSubtract() {
    var result = test.query.SampleSubtractQuery.select(3)
    Assert.assertEquals(2, result.Count)

    result = test.query.SampleSubtractQuery.select(2)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleSubtractQuery.select(1)
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testMod() {
    var result = test.query.SampleModQuery.select(1)
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleModQuery.select(2)
    Assert.assertEquals(2, result.Count)

    result = test.query.SampleModQuery.select(3)
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testAbs() {
    var result = test.query.SampleAbsQuery.select(1)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleAbsQuery.select(2)
    Assert.assertEquals(2, result.Count)

    result = test.query.SampleAbsQuery.select(3)
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testUpper() {
    var result = test.query.SampleUpperQuery.select()
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testOptionalParameter() {
    var result = test.query.SampleOptionalQuery.select(null)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalQuery.select(:misc=null)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalQuery.select()
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalQuery.select("misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalQuery.select("bar")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testOptionalOrParameter() {
    var result = test.query.SampleOptionalOrQuery.select()
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1=null)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc", :misc2="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc2="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc", :misc2="blah")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc2="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc", :misc3="blah")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="blah", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="misc", :misc3="blah")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc2="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="misc", :misc2="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc2="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc2="blah", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalOrQuery.select(:misc1="blah", :misc2="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testOptionalAndParameter() {
    var result = test.query.SampleOptionalAndQuery.select()
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1=null)
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc", :misc2="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc2="misc")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc", :misc2="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc2="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc3="misc")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="blah", :misc3="misc")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="misc", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc2="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="misc", :misc2="misc", :misc3="misc")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc2="misc", :misc3="misc")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc2="blah", :misc3="misc")
    Assert.assertEquals(0, result.Count)

    result = test.query.SampleOptionalAndQuery.select(:misc1="blah", :misc2="blah", :misc3="blah")
    Assert.assertEquals(0, result.Count)
  }
}
