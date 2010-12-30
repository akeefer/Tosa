package tosa.loader.data;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnData {
  private String _name;
  private ColumnType _columnType;

  public ColumnData(String name, ColumnType columnType) {
    _name = name;
    _columnType = columnType;
  }

  public String getName() {
    return _name;
  }

  public ColumnType getColumnType() {
    return _columnType;
  }
}
