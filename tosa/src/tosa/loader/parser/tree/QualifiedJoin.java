package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class QualifiedJoin extends SQLParsedElement {
  private SQLParsedElement _root;
  private SQLParsedElement _table;
  private SQLParsedElement _joinSpec;

  public QualifiedJoin(Token start, SQLParsedElement joinTarget, SQLParsedElement table, SQLParsedElement joinSpec) {
    super(start, joinTarget, table, joinSpec);
    _root = joinTarget;
    _table = table;
    _joinSpec = joinSpec;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _root.toSQL(prettyPrint, indent, sb, values);
    sb.append("\nJOIN ");
    _table.toSQL(prettyPrint, indent, sb, values);
    _joinSpec.toSQL(prettyPrint, indent, sb, values);
  }
}
