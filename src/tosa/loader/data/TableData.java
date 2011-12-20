package tosa.loader.data;

import tosa.loader.parser.tree.CreateTableStatement;

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
  private final CreateTableStatement _originalDefinition;

  public TableData(String name, List<ColumnData> columns, CreateTableStatement originalDefinition) {
    _name = name;
    _columns = columns;
    _originalDefinition = originalDefinition;
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

  public CreateTableStatement getOriginalDefinition() {
    return _originalDefinition;
  }
}
