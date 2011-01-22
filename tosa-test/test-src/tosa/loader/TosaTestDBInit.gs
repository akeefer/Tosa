package tosa.loader
uses gw.lang.reflect.TypeSystem

class TosaTestDBInit {
  
  static function createDatabase() {    
    var dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader)
    var sourceRoot = dbTypeLoader.Module.ResourceAccess.Roots.firstWhere( \ r -> r.Parent.Name == "tosa-test" )
    var ddlFile = sourceRoot.file( "test/testdb.ddl" )
    
    print(">>> Creating database from DDL statements in file " + ddlFile.Path.FileSystemPathString)
    var database = dbTypeLoader.getTypeDataForNamespace( "test.testdb" )
    var connection = database.Connection.connect()
    connection.createStatement().executeUpdate( ddlFile.toJavaFile().read() )
    connection.close()
  }

}