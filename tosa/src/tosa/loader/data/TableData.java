package tosa.loader.data;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableData {
  private final String _name;
  private final List<ColumnData> _columns;

  public TableData(String name, List<ColumnData> columns) {
    _name = name;
    _columns = columns;
  }

  public String getName() {
    return _name;
  }

  public List<ColumnData> getColumns() {
    return _columns;
  }

  public ColumnData getColumn(String name) {
    for (ColumnData column : _columns) {
      if (column.getName().equals(name)) {
        return column;
      }
    }
    return null;
  }
}
