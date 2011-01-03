package tosa.loader.data;

import java.util.ArrayList;
import java.util.Collections;
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
  private final String _connectionString;

  public DBData(List<TableData> tables, String connectionString) {
    _tables = Collections.unmodifiableList(new ArrayList<TableData>(tables));
    _connectionString = connectionString;
  }

  public String getConnectionString() {
    return _connectionString;
  }

  public List<TableData> getTables() {
    return _tables;
  }
}
