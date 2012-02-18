package tosa.loader.parser;

import org.junit.Test;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.tree.SelectStatement;
import tosa.loader.parser.tree.VariableExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SelectParsingBootstrapTest {

  @Test public void bootstrapSelectTest() {
    QueryParser parser = new QueryParser(Token.tokenize("SELECT * FROM foo WHERE foo.bar = 10"), makeSampleDBData());
    SelectStatement select = parser.parseTopLevelSelect();
    assertNotNull(select);
    assertNotNull(select.firstToken().toString());
    String sql = select.toSQL();
    System.out.println(sql);
    assertNotNull(sql);
  }

  private DBData makeSampleDBData() {
    return new DBData("example", makeSampleTableData(), null);
  }

  private List<TableData> makeSampleTableData() {
    ArrayList<TableData> tableDatas = new ArrayList<TableData>();
    tableDatas.add(new TableData("foo",
      Arrays.asList(new ColumnData("bar", DBColumnTypeImpl.INT, null)),
      null
    ));
    return tableDatas;
  }

  @Test public void bootstrapVariableTest() {
    QueryParser parser = new QueryParser(Token.tokenize("SELECT * \n" +
                                                 "FROM foo \n" +
                                                 "WHERE foo.bar = :val"), makeSampleDBData());
    SelectStatement select = parser.parseTopLevelSelect();
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
