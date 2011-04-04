package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 9:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTableStatement extends SQLParsedElement {

  private Token _tableName;

  public CreateTableStatement(Token first, Token last, Token tableName, List<SQLParsedElement> children) {
    super(first, children, last);
    _tableName = tableName;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK

  }

  public Token getTableName() {
    return _tableName;
  }

  public List<ColumnDefinition> getColumnDefinitions() {
    // TODO - AHK - It would be ideal to use a filtered list
    List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
    for (SQLParsedElement element : getChildren()) {
      if (element instanceof ColumnDefinition) {
        columns.add((ColumnDefinition) element);
      }
    }
    return columns;
  }
}
