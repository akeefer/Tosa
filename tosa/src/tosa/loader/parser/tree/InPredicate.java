package tosa.loader.parser.tree;

public class InPredicate extends SQLParsedElement{

  private SQLParsedElement _lhs;
  private SQLParsedElement _in;
  private boolean _not;

  public InPredicate(SQLParsedElement lhs, SQLParsedElement in, boolean not) {
    super(lhs.firstToken(), in.lastToken(), lhs, in);
    _lhs = lhs;
    _not = not;
    _in = in;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    _lhs.toSQL(prettyPrint, indent, sb);
    if(_not) {
      sb.append(" NOT");
    }
    sb.append(" IN ");
    _in.toSQL(prettyPrint,indent, sb);
  }

}
