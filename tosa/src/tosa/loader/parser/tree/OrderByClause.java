package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class OrderByClause extends SQLParsedElement {
  private List<SQLParsedElement> _sortSpec;

  public OrderByClause(Token first, Token last, List<SQLParsedElement> sortSpecification) {
    super(first, last, sortSpecification);
    _sortSpec = sortSpecification;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" ORDER BY ");
    for (int i = 0; i < _sortSpec.size(); i++) {
      SQLParsedElement sortSpec = _sortSpec.get(i);
      if (i != 0) {
        sb.append(",");
      }
      sortSpec.toSQL(prettyPrint, indent, sb, values);
    }
  }
}
