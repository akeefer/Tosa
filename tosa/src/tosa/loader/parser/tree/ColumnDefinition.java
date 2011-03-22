package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnDefinition extends SQLParsedElement {

  private Token _name;
  private ColumnDataType _columnDataType;
  private List<SQLParsedElement> _columnOptions;

  public ColumnDefinition(Token start, Token end, Token name, ColumnDataType columnDataType, List<SQLParsedElement> columnOptions) {
    super(start, end, collectChildren(columnDataType, columnOptions));
    _name = name;
    _columnDataType = columnDataType;
    _columnOptions = columnOptions;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }

  public Token getName() {
    return _name;
  }

  public ColumnDataType getColumnDataType() {
    return _columnDataType;
  }
}
