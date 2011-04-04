package tosa.loader.parser;

import tosa.loader.data.DBData;
import tosa.loader.parser.tree.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryParser implements SQLParserConstants {
  private Token _currentToken;
  private DBData _data;

  public QueryParser(Token token, DBData dbData) {
    _currentToken = token;
    _data = dbData;
  }

  public SelectStatement parseSelect() {
    if (match(SELECT)) {
      Token start = lastMatch();
      SQLParsedElement quantifier = parseSetQuantifers();
      SQLParsedElement selectList = parseSelectList();
      TableExpression tableExpr = parseTableExpression();
      SQLParsedElement orderByExpr = parseOrderByClause();
      SelectStatement select = new SelectStatement(start, quantifier, selectList, tableExpr, orderByExpr);
      if (_data != null) {
        select.verify(_data);
      }
      return select;
    }
    return null;
  }

  private SQLParsedElement parseOrderByClause() {
    if (match(ORDER, BY)) {
      Token first = lastMatch().previous();
      return new OrderByClause(first, parseSortSpecificationList());
    } else {
      return null;
    }
  }

  private List<SQLParsedElement> parseSortSpecificationList() {
    ArrayList<SQLParsedElement> lst = new ArrayList<SQLParsedElement>();
    do {
      SQLParsedElement valueExpr = parseValueExpression();
      boolean ascending = false;
      boolean descending = false;
      if (match(ASC)) {
        ascending = true;
      } else if (match(DESC)) {
        descending = true;
      }
      lst.add(new SortSpecification(valueExpr, lastMatch(), ascending, descending));
    } while (match(COMMA));
    return lst;
  }

  private TableExpression parseTableExpression() {
    TableFromClause fromClause = parseFromClause();
    SQLParsedElement whereClause = parseWhereClause();
    SQLParsedElement groupByClause = parseGroupByClause();
    return new TableExpression(fromClause, whereClause, groupByClause);
  }

  private SQLParsedElement parseGroupByClause() {
    if (match(GROUP, BY)) {
      Token start = lastMatch().previous();
      return new GroupByClause(start, parseGroupingElementList());
    } else {
      return null;
    }
  }

  private List<SQLParsedElement> parseGroupingElementList() {
    ArrayList<SQLParsedElement> groupBys = new ArrayList<SQLParsedElement>();
    do {
      SQLParsedElement ge = parseGroupingElement();
      groupBys.add(ge);
    } while (match(COMMA));
    return groupBys;
  }

  private SQLParsedElement parseGroupingElement() {
    //TODO cgross - support exotic group by targets
    SQLParsedElement colRef = parseColumnReference();
    if (colRef != null) {
      return colRef;
    } else {
      return unexpectedToken();
    }
  }

  private SQLParsedElement parseWhereClause() {
    if (match(WHERE)) {
      return new WhereClause(lastMatch(), parseSearchOrExpression());
    } else if (_currentToken.isEOF() || peek(ORDER, BY) || peek(GROUP, BY)) {
      return null;
    } else {
      return unexpectedToken();
    }
  }

  private SQLParsedElement parseSearchOrExpression() {
    SQLParsedElement lhs = parseSearchAndExpression();
    if (match(OR)) {
      SQLParsedElement rhs = parseSearchOrExpression();
      return new SQLOrExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchAndExpression() {
    SQLParsedElement lhs = parseSearchNotExpression();
    if (match(AND)) {
      SQLParsedElement rhs = parseSearchAndExpression();
      return new SQLAndExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchNotExpression() {
    if (match(NOT)) {
      Token last = lastMatch();
      SQLParsedElement val = parseBooleanTestExpression();
      return new SQLNotExpression(last, val);
    } else {
      return parseBooleanTestExpression();
    }
  }

  private SQLParsedElement parseBooleanTestExpression() {
    return parseBooleanPrimaryExpression();
    //TODO cgross IS NOT TRUE
  }

  private SQLParsedElement parseBooleanPrimaryExpression() {
    if (match(OPEN_PAREN)) {
      SQLParsedElement elt = parseSearchOrExpression();
      expect(CLOSE_PAREN);
      return elt;
    } else {
      return parsePredicate();
    }
  }

  private SQLParsedElement parsePredicate() {
    SQLParsedElement initialValue = parseRowValue();
    if (initialValue != null) {
      SQLParsedElement comparison = parseComparisonPredicate(initialValue);
      if (comparison != null) {
        return comparison;
      }

      SQLParsedElement between = parseBetweenPredicate(initialValue);
      if (between != null) {
        return between;
      }

      SQLParsedElement in = parseInPredicate(initialValue);
      if (in != null) {
        return in;
      }

      SQLParsedElement like = parseLikePredicate(initialValue);
      if (like != null) {
        return like;
      }

      SQLParsedElement nullPredicate = parseNullPredicate(initialValue);
      if (nullPredicate != null) {
        return nullPredicate;
      }
    }
    return unexpectedToken();
  }

  private SQLParsedElement parseNullPredicate(SQLParsedElement initialValue) {
    if (match(IS, NOT, NULL)) {
      return new IsNotNullPredicate(initialValue, lastMatch(), true);
    } else if (match(IS, NULL)) {
      _currentToken = _currentToken.nextToken().nextToken();
      return new IsNotNullPredicate(initialValue, lastMatch(), false);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseLikePredicate(SQLParsedElement initialValue) {
    if (match(NOT, LIKE)) {
      return new LikePredicate(initialValue, parsePattern(), true);
    } else if (match(LIKE)) {
      return new LikePredicate(initialValue, parsePattern(), false);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseComparisonPredicate(SQLParsedElement initialValue) {
    if (matchAny(EQ_OP, LT_OP, LTEQ_OP, GT_OP, GTEQ_OP)) {
      Token op = lastMatch();
      SQLParsedElement comparisonValue = parseValueExpression();
      return new ComparisonPredicate(initialValue, op, comparisonValue);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseInPredicate(SQLParsedElement initialValue) {
    if (match(NOT, IN)) {
      return new InPredicate(initialValue, parseInPredicateValue(), true);
    } else if (match(IN)) {
      return new InPredicate(initialValue, parseInPredicateValue(), false);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseInPredicateValue() {
    //TODO cgross - table subquery
    if (match(OPEN_PAREN)) {
      Token first = lastMatch();
      List<SQLParsedElement> values = new ArrayList<SQLParsedElement>();
      if (!match(CLOSE_PAREN)) {
        do {
          values.add(parseRowValue());
        } while (match(COMMA));
      }
      return new InListExpression(first, values);
    }

    SQLParsedElement varRef = parseVariableReference();
    if (varRef != null) {
      ((VariableExpression) varRef).setList(true);
      return varRef;
    }

    return unexpectedToken();
  }

  private SQLParsedElement parseBetweenPredicate(SQLParsedElement initialValue) {
    if (match(NOT, BETWEEN)) {
      return parseRestOfBetween(initialValue, true);
    } else if (match(BETWEEN)) {
      return parseRestOfBetween(initialValue, false);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseRestOfBetween(SQLParsedElement initialValue, boolean not) {
    boolean symmetric = false;
    boolean asymmetric = false;
    if (match(SYMMETRIC)) {
      symmetric = true;
    } else if (match(ASYMMETRIC)) {
      asymmetric = true;
    }
    SQLParsedElement bottom = parseRowValue();
    if (!match(AND)) {
      bottom.addParseError(new SQLParseError(_currentToken, "Expected And"));
    }
    SQLParsedElement top = parseRowValue();
    return new BetweenPredicate(initialValue, bottom, top, not, symmetric, asymmetric);
  }

  private SQLParsedElement parsePattern() {
    return parseRowValue();
  }

  private UnexpectedTokenExpression unexpectedToken() {
    Token unexpectedToken = takeToken();
    UnexpectedTokenExpression utExpr = new UnexpectedTokenExpression(unexpectedToken);
    utExpr.addParseError(new SQLParseError(unexpectedToken, "Unexpected Token: " + unexpectedToken.getValue()));
    return utExpr;
  }

  private SQLParsedElement parseRowValue() {
    //TODO NULL, DEFAULT
    return parseValueExpression();
  }

  private SQLParsedElement parseValueExpression() {

    SQLParsedElement elt = parseNumericValueExpression();
    if (elt != null) {
      return elt;
    }

    elt = parseStringValueExpression();
    if (elt != null) {
      return elt;
    }

    elt = parseDateTimeValueExpression();
    if (elt != null) {
      return elt;
    }

    elt = parseIntervalValueExpression();
    if (elt != null) {
      return elt;
    }

    //TODO cgross - this might not belong here.  Distinction between value primary and value is?
    elt = parseSetFunctionExpression();
    if (elt != null) {
      return elt;
    }

    return unexpectedToken();
  }

  private SQLParsedElement parseSetFunctionExpression() {
    if (match(COUNT, OPEN_PAREN, ASTERISK )) {
      Token start = lastMatch().previous().previous();
      expect(CLOSE_PAREN);
      return new CountAllExpression(start, lastMatch());
    } else if (match(COUNT) ||
      match(AVG) ||
      match(SUM) ||
      match(MAX) ||
      match(MIN)) {
      Token first = lastMatch();
      expect(OPEN_PAREN);
      SQLParsedElement value = parseValueExpression();
      expect(CLOSE_PAREN);
      return new SetFunctionExpression(first, value, lastMatch());
    } else if(_currentToken.isSymbol() && _currentToken.nextToken().match(OPEN_PAREN)) {
      return parseFunctionCall();
    } else {
      return null;
    }
  }

  private SQLParsedElement parseFunctionCall() {
    if (_currentToken.isSymbol() && _currentToken.nextToken().match(OPEN_PAREN)) {
      Token functionName = takeToken();
      Token paren = takeToken();
      List<SQLParsedElement> args = new ArrayList<SQLParsedElement>();
      while (!match(CLOSE_PAREN)) {
        SQLParsedElement arg = parseValueExpression();
        args.add(arg);
        if (!peek(CLOSE_PAREN)) {
          expect(COMMA);
        }
      }
      return new GenericFunctionCall(functionName, args, lastMatch());
    } else {
      return unexpectedToken();
    }
  }

  private SQLParsedElement parseIntervalValueExpression() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseDateTimeValueExpression() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseStringValueExpression() {
    if (_currentToken.isString()) {
      return new StringLiteralExpression(takeToken());
    } else {
      return null;
    }
  }

  private SQLParsedElement parseNumericValueExpression() {
    SQLParsedElement lhs = parseTerm();
    if (matchAny(PLUS_OP, MINUS_OP)) {
      Token op = lastMatch();
      SQLParsedElement rhs = parseNumericValueExpression();
      return new SQLAdditiveExpression(lhs, op, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseTerm() {
    SQLParsedElement lhs = parseFactor();
    if (matchAny(TIMES_OP, DIV_OP)) {
      Token op = lastMatch();
      SQLParsedElement rhs = parseTerm();
      return new SQLMultiplicitiveExpression(lhs, op, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseFactor() {
    if (matchAny(PLUS_OP, MINUS_OP)) {
      return new SQLSignedExpression(lastMatch(), parseNumericPrimary());
    } else {
      return parseNumericPrimary();
    }
  }

  private SQLParsedElement parseNumericPrimary() {

    SQLParsedElement numericLiteral = parseNumericLiteral();
    if (numericLiteral != null) {
      return numericLiteral;
    }

    SQLParsedElement varRef = parseVariableReference();
    if (varRef != null) {
      return varRef;
    }

    SQLParsedElement columnRef = parseColumnReference();
    if (columnRef != null) {
      return columnRef;
    }

    return null;
  }

  private SQLParsedElement parseVariableReference() {
    if (_currentToken.isSymbol() && _currentToken.getValue().startsWith(":")) {
      return new VariableExpression(takeToken());
    } else {
      return null;
    }
  }

  private SQLParsedElement parseColumnReference() {
    if (_currentToken.isSymbol() && !_currentToken.nextToken().match(OPEN_PAREN)) {
      Token base = takeToken();
      if (match(".") && _currentToken.isSymbol()) {
        return new ColumnReference(base, takeToken());
      } else {
        return new ColumnReference(base);
      }
    }
    return null;
  }

  private Token takeToken() {
    Token base = _currentToken;
    _currentToken = _currentToken.nextToken();
    return base;
  }

  private SQLParsedElement parseNumericLiteral() {
    if (_currentToken.isNumber()) {
      Token token = takeToken();
      if (token.getValue().contains(".")) {
        return new SQLNumericLiteral(token, new BigDecimal(token.getValue()));
      }
      else
      {
        return new SQLNumericLiteral(token, new BigInteger(token.getValue()));
      }
    }
    return null;
  }

  private TableFromClause parseFromClause() {
    if (expect(FROM)) {
      Token start = lastMatch();
      ArrayList<SQLParsedElement> refs = new ArrayList<SQLParsedElement>();
      do {
        SQLParsedElement ref = parseTableReference();
        refs.add(ref);
      }
      while (match(COMMA));
      return new TableFromClause(start, refs);
    } else {
      return new TableFromClause(lastMatch(), Collections.EMPTY_LIST);
    }
  }

  private SQLParsedElement parseTableReference() {
    SQLParsedElement primaryTable = parseTablePrimary();
    return parseJoinedTable(primaryTable);
  }

  private SQLParsedElement parseJoinedTable(SQLParsedElement joinTarget) {
    // TODO cgross - other types of joins
    if(match(JOIN) ||
       match(INNER, JOIN) ||
       match(LEFT, OUTER, JOIN) ||
       match(RIGHT, OUTER, JOIN) ||
       match(FULL, OUTER, JOIN)) {
      Token lastMatch = lastMatch();
      //TODO cgross - grammar says this is a table entry.  Ambiguity?
      SQLParsedElement table = parseTablePrimary();
      SQLParsedElement joinSpec = parseJoinSpecification();
      return parseJoinedTable(new QualifiedJoin(joinTarget, table, joinSpec, getJoinType(lastMatch)));
    } else {
      return joinTarget;
    }
  }

  private QualifiedJoin.JoinType getJoinType(Token lastMatch) {
    if (lastMatch.endOf(INNER, JOIN)) {
      return QualifiedJoin.JoinType.INNER;
    } else if (lastMatch.endOf(LEFT, OUTER, JOIN)) {
      return QualifiedJoin.JoinType.LEFT_OUTER;
    } else if (lastMatch.endOf(RIGHT, OUTER, JOIN)) {
      return QualifiedJoin.JoinType.RIGHT_OUTER;
    } else if (lastMatch.endOf(FULL, OUTER, JOIN)) {
      return QualifiedJoin.JoinType.FULL_OUTER;
    } else {
      return QualifiedJoin.JoinType.REGULAR;      
    }
  }

  private SQLParsedElement parseJoinSpecification() {
    if (match(ON)) {
      Token start = lastMatch();
      SQLParsedElement condition = parseSearchOrExpression();
      return new JoinCondition(start, condition);
    } else {
      // TODO cgross - support named columns join ???
      return unexpectedToken();
    }
  }

  private SQLParsedElement parseTablePrimary() {
    //TODO cgross - shouldn't allow keywords, should support AS and derived column lists
    return new SimpleTableReference(takeToken());
  }

  private SQLParsedElement parseSelectList() {
    if (match(ASTERISK)) {
      return new AsteriskSelectList(lastMatch());
    } else {
      return parseSelectSubList();
    }
  }

  private SQLParsedElement parseSelectSubList() {
    ArrayList<SQLParsedElement> cols = new ArrayList<SQLParsedElement>();
    do {
      SQLParsedElement value = parseValueExpression();
      if (!(value instanceof ColumnReference)) {
        if (match(AS)) {
          Token colName = takeToken();
          if (!colName.isSymbol()) {
            colName.addTemporaryError(new SQLParseError(colName, "Expected a column name!"));
          }
          value = new DerivedColumn(value, colName);
        } else {
          value.addParseError(new SQLParseError(value.firstToken(), value.lastToken(), "Only column references are supported right now."));
        }
      }
      cols.add(value);
    } while (match(COMMA));
    return new ColumnSelectList(cols);
  }

  private SQLParsedElement parseSetQuantifers() {
    if (matchAny(DISTINCT, ALL)) {
      return new QuantifierModifier(lastMatch());
    } else {
      return null;
    }
  }

  private boolean matchAny(String... tokens) {
    for (String s : tokens) {
      if (match(s)) {
        return true;
      }
    }
    return false;
  }

  private boolean match(String str) {
    if (_currentToken.match(str)) {
      _currentToken = _currentToken.nextToken();
      return true;
    } else {
      return false;
    }
  }

  private boolean match(String str1, String str2) {
    if (_currentToken.match(str1) && _currentToken.nextToken().match(str2)) {
      _currentToken = _currentToken.nextToken().nextToken();
      return true;
    } else {
      return false;
    }
  }

  private boolean peek(String str) {
    if (_currentToken.match(str)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean peek(String str1, String str2) {
    if (_currentToken.match(str1) && _currentToken.nextToken().match(str2)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean match(String str1, String str2, String str3) {
    if (_currentToken.match(str1) &&
      _currentToken.nextToken().match(str2) &&
      _currentToken.nextToken().nextToken().match(str3)) {
      _currentToken = _currentToken.nextToken().nextToken().nextToken();
      return true;
    } else {
      return false;
    }
  }

  private Token lastMatch() {
    return _currentToken.previous();
  }

  private boolean expect(String str) {
    if (match(str)) {
      return true;
    } else {
      _currentToken.addTemporaryError(new SQLParseError(_currentToken, _currentToken, "Expected " + str));
      _currentToken = _currentToken.nextToken();
      return false;
    }
  }

}
