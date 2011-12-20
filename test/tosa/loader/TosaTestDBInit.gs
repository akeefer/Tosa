package tosa.loader
uses gw.lang.reflect.TypeSystem
uses tosa.api.DBLocator


class TosaTestDBInit {
  
  static function createDatabase() {    
    var dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader)
    var database = DBLocator.getDatabase( "test.testdb" )
    database.DBUpgrader.recreateTables()
  }

}