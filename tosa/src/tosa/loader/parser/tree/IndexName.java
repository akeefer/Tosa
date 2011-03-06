package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexName extends SQLParsedElement {

  private Token _name;

  public IndexName(Token name) {
    super(name);
    _name = name;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }
}
