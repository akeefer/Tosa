package tosa.loader

uses tosa.api.IDatabase

/**
 * This class effectively serves as a static mix-in for the DatabaseAccessType types.  Each method in here
 * needs to have its first argument be an IDatabase object.  Getter/setter pairs on here will be turned
 * into properties on the Database access types, while other methods will appear like static methods.
 * Every function on this class needs to be static.
 */
class DatabaseAccessTypeDelegate {

  /**
   * The jdbc url for this database
   */
  static function getJdbcUrl(db : IDatabase) : String {
    return db.JdbcUrl
  }

  /**
   * The jdbc url for this database
   */
  static function setJdbcUrl(db : IDatabase, url : String) {
    db.JdbcUrl = url
  }

  /**
   * Creates the tables for this database, specific by executing the DDL statements.
   */
  static function createTables(db : IDatabase) {
    db.createTables()
  }

  /**
   * Drops all tables in this database.
   */
  static function dropTables(db : IDatabase) {
    db.dropTables()

  }

}