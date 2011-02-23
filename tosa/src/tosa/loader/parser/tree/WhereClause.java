package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class WhereClause extends SQLParsedElement {
  private SQLParsedElement _conditions;

  public WhereClause(Token start, SQLParsedElement conditions) {
    super(start, conditions.lastToken(), conditions);
    _conditions = conditions;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    pp(prettyPrint, indent, "WHERE ", sb);
    _conditions.toSQL(prettyPrint, indent, sb);
  }
}
