package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/5/11
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnDataType extends SQLParsedElement {

  public enum Type {
    BIT, BOOL, TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT, REAL, DOUBLE, FLOAT, DECIMAL, NUMERIC,
    DATE, TIMESTAMP, TIME, DATETIME, YEAR,
    BINARY, CHAR, NCHAR, VARCHAR, VARBINARY, TINYBLOB, MEDIUMBLOB, BLOB, LONGBLOB,
    TINYTEXT, TEXT, MEDIUMTEXT, LONGTEXT
  }

  private Type _type;
  private ColumnLengthExpression _length;
  private List<? extends SQLParsedElement> _modifiers;

  public ColumnDataType(Token start, Token end, Type type, ColumnLengthExpression length, List<? extends SQLParsedElement> modifiers) {
    super(start, end, collectChildren(length, modifiers));
    _type = type;
    _length = length;
    _modifiers = modifiers;
  }

  public ColumnDataType(Token token, Type type) {
    super(token);
    _type = type;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }

  public Type getType() {
    return _type;
  }

  public ColumnLengthExpression getLength() {
    return _length;
  }

  public List<? extends SQLParsedElement> getModifiers() {
    return _modifiers;
  }
}
