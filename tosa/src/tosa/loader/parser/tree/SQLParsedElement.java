package tosa.loader.parser.tree;

import tosa.loader.data.DBData;
import tosa.loader.parser.SQLParseException;
import tosa.loader.parser.Token;

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
  private Set<SQLParseError> _errors = new HashSet<SQLParseError>();

  public SQLParsedElement(Token token, SQLParsedElement... children) {
    this(token, token, children);
  }

  public SQLParsedElement(Token first, Token last, SQLParsedElement... children) {
    this(first, last, Arrays.asList(children));
  }

  public SQLParsedElement(Token first, Token last, List<? extends SQLParsedElement> children) {
    _first = first;
    _last = last;
    _children = new ArrayList<SQLParsedElement>();
    _errors.addAll(first.collectTemporaryErrors(last));
    for (SQLParsedElement child : children) {
      if (child != null) {
        _children.add(child);
      }
    }
    for (SQLParsedElement child : _children) {
      child._parent = this;
    }
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

  public <T extends SQLParsedElement> List<T> findDirectDescendents(Class<T> clazz) {
    ArrayList<T> lst = new ArrayList<T>();
    findDescendents(getClass(), clazz, lst);
    return lst;
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

}
