package tosa.api;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An IDBConnection represents a connection to the database, with the connection defined in the .dbc file
 * associated with a particular database.  The connection can be used to access a raw JDBC connection,
 * or it can be used for transactional locking.
 *
 * ${License}
 */
public interface IDBConnection {

  /**
   * Opens a connection to the database, or returns the existing thread-local transaction created by a previous
   * call to startTransaction().  The Connection object will be pooled.  Currently DBCP is used for connection
   * pooling, but the pooling mechanism and parameters will likely be more configurable in the future.
   *
   * @return a Connection to the database
   * @throws SQLException any SQLException thrown in the course of opening the connection
   */
  Connection connect() throws SQLException;

  // TODO - AHK - Should these methods throw, or should they force-throw and not declare exceptions?
  /**
   * Sets up a new thread-local connection, which will be returned by the connect() method
   * until endTransaction() is called.  The thread-local connection will remain open until
   * endTransaction() is called, regardless of calls to Connection.close().  This method should *always* be
   * used in a try/finally block that calls endTransaction().
   *
   * This method will throw an IllegalStateException if a thread-local transaction has already been created.
   *
   * @throws SQLException any SQLException thrown by the underlying connection
   */
  void startTransaction() throws SQLException;

  /**
   * Commits the thread-local connection.  This method will throw an IllegalStateException if a thread-local transaction
   * has not already been created.
   *
   * @throws SQLException any SQLException thrown by the underlying connection
   */
  void commitTransaction() throws SQLException;

  /**
   * Rolls back and closes the currently open thread-local transaction.  This method will throw an IllegalStateException
   * if a thread-local transaction has not already been created.
   *
   * @throws SQLException any SQLException thrown by the underlying connection
   */
  void endTransaction() throws SQLException;
}
