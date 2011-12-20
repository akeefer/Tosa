package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexColumnName extends SQLParsedElement {

  public enum IndexColumnSortDirection {
    ASC, DESC
  }

  private Token _name;
  private Token _length;
  private IndexColumnSortDirection _sortDirection;

  public IndexColumnName(Token start, Token end, Token name, Token length, IndexColumnSortDirection sortDirection) {
    super(start, end);
    _name = name;
    _length = length;
    _sortDirection = sortDirection;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }
}
