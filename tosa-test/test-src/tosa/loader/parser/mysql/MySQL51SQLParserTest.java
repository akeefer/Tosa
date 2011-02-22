package tosa.loader.parser.mysql;

import junit.framework.TestCase;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.TableData;

import java.sql.Types;
import java.util.List;

/**
 * Tests for the tosa.loader.parser.mysql.MySQL51ParserTest class.
 */
public class MySQL51SQLParserTest extends TestCase {

  public void testSimpleTableCreation() {
    List<TableData> tableData = parse("CREATE TABLE \"Bar\"(\n" +
        "    \"id\" INT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Misc\" VARCHAR(50)\n" +
        ");");

    assertSingleTable(table("Bar",
            column("id", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER),
            column("Date", DBColumnTypeImpl.DATE_ITYPE, Types.DATE),
            column("Misc", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR)),
        tableData);
  }

  // ------------------------------- Private helper methods

  private void assertSingleTable(TableAssertionData expected, List<TableData> tables) {
    assertEquals(1, tables.size());
    assertTableData(expected, tables.get(0));
  }

  private void assertTableData(TableAssertionData expected, TableData actual) {
    assertEquals(expected._name, actual.getName());
    assertEquals(expected._columns.length, actual.getColumns().size());
    for (int i = 0; i < expected._columns.length; i++) {
      assertColumnData(expected._columns[i], actual.getColumns().get(i));
    }
  }

  private void assertColumnData(ColumnAssertionData expected, ColumnData actual) {
    assertEquals(expected._name, actual.getName());
    assertEquals(expected._gosuType, actual.getColumnType().getGosuTypeName());
    assertEquals(expected._jdbcType, actual.getColumnType().getJdbcType());
    // TODO - Persistence handler?
  }

  private TableAssertionData table(String name, ColumnAssertionData... columns) {
    return new TableAssertionData(name, columns);
  }

  private ColumnAssertionData column(String name, String gosuType, int jdbcType) {
    return new ColumnAssertionData(name, gosuType, jdbcType);
  }

  private List<TableData> parse(String sql) {
    return new MySQL51SQLParser().parseDDLFile(sql);
  }

  private static class TableAssertionData {
    private String _name;
    private ColumnAssertionData[] _columns;

    private TableAssertionData(String name, ColumnAssertionData[] columns) {
      _name = name;
      _columns = columns;
    }
  }

  private static class ColumnAssertionData {
    private String _name;
    private String _gosuType;
    private int _jdbcType;

    private ColumnAssertionData(String name, String gosuType, int jdbcType) {
      _name = name;
      _gosuType = gosuType;
      _jdbcType = jdbcType;
    }
  }
}
