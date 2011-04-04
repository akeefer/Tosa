package tosa.loader.parser.tree;

import java.util.Map;

public class InPredicate extends SQLParsedElement{

  private SQLParsedElement _lhs;
  private SQLParsedElement _in;
  private boolean _not;

  public InPredicate(SQLParsedElement lhs, SQLParsedElement in, boolean not) {
    super(lhs, in);
    _lhs = lhs;
    _not = not;
    _in = in;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _lhs.toSQL(prettyPrint, indent, sb, values);
    if(_not) {
      sb.append(" NOT");
    }
    sb.append(" IN ");
    _in.toSQL(prettyPrint,indent, sb, values);
  }

}
