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
uses test.testdb.SortPage
uses test.testdb.Foo
uses test.testdb.Baz
uses gw.lang.reflect.TypeSystem

class DatabaseAccessTypeTest {

  @BeforeClass
  static function beforeTestClass() {
    TosaTestDBInit.createDatabase()
  }

  @Test
  function testDatabaseUrlPropertyGetter() {
    Assert.assertEquals("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", test.testdb.Database.Url)
  }
}
