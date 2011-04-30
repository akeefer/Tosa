package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 11:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnLengthExpression extends SQLParsedElement {

  private Token _length;
  private Token _decimals;

  public ColumnLengthExpression(Token start, Token end, Token length, Token decimals) {
    super(start, end);
    _length = length;
    _decimals = decimals;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }

  public Token getLength() {
    return _length;
  }

  public Token getDecimals() {
    return _decimals;
  }
}
