package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumericDataTypeModifier extends SQLParsedElement {
  public enum Type {
    UNSIGNED, SIGNED, ZEROFILL
  }

  private Type _type;

  public NumericDataTypeModifier(Token token, Type type) {
    super(token);
    _type = type;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK
  }

  public Type getType() {
    return _type;
  }
}
