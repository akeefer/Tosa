package tosa.loader.parser.tree;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import tosa.api.IDBColumnType;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.SQLParseException;
import tosa.loader.parser.Token;
import tosa.loader.parser.TokenType;

import java.util.*;

public abstract class SQLParsedElement {

  public static final Comparator<SQLParsedElement> OFFSET_COMPARATOR = new Comparator<SQLParsedElement>() {
      @Override
      public int compare(SQLParsedElement v1, SQLParsedElement v2) {
        return v1.getStart() - v2.getStart();
      }
    };

  private Token _first;
  private Token _last;
  private List<SQLParsedElement> _children;
  private SQLParsedElement _parent;
  private IDBColumnType _dbType;
  private Set<SQLParseError> _errors = new HashSet<SQLParseError>();
  public static final Token INFER = new Token(TokenType.UNKNOWN, "Infer This Token", 0, 0, 0, 0);

  public SQLParsedElement(SQLParsedElement... children) {
    this(Arrays.asList(children));
  }

  public SQLParsedElement(Token start, SQLParsedElement... children) {
    this(start, Arrays.asList(children));
  }

  public SQLParsedElement(SQLParsedElement child, Token end) {
    this(INFER, Collections.singletonList(child), end);
  }

  public SQLParsedElement(Token start, SQLParsedElement child) {
    this(start, Collections.singletonList(child), INFER);
  }

  public SQLParsedElement(Token start, SQLParsedElement child, Token end) {
    this(start, Collections.singletonList(child), end);
  }

  public SQLParsedElement(List<SQLParsedElement> children) {
    this(INFER, children,  INFER);
  }

  public SQLParsedElement(Token start, List<SQLParsedElement> children) {
    this(start, children, INFER);
  }

  public SQLParsedElement(List<SQLParsedElement> children, Token last) {
    this(INFER, children, last);
  }

  public SQLParsedElement(SQLParsedElement lhs, SQLParsedElement rhs) {
    this(lhs.getFirst(), Arrays.asList(lhs, rhs), rhs.getLast());
  }

  public SQLParsedElement(Token start) {
    this(start, Collections.<SQLParsedElement>emptyList(), start);
  }

  public SQLParsedElement(Token start, Token last) {
    this(start, Collections.<SQLParsedElement>emptyList(), last);
  }

  public SQLParsedElement(Token first, List<? extends SQLParsedElement> children, Token last) {

    _children = new ArrayList<SQLParsedElement>();
    for (SQLParsedElement child : children) {
      if (child != null) {
        _children.add(child);
        child._parent = this;
      }
    }

    if (first == INFER) {
      _first = findFirstToken(_children, last);
    } else {
      _first = first;
    }

    if (last == INFER) {
      _last = findLastToken(_children, first);
    } else {
      _last = last;
    }

    _errors.addAll(_first.collectTemporaryErrors(_last));
  }

  public Token firstToken() {
    return _first;
  }

  public Token lastToken() {
    return _last;
  }

  public Token nextToken() {
    return _last.nextToken();
  }

  public SQLParsedElement getParent() {
    return _parent;
  }

  public List<? extends SQLParsedElement> getChildren() {
    return _children;
  }

  public final String toSQL() {
    return toSQL(true);
  }

  public final String toSQL(boolean prettyPrint) {
    return toSQL(prettyPrint, null);
  }

  public final String toSQL(Map<String, Object> values) {
    return toSQL(true, values);
  }

  public final String toSQL(boolean prettyPrint, Map<String, Object> values) {
    StringBuilder sb = new StringBuilder();
    toSQL(prettyPrint, 2, sb, values);
    return sb.toString();
  }

  public <T extends SQLParsedElement> List<T> findDescendents(Class<T> clazz) {
    ArrayList<T> lst = new ArrayList<T>();
    findDescendents(null, clazz, lst);
    return lst;
  }

  public <T> T getAncestor(Class<T> type) {
    if (type.isAssignableFrom(this.getClass())) {
      return (T) this;
    } else if(getParent() == null) {
      return null;
    } else {
      return getParent().getAncestor(type);
    }
  }

  @Override
  public String toString() {
    return toSQL();
  }

  protected abstract void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values);

  protected void pp(boolean prettyPrint, int indent, String s, StringBuilder sb) {
    if (prettyPrint) {
      while (indent > 0) {
        sb.append("  ");
        indent--;
      }
    }
    sb.append(s);
  }

  private void findDescendents(Class cutOff, Class type, List elts) {
    if (cutOff != null && cutOff.isAssignableFrom(this.getClass())) {
      return;
    } else if (type.isAssignableFrom(this.getClass())) {
      elts.add(this);
    }
    for (SQLParsedElement child : getChildren()) {
      child.findDescendents(cutOff, type, elts);
    }
  }

  public IRootParseElement getRootElement() {
    if (getParent() == null) {
      return (IRootParseElement) this;
    } else {
      return getParent().getRootElement();
    }
  }

  public void resolveTypes(DBData dbData) {
    for (SQLParsedElement child : getChildren()) {
      child.resolveTypes(dbData);
    }
  }

  public void resolveVars(DBData dbData) {
    for (SQLParsedElement child : getChildren()) {
      child.resolveVars(dbData);
    }
  }

  public void verify(DBData dbData) {
    for (SQLParsedElement child : getChildren()) {
      child.verify(dbData);
    }
  }

  public void addParseError(SQLParseError error){
    _errors.add(error);
  }

  public Token getFirst() {
    return _first;
  }

  public Token getLast() {
    return _last;
  }

  public int getStart() {
    return _first.getStart();
  }

  public int getEnd() {
    return _last .getEnd();
  }

  protected static List<SQLParsedElement> collectChildren(Object... args) {
    List<SQLParsedElement> results = new ArrayList<SQLParsedElement>();
    for (Object arg : args) {
      if (arg != null) {
        if (arg instanceof SQLParsedElement) {
          results.add((SQLParsedElement) arg);
        } else if (arg instanceof List) {
          results.addAll((List) arg);
        } else {
          throw new IllegalArgumentException("Argument " + arg + " with type " + arg.getClass() + " is not a SQLParsedElement or a list");
        }
      }
    }
    return results;
  }

  public Set<SQLParseError> getErrors() {
    HashSet<SQLParseError> errors = new HashSet<SQLParseError>();
    collectErrors(errors);
    return errors;
  }

  private void collectErrors(HashSet<SQLParseError> errors) {
    errors.addAll(_errors);
    for (SQLParsedElement child : _children) {
      child.collectErrors(errors);
    }
  }

  public SQLParseException getSQLParseException(String fileName) {
    Set<SQLParseError> errors = getErrors();
    if (errors.size() > 0) {
      return new SQLParseException(fileName, errors);
    } else {
      return null;
    }
  }

  public void setType(IDBColumnType columnType) {
    _dbType = columnType;
  }

  public IDBColumnType getDBType() {
    return _dbType;
  }

  private Token findFirstToken(List<? extends SQLParsedElement> children, Token defaultToken) {
    for (int i = 0; i < children.size(); i++) {
      SQLParsedElement child = children.get(i);
      if (child != null) {
        return child.firstToken();
      }
    }
    return defaultToken == INFER ? null : defaultToken;
  }

  private Token findLastToken(List<? extends SQLParsedElement> children, Token defaultToken) {
    for (int i = children.size() - 1; i >= 0; i--) {
      SQLParsedElement child = children.get(i);
      if (child != null) {
        return child.lastToken();
      }
    }
    return defaultToken == INFER ? null : defaultToken;
  }

  public IType getVarTypeForChild() {
    return JavaTypes.STRING();
  }
}
