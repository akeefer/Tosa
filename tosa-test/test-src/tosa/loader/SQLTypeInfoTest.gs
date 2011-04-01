package tosa.loader

uses java.io.*
uses java.lang.*
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
    var bar = new Bar(){:Date = new java.sql.Date(new java.util.Date("4/22/2009").Time), :Misc = "misc"}
    bar.update()
    var foo = new Foo(){:Bar = bar, :FirstName="First", :LastName="Bar"}
    foo.update()
    var foo2 = new Foo(){:FirstName="First2", :LastName="Bar2"}
    foo2.update()
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
  function testBasicBetweenComparisonWorks() {
    var result = test.query.SampleBetweenQuery.select("2001-1-1", "2101-1-1")
    Assert.assertEquals(1, result.Count)

    result = test.query.SampleBetweenQuery.select("2101-1-1", "2101-1-1")
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
  function testBasicVariableWorks() {
    var result = test.query.SampleComparisonQueryWithVar.select( "2001-1-1" )
    Assert.assertEquals(1, result.Count)
    result = test.query.SampleComparisonQueryWithVar.select( "2101-1-1" )
    Assert.assertEquals(0, result.Count)
  }

  @Test
  function testBasicFieldSelectionWorks() {
    var result = test.query.SampleQueryWithSpecificCols.select()
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
    Assert.assertEquals(1, result.Count)
  }

  @Test
  function testBasicJoinWorks() {
    var result = test.query.SampleJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
  }

  @Test
  function testBasicInnerJoinWorks() {
    var result = test.query.SampleInnerJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
  }

  @Test
  function testBasicLeftOuterJoinWorks() {
    var result = test.query.SampleLeftOuterJoinQuery.select("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
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
  function testBasicJoinAsStructWorks() {
    var result = test.query.SampleJoinQuery.selectAsStruct("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
    Assert.assertEquals("First", result.first().FirstName)
    Assert.assertEquals("Bar", result.first().LastName)
  }

  @Test
  function testBasicInnerJoinAsStructWorks() {
    var result = test.query.SampleInnerJoinQuery.selectAsStruct("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
    Assert.assertEquals("First", result.first().FirstName)
    Assert.assertEquals("Bar", result.first().LastName)
  }

  @Test
  function testBasicLeftOuterJoinAsStructWorks() {
    var result = test.query.SampleLeftOuterJoinQuery.selectAsStruct("First")
    Assert.assertEquals(1, result.Count)
    Assert.assertEquals(new java.sql.Date(new java.util.Date("4/22/2009").Time), result.first().Date)
    Assert.assertEquals("misc", result.first().Misc)
    Assert.assertEquals("First", result.first().FirstName)
    Assert.assertEquals("Bar", result.first().LastName)
  }

  @Test
  function testBasicRightOuterJoinAsStructWorks() {
    var result = test.query.SampleRightOuterJoinQuery.selectAsStruct("First")
    Assert.assertEquals(2, result.Count)
    Assert.assertTrue( result.hasMatch( \ f -> f.Misc == "misc" and f.FirstName == "First" ) )
    Assert.assertTrue( result.hasMatch( \ f -> f.Date == null and f.Misc == null and f.FirstName == "First2") )
  }

  @Test @Ignore("H2 does not implement FULL OUTER JOIN" )
  function testBasicFullOuterJoinAsStructWorks() {
    var result = test.query.SampleFullOuterJoinQuery.selectAsStruct()
    Assert.assertEquals(2, result.Count)
  }

}
