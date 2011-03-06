package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 6:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class WithParserIndexOption extends SQLParsedElement {

  public WithParserIndexOption(Token start, Token value) {
    super(start, value);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }
}
