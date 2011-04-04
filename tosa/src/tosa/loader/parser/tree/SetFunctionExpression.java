package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: carson
 * Date: 4/4/11
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetFunctionExpression extends SQLParsedElement {
  private Token _op;
  private SQLParsedElement _value;

  public SetFunctionExpression(Token first, SQLParsedElement value, Token last) {
    super(first, value, last);
    _op = first;
    _value = value;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_op.getValue());
    sb.append("(");
    _value.toSQL(prettyPrint, indent, sb, values);
    sb.append(")");
  }
}
