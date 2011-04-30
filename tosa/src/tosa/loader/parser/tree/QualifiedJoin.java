package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

import static tosa.loader.parser.tree.QualifiedJoin.JoinType.*;

public class QualifiedJoin extends SQLParsedElement {
  private SQLParsedElement _root;
  private SQLParsedElement _table;
  private SQLParsedElement _joinSpec;
  private JoinType _type;

  public enum JoinType {
    REGULAR,
    INNER,
    LEFT_OUTER,
    RIGHT_OUTER,
    FULL_OUTER,
  }

  public QualifiedJoin(SQLParsedElement joinTarget, SQLParsedElement table, SQLParsedElement joinSpec, JoinType type) {
    super(joinTarget.firstToken(), joinTarget, table, joinSpec);
    _root = joinTarget;
    _table = table;
    _joinSpec = joinSpec;
    _type = type;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _root.toSQL(prettyPrint, indent, sb, values);
    sb.append("\n");
    if (_type == INNER) {
      sb.append("INNER ");
    } else if (_type == LEFT_OUTER) {
      sb.append("LEFT OUTER ");
    } else if (_type == RIGHT_OUTER) {
      sb.append("RIGHT OUTER ");
    } else if (_type == FULL_OUTER) {
      sb.append("FULL OUTER ");
    }
    sb.append("JOIN ");
    _table.toSQL(prettyPrint, indent, sb, values);
    _joinSpec.toSQL(prettyPrint, indent, sb, values);
  }
}
