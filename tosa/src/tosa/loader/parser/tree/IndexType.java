package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexType extends SQLParsedElement {

  private IndexTypeOption _type;

  public enum IndexTypeOption {
    BTREE, HASH
  }

  public IndexType(Token start, Token end, IndexTypeOption type) {
    super(start, end);
    _type = type;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }
}
