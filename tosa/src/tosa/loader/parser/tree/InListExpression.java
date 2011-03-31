package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;

public class InListExpression extends SQLParsedElement {
  private List<SQLParsedElement> _values;

  public InListExpression(Token first, Token last, List<SQLParsedElement> values) {
    super(first, last, values);
    _values = values;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append("( ");
    for (int i = 0; i < _values.size(); i++) {
      SQLParsedElement sqlParsedElement = _values.get(i);
      if (i != 0) {
        sb.append(", ");
      }
      sqlParsedElement.toSQL(prettyPrint, indent, sb);
    }
    sb.append(" )");
  }
}
