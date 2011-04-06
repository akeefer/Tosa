package tosa.loader.parser.tree;

import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.math.BigDecimal;
import java.util.Map;

public class SQLNumericLiteral extends SQLParsedElement {
  private Number _value;

  public SQLNumericLiteral(Token start, Number value) {
    super(start);
    _value = value;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    if (_value instanceof BigDecimal) {
      setType(DBColumnTypeImpl.DOUBLE);
    } else {
      setType(DBColumnTypeImpl.INT);
    }
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_value);
  }
}
