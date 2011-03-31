package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class TableFromClause extends SQLParsedElement {
  private List<SQLParsedElement> _tableRefs;

  public TableFromClause(Token t, List<SQLParsedElement> refs) {
    super(t, refs.get(refs.size()-1).lastToken(), refs);
    _tableRefs = refs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    pp(prettyPrint, indent, "FROM ", sb);
    for (int i = 0, _refsSize = _tableRefs.size(); i < _refsSize; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      _tableRefs.get(i).toSQL(prettyPrint, indent, sb, values);
    }
    sb.append("\n");
  }

  public List<SQLParsedElement> getTableRefs() {
    return _tableRefs;
  }
}
