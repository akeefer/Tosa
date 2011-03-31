package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/11/11
 * Time: 10:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnOptionExpression extends SQLParsedElement {

  public enum ColumnOptionType {
    NOT_NULL, NULL, AUTO_INCREMENT, UNIQUE, UNIQUE_KEY, PRIMARY_KEY,
    COLUMN_FORMAT_FIXED, COLUMN_FORMAT_DYNAMIC, COLUMN_FORMAT_DEFAULT,
    STORAGE_DISK, STORAGE_MEMORY, STORAGE_DEFAULT
  }

  private ColumnOptionType _type;

  public ColumnOptionExpression(Token start, Token end, ColumnOptionType type) {
    super(start, end);
    _type = type;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }
}
