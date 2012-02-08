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
  private final String _possiblyQuotedName;
  private final List<ColumnData> _columns;
  private final CreateTableStatement _originalDefinition;

  public TableData(String possiblyQuotedName, List<ColumnData> columns, CreateTableStatement originalDefinition) {
    _possiblyQuotedName = possiblyQuotedName;
    _name = stripQuotes(possiblyQuotedName);
    _columns = columns;
    _originalDefinition = originalDefinition;
  }

  public String getName() {
    return _name;
  }

  public String getPossiblyQuotedName() {
    return _possiblyQuotedName;
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

  private String stripQuotes(String str) {
    if (str.startsWith("\"")) {
      str = str.substring(1);
    }
    if (str.endsWith("\"")) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
}
