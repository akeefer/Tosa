package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 11:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharacterSetExpression extends SQLParsedElement {

  private Token _charSetName;

  public CharacterSetExpression(Token start, Token end, Token charSetName) {
    super(start, end);
    _charSetName = charSetName;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }

  public Token getCharSetName() {
    return _charSetName;
  }
}
