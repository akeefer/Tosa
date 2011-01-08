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

class DBTypeInfoTest {

  static final var NUM_FOOS = 1

  @BeforeClass
  static function beforeTestClass() {
    print("*** Before class")
    TosaTestDBInit.createDatabase()
  }

  private function deleteAllData() {
    clearTable("SelfJoins_join_Baz_Baz")
    clearTable("Relatives_join_Bar_Baz")
    clearTable("join_Foo_Baz")
    clearTable("Baz")
    clearTable("Foo")
    clearTable("SortPage")
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

  private var _barId : long
  private var _fooId : long
  private var _bazId : long
  private var _sortPageId : long

  private function importSampleData() {
    var bar = new Bar(){:Date = new java.sql.Date(new java.util.Date("4/22/2009").Time), :Misc = "misc"}
    bar.update()
    _barId = bar.id
    new SortPage(){:Number = 1}.update()
    new SortPage(){:Number = 2}.update()
    new SortPage(){:Number = 3}.update()
    new SortPage(){:Number = 4}.update()
    new SortPage(){:Number = 5}.update()
    new SortPage(){:Number = 1}.update()
    new SortPage(){:Number = 2}.update()
    new SortPage(){:Number = 3}.update()
    new SortPage(){:Number = 4}.update()
    new SortPage(){:Number = 5}.update()
    new SortPage(){:Number = 1}.update()
    new SortPage(){:Number = 2}.update()
    new SortPage(){:Number = 3}.update()
    new SortPage(){:Number = 4}.update()
    new SortPage(){:Number = 5}.update()
    var sortPage16 = new SortPage(){:Number = 1}
    sortPage16.update()
    _sortPageId = sortPage16.id
    new SortPage(){:Number = 2}.update()
    new SortPage(){:Number = 3}.update()
    new SortPage(){:Number = 4}.update()
    new SortPage(){:Number = 5}.update()


    var foo =new Foo(){:FirstName = "Charlie", :LastName="Brown", :Bar=bar, :Address="1234 Main St.\nCentreville, KS 12345", :Named = sortPage16}
    foo.update()
    _fooId = foo.id

    var baz = new Baz(){:Text = "First"}
    baz.update()
    _bazId = baz.id

    foo.Bazs.add(baz)
    foo.update()

    bar.Relatives.add(baz)
    bar.update()
  }

  @Before
  function beforeTestMethod() {
    deleteAllData()
    importSampleData()
  }

  @Test
  function testTypesCreated() {
      var types = gw.lang.reflect.TypeSystem.getAllTypeNames().where(\ c -> c.toString().startsWith("test.testdb."))

      // TODO H2 returns many tables from INFORMATION_SCHEMA - filter out?
//      Assert.assertEquals("Too many types:\n${types.join("\n")}", 3, types.Count)

      Assert.assertTrue(types.contains("test.testdb.Foo"))
      Assert.assertTrue(types.contains("test.testdb.Bar"))
      Assert.assertTrue(types.contains("test.testdb.Baz"))
      Assert.assertTrue(types.contains("test.testdb.Transaction"))
  }

  @Test
  function testPropertiesCreated() {
      print(">>>>Test 1")
      var typeinfo = test.testdb.Foo.Type.TypeInfo
      Assert.assertEquals(8, typeinfo.Properties.Count)

      print(">>>>Test 2")
      var idProp = typeinfo.getProperty("id")
      Assert.assertNotNull(idProp)
      Assert.assertEquals(Integer, idProp.FeatureType)

      print(">>>>Test 3")
      var firstNameProp = typeinfo.getProperty("FirstName")
      Assert.assertNotNull(firstNameProp)
      Assert.assertEquals(String, firstNameProp.FeatureType)

      print(">>>>Test 4")
      var lastNameProp = typeinfo.getProperty("LastName")
      Assert.assertNotNull(lastNameProp)
      Assert.assertEquals(String, lastNameProp.FeatureType)

      print(">>>>Test 5")
      var fkProp = typeinfo.getProperty("Bar")
      Assert.assertNotNull(fkProp)
      Assert.assertEquals(test.testdb.Bar, fkProp.FeatureType)

      print(">>>>Test 6")
      var namedFkProp = typeinfo.getProperty("Named")
      Assert.assertNotNull(namedFkProp)
      Assert.assertEquals(test.testdb.SortPage, namedFkProp.FeatureType)

      print(">>>>Test 7")
      var textProp = typeinfo.getProperty("Address")
      Assert.assertNotNull(textProp)
      Assert.assertEquals(String, textProp.FeatureType)

      print(">>>>Test 8")
      var joinProp = typeInfo.getProperty("Bazs")
      Assert.assertNotNull(joinProp)
      Assert.assertEquals(List<test.testdb.Baz>, joinProp.FeatureType)

      print(">>>>Test 9")
      typeinfo = test.testdb.Bar.Type.TypeInfo
      Assert.assertEquals(6, typeinfo.Properties.Count)

      print(">>>>Test 10")
      idProp = typeinfo.getProperty("id")
      Assert.assertNotNull(idProp)
      Assert.assertEquals(Integer, idProp.FeatureType)

      print(">>>>Test 11")
      var miscProp = typeinfo.getProperty("Misc")
      Assert.assertNotNull(miscProp)
      Assert.assertEquals(String, miscProp.FeatureType)

      print(">>>>Test 12")
      var dateProp = typeinfo.getProperty("Date")
      Assert.assertNotNull(dateProp)
      Assert.assertEquals(java.sql.Date, dateProp.FeatureType)

      print(">>>>Test 13")
      var arrayProp = typeinfo.getProperty("Foos")
      Assert.assertNotNull(arrayProp)
      Assert.assertEquals(List<test.testdb.Foo>, arrayProp.FeatureType)
      Assert.assertFalse(arrayProp.Writable)

      print(">>>>Test 14")
      joinProp = typeInfo.getProperty("Relatives")
      Assert.assertNotNull(joinProp)
      Assert.assertEquals(List<test.testdb.Baz>, joinProp.FeatureType)

      print(">>>>Test passed")
  }

  @Test
  function testBasicMethodsCreated() {
      var typeinfo : gw.lang.reflect.ITypeInfo = test.testdb.Foo.Type.TypeInfo

      var getMethod = typeinfo.getMethod("fromID", {long})
      Assert.assertNotNull(getMethod)
      Assert.assertTrue(getMethod.Static)
      Assert.assertEquals(test.testdb.Foo, getMethod.ReturnType)

      var updateMethod = typeinfo.getMethod("update", {})
      Assert.assertNotNull(updateMethod)

      var deleteMethod = typeinfo.getMethod("delete", {})
      Assert.assertNotNull(deleteMethod)

      var findBySqlMethod = typeinfo.getMethod("findWithSql", {String})
      Assert.assertNotNull(findBySqlMethod)
      Assert.assertTrue(findBySqlMethod.Static)
      Assert.assertEquals(List<test.testdb.Foo>, findBySqlMethod.ReturnType)

      var findMethod = typeinfo.getMethod("find", {test.testdb.Foo})
      Assert.assertNotNull(findMethod)
      Assert.assertTrue(findMethod.Static)
      Assert.assertEquals(List<test.testdb.Foo>, findMethod.ReturnType)

      var countBySqlMethod = typeinfo.getMethod("countWithSql", {String})
      Assert.assertNotNull(countBySqlMethod)
      Assert.assertTrue(countBySqlMethod.Static)
      Assert.assertEquals(int, countBySqlMethod.ReturnType)

      var countMethod = typeinfo.getMethod("count", {test.testdb.Foo})
      Assert.assertNotNull(countMethod)
      Assert.assertTrue(countMethod.Static)
      Assert.assertEquals(int, countMethod.ReturnType)

      var findSortedMethod = typeinfo.getMethod("findSorted", {test.testdb.Foo, PropertyReference, boolean})
      Assert.assertNotNull(findSortedMethod)
      Assert.assertTrue(findSortedMethod.Static)
      Assert.assertEquals(List<test.testdb.Foo>, findSortedMethod.ReturnType)

      var findPagedMethod = typeinfo.getMethod("findPaged", {test.testdb.Foo, int, int})
      Assert.assertNotNull(findPagedMethod)
      Assert.assertTrue(findPagedMethod.Static)
      Assert.assertEquals(List<test.testdb.Foo>, findPagedMethod.ReturnType)

      var findSortedPagedMethod = typeinfo.getMethod("findSortedPaged", {test.testdb.Foo, PropertyReference, boolean, int, int})
      Assert.assertNotNull(findSortedPagedMethod)
      Assert.assertTrue(findSortedPagedMethod.Static)
      Assert.assertEquals(List<test.testdb.Foo>, findSortedPagedMethod.ReturnType)

  }

  @Test
  function testGetMethod() {
      var foo = test.testdb.Foo.fromID(_fooId)
      Assert.assertNotNull(foo)
      Assert.assertEquals("Charlie", foo.FirstName)
      Assert.assertEquals("Brown", foo.LastName)

      var noFoo = test.testdb.Foo.fromID(3582053)
      Assert.assertNull(noFoo)
  }

  @Test
  function testFindWithSqlMethod() {
      var foos = test.testdb.Foo.findWithSql("select * from \"Foo\" where \"FirstName\"='Charlie'")
      Assert.assertEquals(1, foos.Count)
      var foo = foos[0]
      Assert.assertEquals("Charlie", foo.FirstName)
      Assert.assertEquals("Brown", foo.LastName)

      var noFoo = test.testdb.Foo.findWithSql("select * from \"Foo\" where \"FirstName\"='Rupert'")
      Assert.assertEquals(0, noFoo.Count)
  }

  @Test
  function testFindWithSqlWithJoin() {
      var foos = test.testdb.Foo.findWithSql("select * from \"Foo\" inner join \"SortPage\" on \"SortPage\".\"id\" = \"Foo\".\"Named_SortPage_id\" where \"SortPage\".\"Number\" = 1")
      Assert.assertEquals(1, foos.Count)
      var foo = foos[0]
      Assert.assertEquals("Charlie", foo.FirstName)
      Assert.assertEquals("Brown", foo.LastName)
      Assert.assertEquals(_fooId, foo.id)
  }

  @Test
  function testFindWithRegularColumns() {
      var foos = test.testdb.Foo.find(new test.testdb.Foo(){:FirstName = "Charlie"})
      Assert.assertEquals(1, foos.Count)
      var foo = foos[0]
      Assert.assertEquals("Charlie", foo.FirstName)
      Assert.assertEquals("Brown", foo.LastName)

      var noFoo = test.testdb.Foo.find(new test.testdb.Foo(){:FirstName = "Rupert"})
      Assert.assertEquals(0, noFoo.Count)

      var allFoos = test.testdb.Foo.find(new test.testdb.Foo())
      Assert.assertEquals(NUM_FOOS, allFoos.Count)
      allFoos = test.testdb.Foo.find(null)
      Assert.assertEquals(NUM_FOOS, allFoos.Count)
  }

  @Test
  function testFindSorted() {
      var sorted = test.testdb.SortPage.findSorted(null, test.testdb.SortPage#Number, true)
      sorted.eachWithIndex(\s, i -> {
        Assert.assertTrue(i == 0 or s.Number >= sorted[i - 1].Number)
      })
  }

  @Test
  function testFindPaged() {
      var page1 = test.testdb.SortPage.findPaged(null, 10, 0)
      var page2 = test.testdb.SortPage.findPaged(null, 10, 10)
      Assert.assertEquals(10, page1.Count)
      Assert.assertEquals(10, page2.Count)
      page1.each(\s -> {
          Assert.assertNull(page2.firstWhere(\s2 -> s2.id == s.id))
      })
  }

  @Test
  function testFindSortedPaged() {
      var page1 = test.testdb.SortPage.findSortedPaged(null, test.testdb.SortPage#Number, true, 10, 0)
      var page2 = test.testdb.SortPage.findSortedPaged(null, test.testdb.SortPage#Number, true, 10, 10)
      Assert.assertEquals(10, page1.Count)
      Assert.assertEquals(10, page2.Count)
      page1.eachWithIndex(\s, i -> {
          Assert.assertTrue(i == 0 or s.Number >= page1[i - 1].Number)
          Assert.assertNull(page2.firstWhere(\s2 -> s2.id == s.id or s2.Number < s.Number))
      })
  }

  @Test
  function testCount() {
      Assert.assertEquals(20, test.testdb.SortPage.count(null))
      Assert.assertEquals(4, test.testdb.SortPage.count(new test.testdb.SortPage(){:Number = 1}))
  }

  @Test
  function testCountWithSql() {
      Assert.assertEquals(8, test.testdb.SortPage.countWithSql("select count(*) as count from \"SortPage\" where \"Number\" < 3"))
  }

  @Test
  function testFindWithFK() {
      var bar = test.testdb.Bar.fromID(1)
      var foos = test.testdb.Foo.find(new test.testdb.Foo(){:Bar = bar})
      Assert.assertEquals(1, foos.Count)
      var foo = foos[0]
      Assert.assertEquals("Charlie", foo.FirstName)
      Assert.assertEquals("Brown", foo.LastName)
  }

  @Test
  function testForeignKey() {
      var foo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals(_barId, foo.Bar.id)
  }

  @Test
  function testNamedForeignKey() {
      var foo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals(_sortPageId, foo.Named.id)
      Assert.assertEquals(1, foo.Named.Number)
  }

  @Test
  function testArray() {
      var bar = test.testdb.Bar.fromID(_barId)
      var array = bar.foos.map(\f -> f.id)
      Assert.assertEquals(1, array.Count)
      Assert.assertEquals(_fooId, array[0])
  }

  @Test
  function testJoinArray() {
      var foo = test.testdb.Foo.fromID(_fooId)
      var array = foo.Bazs.map(\b -> b.id)
      Assert.assertEquals(1, array.Count)
      Assert.assertEquals(_bazId, array[0])
      var baz = test.testdb.Baz.fromID(_bazId)
      var array2 = baz.Foos.map(\f -> f.id)
      Assert.assertEquals(1, array2.Count)
      Assert.assertEquals(_fooId, array2[0])
  }

  @Test
  function testNamedJoinArray() {
      var bar = test.testdb.Bar.fromID(_barId)
      var array = bar.Relatives.map(\b -> b.id)
      Assert.assertEquals(1, array.Count)
      Assert.assertEquals(_barId, array[0])
      var baz = test.testdb.Baz.fromID(_bazId)
      var array2 = baz.Relatives.map(\b -> b.id)
      Assert.assertEquals(1, array2.Count)
      Assert.assertEquals(_bazId, array2[0])
  }

  @Test
  function testDelete() {
      test.testdb.Foo.fromID(_fooId).delete()
      Assert.assertEquals(0, test.testdb.Foo.find(new test.testdb.Foo()).Count)
  }

  @Test
  function testCreateNew() {
      var newFoo = new test.testdb.Foo(){:FirstName = "Linus", :LastName = "Van Pelt"}
      newFoo.update()
      
      print(">> newFoo id is " + newFoo.id)

      Assert.assertNotNull(newFoo.id)
      Assert.assertEquals(NUM_FOOS + 1, test.testdb.Foo.find(null).Count)

      var newFooRetrieved = test.testdb.Foo.fromID(newFoo.id)
      Assert.assertEquals("Linus", newFooRetrieved.FirstName)
      Assert.assertEquals("Van Pelt", newFooRetrieved.LastName)
  }

  @Test
  function testUpdateRegularColumns() {
      print(">> Here")
      var foo = test.testdb.Foo.fromID(_fooId)
      print(">> Now here")
      foo.FirstName = "Leroy"
      foo.update()

      var retrievedFoo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals("Leroy", retrievedFoo.FirstName)
  }

  @Test
  function testUpdateTextColumn() {
      print("*** Here")
      var foo = test.testdb.Foo.fromID(_fooId)
      print("*** And here")
      foo.Address = "54321 Centre Ave.\nMiddleton, IA 52341"
      foo.update()

      var retrievedFoo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals("54321 Centre Ave.\nMiddleton, IA 52341", retrievedFoo.Address)
  }

  @Test
  function testUpdateFK() {
      var newBar = new test.testdb.Bar()
      newBar.update()

      var foo = test.testdb.Foo.fromID(_fooId)
      foo.Bar = newBar
      foo.update()

      var retrievedFoo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals(newBar, retrievedFoo.Bar)
  }

  @Test
  function testAddJoin() {
      var newBaz = new test.testdb.Baz()
      newBaz.update()
      var foo = test.testdb.Foo.fromID(_fooId)
      foo.Bazs.add(newBaz)
      Assert.assertTrue(foo.Bazs.contains(newBaz))
  }

  @Test
  function testAddAllJoin() {
      var foo = test.testdb.Foo.fromID(_fooId)
      var oldBazsCount = foo.Bazs.Count
      var newBazs : List<test.testdb.Baz> = {}
      for(i in 1..10) {
        var newBaz = new test.testdb.Baz()
        newBaz.update()
        newBazs.add(newBaz)
      }
      foo.Bazs.addAll(newBazs)
      Assert.assertEquals(newBazs.Count + oldBazsCount, foo.Bazs.Count)
      for(newBaz in newBazs) {
        Assert.assertTrue(foo.Bazs.contains(newBaz))
      }

  }

  @Test
  function testRemoveJoin() {
      var foo = test.testdb.Foo.fromID(_fooId)
      foo.Bazs.remove(test.testdb.Baz.fromID(_bazId))
      Assert.assertEquals(0, foo.Bazs.Count)
  }

  @Test
  function testAddNamedJoin() {
      var newBaz = new test.testdb.Baz()
      newBaz.update()
      var bar = test.testdb.Bar.fromID(_barId)
      bar.Relatives.add(newBaz)
      Assert.assertTrue(bar.Relatives.contains(newBaz))
  }

  @Test
  function testRemoveNamedJoin() {
      var bar = test.testdb.Bar.fromID(_barId)
      bar.Relatives.remove(test.testdb.Baz.fromID(_bazId))
      Assert.assertEquals(0, bar.Relatives.Count)
  }

  @Test
  function testSelfJoin() {
    var baz1 = new test.testdb.Baz()
    baz1.update()
    baz1 = test.testdb.Baz.fromId(baz1.id)
    var baz2 = new test.testdb.Baz()
    baz2.update()
    baz2 = test.testdb.Baz.fromId(baz2.id)
    baz1.SelfJoins.add(baz2)
    Assert.assertTrue(baz1.SelfJoins.contains(baz2))
    Assert.assertTrue(baz2.SelfJoins.Empty)
    baz1.SelfJoins.remove(baz2)
    Assert.assertTrue(baz1.SelfJoins.Empty)
    Assert.assertTrue(baz2.SelfJoins.Empty)
  }

  @Test
  function testTextColumn() {
      var foo = test.testdb.Foo.fromID(_fooId)
      Assert.assertEquals("1234 Main St.\nCentreville, KS 12345", foo.Address)
  }

  @Test
  function testNewProperty() {
      var newFoo = new test.testdb.Foo()
      Assert.assertTrue(newFoo._New)

      var oldFoo = test.testdb.Foo.fromID(_fooId)
      Assert.assertFalse(oldFoo._New)
  }

  @Test
  function testSingleQuoteEscape() {
      var foo = new test.testdb.Foo(){:FirstName = "It's-a", :LastName = "me!!"}
      foo.update()

      var retrievedFoo = test.testdb.Foo.fromID(foo.id)
      Assert.assertEquals("It's-a", retrievedFoo.FirstName)
  }

  @Test
  function testTransactionNoCommit() {
    var foo = test.testdb.Foo.fromID(_fooId)
    using(test.testdb.Transaction.Lock) {
      foo.FirstName = "not committed"
      foo.update()
    }
    Assert.assertFalse(test.testdb.Foo.fromID(_fooId).FirstName == "not committed")
  }

  @Test
  function testTransactionCommit() {
    var foo = test.testdb.Foo.fromID(_fooId)
    using(test.testdb.Transaction.Lock) {
      foo.FirstName = "committed"
      foo.update()
      test.testdb.Transaction.commit()
    }
    Assert.assertEquals("committed", test.testdb.Foo.fromID(1).FirstName)
  }

}
