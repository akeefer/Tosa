package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 11:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharacterTypeAttribute extends SQLParsedElement {

  public enum Attribute {
    ASCII, UNICODE, BINARY
  }

  private Attribute _attribute;

  public CharacterTypeAttribute(Token token, Attribute attribute) {
    super(token);
    _attribute = attribute;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }
}
