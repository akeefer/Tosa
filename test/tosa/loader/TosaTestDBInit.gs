package tosa.loader
uses gw.lang.reflect.TypeSystem
uses tosa.api.DBLocator


class TosaTestDBInit {
  
  static function createDatabase() {    
    var dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader)
    var database = DBLocator.getDatabase( "test.testdb" )
    database.JdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2"
    database.DBUpgrader.recreateTables()
  }

}