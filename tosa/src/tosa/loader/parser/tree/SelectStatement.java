package tosa.loader.parser.tree;

import gw.util.Pair;
import tosa.api.IDBColumnType;
import tosa.loader.SQLParameterInfo;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.*;

public class SelectStatement extends SQLParsedElement implements IRootParseElement {

  private SQLParsedElement _quantifier;
  private SQLParsedElement _selectList;
  private TableExpression _tableExpr;
  private List<SQLParameterInfo> _parameters;

  public SelectStatement(Token start, Token end, SQLParsedElement quantifier, SQLParsedElement selectList, TableExpression tableExpr) {
    super(start, end, quantifier, selectList, tableExpr);
    _quantifier = quantifier;
    _selectList = selectList;
    _tableExpr = tableExpr;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    pp(prettyPrint, indent, "SELECT ", sb);
    if (_quantifier != null) {
      _quantifier.toSQL(prettyPrint, indent, sb);
    }
    _selectList.toSQL(prettyPrint, indent, sb);
    sb.append("\n");
    _tableExpr.toSQL(prettyPrint, indent, sb);
  }

  public TableExpression getTableExpression() {
    return _tableExpr;
  }

  public SQLParsedElement getSelectList() {
    return _selectList;
  }

  public void verify(DBData dbData) {
    super.verify(dbData);
    _parameters = determineParameters(dbData);
  }

  private List<SQLParameterInfo> determineParameters(DBData dbData) {
    Map<String, SQLParameterInfo> pis = new HashMap<String, SQLParameterInfo>();
    List<VariableExpression> findDescendents = findDescendents(VariableExpression.class);
    for (int i = 0, findDescendentsSize = findDescendents.size(); i < findDescendentsSize; i++) {
      VariableExpression var = findDescendents.get(i);
      SQLParameterInfo pi = pis.get(var.getName());
      if (pi == null) {
        pi = new SQLParameterInfo(var.getName());
        pis.put(var.getName(), pi);
      }
      pi.getIndexes().add(Pair.<Integer, IDBColumnType>make(i + 1, null));
    }
    return new ArrayList<SQLParameterInfo>(pis.values());
  }

  public List<SQLParameterInfo> getParameters() {
    return _parameters;
  }

  @Override
  public String getDefaultTableName() {
    return _tableExpr.getFrom().getTableRefs().get(0).getName().getValue(); //TODO cgross - support multiple tables in table clause
  }
}
