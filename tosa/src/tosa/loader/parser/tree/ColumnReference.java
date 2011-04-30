package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.Token;

import java.util.Map;

public class ColumnReference extends SQLParsedElement {

  private Token _column;
  private Token _table;
  private TableData _tableData;
  private ColumnData _columnData;

  @Override
  public void verify(DBData dbData) {
    super.verify(dbData);
    if(_columnData == null) {
      addParseError(new SQLParseError(_column, "Unable to resolve column " + _column.getValue()));
    }
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    if (dbData != null) {
      if (_table != null) {
        _tableData = dbData.getTable(_table.getValue());
      } else {
        _tableData = dbData.getTable(getRootElement().getPrimaryTableName());
      }
      if (_tableData != null) {
        _columnData = _tableData.getColumn(_column.getValue());
      }
    }
    if (_columnData != null) {
      setType(_columnData.getColumnType());
    }
  }

  public ColumnReference(Token column) {
    super(column);
    _column = column;
  }

  public ColumnReference(Token table, Token column) {
    super(table, column);
    _table = table;
    _column = column;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append('"');
    if (_table != null) {
      sb.append(_table.getValue());
      sb.append('"');
      sb.append(".");
      sb.append('"');
    }
    sb.append(_column.getValue());
    sb.append('"');
  }

  public String getName() {
    return _column.getValue();
  }
}
