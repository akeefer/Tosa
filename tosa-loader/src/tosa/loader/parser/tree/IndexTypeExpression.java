package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexTypeExpression extends SQLParsedElement {
  public IndexTypeExpression(Token token, SQLParsedElement... children) {
    super(token, children);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }
}
