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
      SelectStatement select = new SelectStatement(start, tableExpr.lastToken(), quantifier, selectList, tableExpr);
      if (_data != null) {
        select.verify(_data);
      }
      return select;
    }
    return null;
  }

  private Token lastMatch() {
    return _currentToken.previous();
  }

  private boolean expect(String str) {
    //TODO cgross - parse error at the token level
    return match(str);
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

  private TableExpression parseTableExpression() {
    Token start = _currentToken;
    TableFromClause fromClause = parseFromClause();
    SQLParsedElement whereClause = parseWhereClause();
    TableExpression table = new TableExpression(start, lastMatch(), fromClause, whereClause);
    return table;
  }

  private SQLParsedElement parseWhereClause() {
    if (match(WHERE)) {
      return new WhereClause(lastMatch(), parseSearchOrExpression());
    } else if(_currentToken.isEOF()) {
      return null;
    } else {
      throw new IllegalStateException("This should be a parse error.");
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
      expectToken(elt, CLOSE_PAREN);
      return elt;
    } else {
      return parsePredicate();
    }
  }

  private SQLParsedElement parsePredicate() {
    SQLParsedElement initialValue = parseRowValue();
    if (initialValue != null) {
      SQLParsedElement comparison = parseComparsionPredicate(initialValue);
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
      return new IsNotNullPredicate(initialValue, _currentToken.previous(), true);
    } else if (match(IS, NULL)) {
      _currentToken = _currentToken.nextToken().nextToken();
      return new IsNotNullPredicate(initialValue, _currentToken.previous(), false);
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

  private SQLParsedElement parseComparsionPredicate(SQLParsedElement initialValue) {
    if (matchAny(EQ_OP, LT_OP, LTEQ_OP, GT_OP, GTEQ_OP)) {
      SQLParsedElement comparisonValue = parseValueExpression();
      return new ComparisonPredicate(initialValue, initialValue.nextToken(), comparisonValue);
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
      return new InListExpression(first,  _currentToken.previous(), values);
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
    utExpr.addParseError(new SQLParseError(unexpectedToken, "Unexpected Token"));
    return utExpr;
  }

  private void expectToken(SQLParsedElement elt, String str) {
    if (!match(str)) {
      elt.addParseError(new SQLParseError(_currentToken, "Expected " + str));
    }
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

    return null;
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
    if (_currentToken.isSymbol()) {
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
    if (match(FROM)) {
      Token start = lastMatch();
      ArrayList<SimpleTableReference> refs = new ArrayList<SimpleTableReference>();
      do {
        SimpleTableReference ref = parseTableReference();
        refs.add(ref);
      }
      while (match(COMMA));
      return new TableFromClause(start, refs);
    } else {
      TableFromClause from = new TableFromClause(_currentToken, Collections.<SimpleTableReference>emptyList());
      expectToken(from, FROM);
      takeToken();
      return from;
    }
  }

  private SimpleTableReference parseTableReference() {
    return new SimpleTableReference(takeToken());
    //TODO cgross more exotic table references
  }

  private SQLParsedElement parseSelectList() {
    if (match(ASTERISK)) {
      return new AsteriskSelectList(lastMatch());
    } else {
      return parseSelectSubList();
    }
  }

  private SQLParsedElement parseSelectSubList() {
    Token start = _currentToken;
    ArrayList<SQLParsedElement> cols = new ArrayList<SQLParsedElement>();
    do {
      SQLParsedElement value = parseValueExpression();
      if (!(value instanceof ColumnReference)) {
        value.addParseError(new SQLParseError(value.firstToken(), value.lastToken(), "Only column references are supported right now."));
      }
      if (value != null) {
        cols.add(value);
      } else {
        break;
      }
    } while (match(COMMA));
    return new ColumnSelectList(start, lastMatch(), cols);
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
}
