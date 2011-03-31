package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 11:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollateExpression extends SQLParsedElement {

  private Token _collationName;

  public CollateExpression(Token start, Token end, Token collationName) {
    super(start, end);
    _collationName = collationName;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }
}
