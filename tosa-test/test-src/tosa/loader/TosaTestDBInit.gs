package tosa.loader
uses gw.lang.reflect.TypeSystem

class TosaTestDBInit {
  
  static function createDatabase() {    
    var dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader)
    var database = dbTypeLoader.getTypeDataForNamespace( "test.testdb" )
    database.DBUpgrader.createTables()
  }

}