package tosa.db.execution;

import gw.fs.IFile;
import tosa.api.IDBUpgrader;
import tosa.dbmd.DatabaseImpl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/18/11
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBUpgraderImpl implements IDBUpgrader {

  private DatabaseImpl _database;

  public DBUpgraderImpl(DatabaseImpl database) {
    _database = database;
  }

  @Override
  public void createTables() {
    try {
      Connection connection = _database.getConnection().connect();
      IFile createTableFile = _database.getDdlFile();
      String statements = readFile(createTableFile);
      connection.createStatement().executeUpdate(statements);
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO - AHK - Is there some shared utility we can use here?

  private String readFile(IFile file) {
    try {
      return readFileWithoutHandlingExceptions(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readFileWithoutHandlingExceptions(IFile file) throws IOException {
    byte[] result = new byte[0];
    InputStream inputStream = file.openInputStream();
    try {
      byte[] buffer = new byte[4196];
      while (true) {
        int numRead = inputStream.read(buffer);
        if (numRead != -1) {
          byte[] newResult = new byte[result.length + numRead];
          System.arraycopy(result, 0, newResult, 0, result.length);
          System.arraycopy(buffer, 0, newResult, result.length, numRead);
          result = newResult;
        } else {
          break;
        }
      }
    } finally {
      inputStream.close();
    }
    // TODO - AHK - Verify the charset somehow?
    return new String(result);
  }
}
