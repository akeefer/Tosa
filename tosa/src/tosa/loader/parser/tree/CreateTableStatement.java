package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 9:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTableStatement extends SQLParsedElement {

  private Token _tableName;

  public CreateTableStatement(Token first, Token last, Token tableName, List<SQLParsedElement> children) {
    super(first, last, children);
    _tableName = tableName;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    // TODO - AHK

  }
}
