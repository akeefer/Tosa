package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class TableExpression extends SQLParsedElement {

  private TableFromClause _from;
  private SQLParsedElement _where;
  private SQLParsedElement _groupBy;

  public TableExpression(TableFromClause fromClause, SQLParsedElement whereClause, SQLParsedElement groupByClause) {
    super(fromClause.firstToken(), fromClause, whereClause);
    _from = fromClause;
    _where = whereClause;
    _groupBy = groupByClause;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _from.toSQL(prettyPrint, indent, sb, values);
    if (_where != null) _where.toSQL(prettyPrint, indent, sb, values);
    if (_groupBy != null) _groupBy.toSQL(prettyPrint, indent, sb, values);
  }

  public TableFromClause getFrom() {
    return _from;
  }

  public SQLParsedElement getWhere() {
    return _where;
  }
}
