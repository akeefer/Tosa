package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/11/11
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultValueExpression extends SQLParsedElement {
  private Token _value;

  public DefaultValueExpression(Token start, Token end, Token value) {
    super(start, end);
    _value = value;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO
  }
}
