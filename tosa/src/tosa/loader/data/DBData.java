package tosa.loader.data;

import gw.fs.IFile;

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
  private final IFile _ddlFile;

  public DBData(List<TableData> tables, String connectionString, IFile ddlFile) {
    _tables = Collections.unmodifiableList(new ArrayList<TableData>(tables));
    _connectionString = connectionString;
    _ddlFile = ddlFile;
  }

  public String getConnectionString() {
    return _connectionString;
  }

  public List<TableData> getTables() {
    return _tables;
  }

  public IFile getDdlFile() {
    return _ddlFile;
  }
}
