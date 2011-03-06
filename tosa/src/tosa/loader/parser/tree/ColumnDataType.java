package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnDataType extends SQLParsedElement {
  public ColumnDataType(Token token, SQLParsedElement... children) {
    super(token, children);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }
}
