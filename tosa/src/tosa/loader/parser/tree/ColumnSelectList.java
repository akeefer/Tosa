package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class ColumnSelectList extends SQLParsedElement {

  public ColumnSelectList(Token start, Token last, List<SQLParsedElement> children) {
    super(start, last, children);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    List<? extends SQLParsedElement> children = getChildren();
    for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
      SQLParsedElement sqlParsedElement = children.get(i);
      if (i != 0) {
        sb.append(", ");
      }
      sqlParsedElement.toSQL(prettyPrint, indent, sb, values);
    }
  }
}
