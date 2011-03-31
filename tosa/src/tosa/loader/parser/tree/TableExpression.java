package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class TableExpression extends SQLParsedElement {

  private TableFromClause _from;
  private SQLParsedElement _where;

  public TableExpression(TableFromClause fromClause) {
    super(fromClause.firstToken(), fromClause );
    _from = fromClause;
  }

  public TableExpression(TableFromClause fromClause, SQLParsedElement whereClause) {
    super(fromClause.firstToken(), fromClause, whereClause);
    _from = fromClause;
    _where = whereClause;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _from.toSQL(prettyPrint, indent, sb, values);
    if (_where != null) _where.toSQL(prettyPrint, indent, sb, values);
  }

  public TableFromClause getFrom() {
    return _from;
  }

  public SQLParsedElement getWhere() {
    return _where;
  }
}
