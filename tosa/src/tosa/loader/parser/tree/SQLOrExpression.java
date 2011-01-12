package tosa.loader.parser.tree;

public class SQLOrExpression extends SQLParsedElement {
  private SQLParsedElement _lhs;
  private SQLParsedElement _rhs;

  public SQLOrExpression(SQLParsedElement lhs, SQLParsedElement rhs) {
    super(lhs.firstToken(), rhs.lastToken(), lhs, rhs);
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    _lhs.toSQL(prettyPrint, indent, sb);
    sb.append(" OR ");
    _rhs.toSQL(prettyPrint, indent, sb);
  }
}
