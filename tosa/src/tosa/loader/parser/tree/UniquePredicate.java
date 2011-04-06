package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class UniquePredicate extends SQLParsedElement {
  private SQLParsedElement _select;

  public UniquePredicate(Token start, SQLParsedElement select) {
    super(start, select);
    _select = select;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" UNIQUE ");
    _select.toSQL(prettyPrint, indent, sb, values);
  }
}
