package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class TableFromClause extends SQLParsedElement {
  private List<SimpleTableReference> _refs;

  public TableFromClause(Token t, List<SimpleTableReference> refs) {
    super(t, refs.get(refs.size()-1).lastToken(), refs);
    _refs = refs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    pp(prettyPrint, indent, "FROM ", sb);
    for (int i = 0, _refsSize = _refs.size(); i < _refsSize; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      _refs.get(i).toSQL(prettyPrint, indent, sb, values);
    }
    sb.append("\n");
  }

  public List<SimpleTableReference> getTableRefs() {
    return _refs;
  }
}
