package tosa.loader.parser;

import org.junit.Ignore;
import org.junit.Test;
import tosa.loader.parser.tree.SelectStatement;
import tosa.loader.parser.tree.VariableExpression;

import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class SelectParsingBootstrapTest {
  @Test public void bootstrapSelectTest() {
    QueryParser parser = new QueryParser(Token.tokenize("SELECT * FROM foo WHERE foo.bar = 10"), null);
    SelectStatement select = parser.parseSelect();
    assertNotNull(select);
    assertNotNull(select.firstToken().toString());
    String sql = select.toSQL();
    System.out.println(sql);
    assertNotNull(sql);
  }

  @Test public void bootstrapVariableTest() {
    QueryParser parser = new QueryParser(Token.tokenize("SELECT * \n" +
                                                 "FROM foo \n" +
                                                 "WHERE foo.bar = :val"), null);
    SelectStatement select = parser.parseSelect();
    assertNotNull(select);
    assertNotNull(select.firstToken().toString());
    String sql = select.toSQL();
    System.out.println(sql);
    assertNotNull(sql);
    List<VariableExpression> vars = select.findDescendents(VariableExpression.class);
    VariableExpression var = vars.get(0);
    assertNotNull(var);
    assertEquals(":val", var.getName());
    assertEquals("?", var.toString());
  }
}
