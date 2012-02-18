package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.JavaTypes;
import tosa.api.IDBColumnType;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.parser.Token;

import java.util.Arrays;
import java.util.Map;

public class ModExpression extends SQLParsedElement {
  private SQLParsedElement _firstExp;
  private SQLParsedElement _secondExp;

  public ModExpression(Token start, SQLParsedElement first, SQLParsedElement second, Token token) {
    super(start, Arrays.asList(first, second), token);
    _firstExp = first;
    _secondExp = second;
  }

  @Override
  public IDBColumnType getVarTypeForChild() {
    return DBColumnTypeImpl.INT;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("MOD(");
    _firstExp.toSQL(prettyPrint, indent, sb, values);
    sb.append(", ");
    _secondExp.toSQL(prettyPrint, indent, sb, values);
    sb.append(")");
  }
}
