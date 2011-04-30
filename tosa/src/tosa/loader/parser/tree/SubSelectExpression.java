package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class SubSelectExpression extends SQLParsedElement {
  private SelectStatement _subSelect;

  public SubSelectExpression(Token start, SelectStatement subSelect, Token end) {
    super(start, subSelect, end);
    _subSelect = subSelect;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" (");
    _subSelect.toSQL(prettyPrint,indent, sb, values);
    sb.append(")");
  }
}
