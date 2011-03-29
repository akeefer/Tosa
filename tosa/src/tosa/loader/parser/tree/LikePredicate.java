package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class LikePredicate extends SQLParsedElement{
  private SQLParsedElement _lhs;
  private SQLParsedElement _pattern;
  private boolean _not;

  public LikePredicate(SQLParsedElement lhs, SQLParsedElement pattern, boolean notFound) {
    super(lhs.firstToken(), pattern.lastToken(), lhs, pattern);
    _lhs = lhs;
    _pattern = pattern;
    _not = notFound;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    _lhs.toSQL(prettyPrint, indent, sb);
    if (_not) {
      sb.append(" NOT");
    }
    sb.append(" LIKE ");
    _pattern.toSQL(prettyPrint, indent, sb);
  }
}
