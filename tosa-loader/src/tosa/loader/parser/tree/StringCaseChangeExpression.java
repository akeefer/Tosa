package tosa.loader.parser.tree;

import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.Map;

public class StringCaseChangeExpression extends SQLParsedElement {
  private Token _op;
  private SQLParsedElement _value;

  public StringCaseChangeExpression(Token start, SQLParsedElement value, Token end) {
    super(start, value, end);
    _op = start;
    _value = value;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    setType(DBColumnTypeImpl.STRING);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_op.getValue());
    sb.append("(");
    _value.toSQL(prettyPrint, indent, sb, values);
    sb.append(")");
  }
}
