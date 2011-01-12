package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import tosa.loader.DBTypeData;
import tosa.loader.SQLParameterInfo;
import tosa.loader.TableTypeData;
import tosa.loader.parser.Token;

import java.util.*;

public class SelectStatement extends SQLParsedElement {

  private SQLParsedElement _quantifier;
  private SQLParsedElement _selectList;
  private TableExpression _tableExpr;
  private IType _type;
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

  public void verify(DBTypeData dbData) {
    _type = determineType(dbData);
    _parameters = determineParameters();
  }

  private List<SQLParameterInfo> determineParameters() {
    Map<String, SQLParameterInfo> pis = new HashMap<String, SQLParameterInfo>();
    List<VariableExpression> findDescendents = findDescendents(VariableExpression.class);
    for (int i = 0, findDescendentsSize = findDescendents.size(); i < findDescendentsSize; i++) {
      VariableExpression var = findDescendents.get(i);
      SQLParameterInfo pi = pis.get(var.getName());
      if (pi == null) {
        pi = new SQLParameterInfo(var.getName(), IJavaType.OBJECT);
      }
      pi.getIndexes().add(i);
    }
    return new ArrayList<SQLParameterInfo>(pis.values());
  }

  private IType determineType(DBTypeData dbData) {
    if (_selectList instanceof AsteriskSelectList) {
      TableFromClause from = _tableExpr.getFrom();
      if (from != null) {
        if (from.getTableRefs().size() == 1) {
          SimpleTableReference tableReference = from.getTableRefs().get(0);
          Token token = tableReference.getName();
          TableTypeData data = dbData.getTable(token.getValue());
          if (data != null) {
            return TypeSystem.getByFullName(data.getTypeName());
          }
        }
      }
    }
    return IJavaType.OBJECT;
  }


  public IType getSelectType() {
    return _type;
  }

  public List<SQLParameterInfo> getParameters() {
    return _parameters;
  }
}
