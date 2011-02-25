package tosa.loader

uses java.io.*
uses java.lang.*
uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.features.PropertyReference
uses org.junit.Assert
uses org.junit.Before
uses org.junit.BeforeClass
uses org.junit.Test
uses test.testdb.Bar
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
  }

  @Before
  function beforeTestMethod() {
    deleteAllData()
    importSampleData()
  }
/*
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
*/
  @Test
  function testBasicColumnComparisonWorks() {
    var result = test.query.SampleComparisonQuery.select()
    Assert.assertEquals(1, result.Count)
  }

}
