package tosa.loader.parser.tree;

public class SQLAndExpression extends SQLParsedElement {
  private SQLParsedElement _lhs;
  private SQLParsedElement _rhs;

  public SQLAndExpression(SQLParsedElement lhs, SQLParsedElement rhs) {
    super(lhs.firstToken(), rhs.lastToken(), lhs, rhs);
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    _lhs.toSQL(prettyPrint, indent, sb);
    sb.append(" AND ");
    _rhs.toSQL(prettyPrint, indent, sb);
  }
}
