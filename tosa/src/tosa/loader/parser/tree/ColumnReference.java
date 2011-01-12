package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class ColumnReference extends SQLParsedElement {
  private Token _column;
  private Token _table;

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
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    if (_table != null) {
      sb.append(_table.getValue()).append(".");
    }
    sb.append(_column.getValue());
  }
}
