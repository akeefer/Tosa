package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 6:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyBlockSizeIndexOption extends SQLParsedElement {
  private Token _value;
  public KeyBlockSizeIndexOption(Token start, Token end) {
    super(start, end);
    _value = end;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }
}
