package tosa.loader.data;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnData {
  private String _name;
  private DBColumnTypeImpl _columnType;

  public ColumnData(String name, DBColumnTypeImpl columnType) {
    _name = name;
    _columnType = columnType;

    // TODO - AHK - Total hack
    if (_columnType == null) {
      _columnType = new DBColumnTypeImpl("PlaceHolder", "place holder", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR);
    }
  }

  public String getName() {
    return _name;
  }

  public DBColumnTypeImpl getColumnType() {
    return _columnType;
  }
}
