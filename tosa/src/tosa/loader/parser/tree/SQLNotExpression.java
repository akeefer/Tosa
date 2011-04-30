package tosa.loader.parser.tree;

import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.Map;

public class SQLNotExpression extends SQLParsedElement {
  private SQLParsedElement _rhs;

  public SQLNotExpression(Token start, SQLParsedElement rhs) {
    super(start, rhs);
    _rhs = rhs;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    setType(DBColumnTypeImpl.BOOLEAN);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("NOT ");
    _rhs.toSQL(prettyPrint, indent, sb, values);
  }
}
