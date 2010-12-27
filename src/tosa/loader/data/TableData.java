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
  private final List<ArrayData> _arrays;
  private final List<ForeignKeyData> _foreignKeys;

  public TableData(String name, List<ColumnData> columns, List<ArrayData> arrays, List<ForeignKeyData> foreignKeys) {
    _name = name;
    _columns = columns;
    _arrays = arrays;
    _foreignKeys = foreignKeys;
  }
}
