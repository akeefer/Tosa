package tosa.loader

uses java.io.*
uses java.lang.*
uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.features.PropertyReference
uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test
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
  }

}
