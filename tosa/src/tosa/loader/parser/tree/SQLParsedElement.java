package tosa.loader.parser.tree;

import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class SQLParsedElement {

  private Token _first;
  private Token _last;
  private List<SQLParsedElement> _children;
  private SQLParsedElement _parent;
  private Set<SQLParseError> _errors;

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
    StringBuilder sb = new StringBuilder();
    toSQL(prettyPrint, 2, sb);
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

  protected abstract void toSQL(boolean prettyPrint, int indent, StringBuilder sb);

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
}
