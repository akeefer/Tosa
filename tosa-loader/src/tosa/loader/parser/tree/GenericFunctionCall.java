package tosa.loader.parser.tree;

import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class GenericFunctionCall extends SQLParsedElement {

  private Token _functionName;
  private List<SQLParsedElement> _args;

  public GenericFunctionCall(Token functionName, List<SQLParsedElement> args, Token end) {
    super(functionName, args, end);
    _functionName = functionName;
    _args = args;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    //TODO cgross - should we determine types of common functions?
    setType(DBColumnTypeImpl.OBJECT);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" ");
    sb.append(_functionName);
    sb.append("(");
    for (SQLParsedElement arg : _args) {
      arg.toSQL(prettyPrint, indent, sb, values);
    }
    sb.append(")");
  }
}
