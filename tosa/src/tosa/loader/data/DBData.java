package tosa.loader.data;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBData {
  // TODO - AHK - Additional metadata about the database, such as the type
  private final List<TableData> _tables;
  private String _connectionString;

  public DBData(List<TableData> tables) {
    // TODO - AHK - Should copy and wrap in an unmodifiable list
    _tables = tables;
  }

  // TODO - AHK - Should this be settable?
  public String getConnectionString() {
    return _connectionString;
  }

  public void setConnectionString(String connectionString) {
    _connectionString = connectionString;
  }

  public List<TableData> getTables() {
    return _tables;
  }
}
