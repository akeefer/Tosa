package tosa.loader.parser.tree;

import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

public class SQLOptionalExpression extends SQLParsedElement implements IMightApply {

  private SQLParsedElement _elt;
  private List<VariableExpression> _descendentVars;

  public SQLOptionalExpression(Token openParen, SQLParsedElement elt, Token closeParen) {
    super(openParen, elt, closeParen);
    _elt = elt;
  }

  @Override
  public void resolveVars(DBData dbData) {
    _descendentVars = findDescendents(VariableExpression.class);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    if (applies(values)) {
      sb.append("(");
      _elt.toSQL(prettyPrint, indent, sb, values);
      sb.append(")");
    } else {
      sb.append("(TRUE)");
    }
  }

  @Override
  public boolean applies(Map<String, Object> values) {
    for (VariableExpression var : _descendentVars) {
      if(values.get(var.getName()) == null) {
        return false;
      }
    }
    return true;
  }
}
