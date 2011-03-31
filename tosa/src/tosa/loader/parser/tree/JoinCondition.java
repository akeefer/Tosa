package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class JoinCondition extends SQLParsedElement {
  private SQLParsedElement _condition;

  public JoinCondition(Token start, SQLParsedElement condition) {
    super(start, condition);
    _condition = condition;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" ON ");
    _condition.toSQL(prettyPrint, indent, sb, values);
  }
}
