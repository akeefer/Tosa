package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class GroupByClause extends SQLParsedElement {
  public GroupByClause(Token start, List<SQLParsedElement> children) {
    super(start, children);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" GROUP BY");
    for (SQLParsedElement groupByClauses : getChildren()) {
      groupByClauses.toSQL(prettyPrint, indent, sb, values);
    }
  }
}
