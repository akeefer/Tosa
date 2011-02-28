package tosa.loader.parser.mysql;

import junit.framework.TestCase;
import org.junit.Test;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.TableData;

import java.sql.Types;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests for the tosa.loader.parser.mysql.MySQL51ParserTest class.
 */
public class MySQL51SQLParserTest {

  @Test
  public void simpleTableCreation() {
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

  @Test
  public void bitDataType() {
    assertColumnDataType("BIT", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT));
    assertColumnDataType("bIt", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT));
    assertColumnDataType("BIT(1)", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT));
    // TODO - AHK
//    assertColumnDataType("BIT(3)", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT));
  }

  @Test
  public void tinyIntDataType() {
    assertColumnDataType("TINYINT", column("test", DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT));
    assertColumnDataType("tinYiNt", column("test", DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT));
    assertColumnDataType("TINYINT(8)", column("test", DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT));
    assertColumnDataType("TINYINT(8) SIGNED", column("test", DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT));
    // TINYINT(1) is a bit/boolean
    assertColumnDataType("TINYINT(1)", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
    // TODO - AHK
//    assertColumnDataType("TINYINT UNSIGNED", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
//    assertColumnDataType("TINYINT ZEROFILL", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
  }

  @Test
  public void boolDataType() {
    // BOOL and BOOLEAN are equivalent to TINYINT(1)
    assertColumnDataType("BOOL", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
    assertColumnDataType("bOoL", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
    assertColumnDataType("BOOLEAN", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
    assertColumnDataType("boOlEan", column("test", DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT));
  }


  @Test
  public void smallIntDataType() {
    assertColumnDataType("SMALLINT", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
    assertColumnDataType("sMaLLiNt", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
    assertColumnDataType("SMALLINT(1)", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
    assertColumnDataType("SMALLINT(16)", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
    assertColumnDataType("SMALLINT(16) SIGNED", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
    // TODO - AHK
//    assertColumnDataType("SMALLINT UNSIGNED", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
//    assertColumnDataType("SMALLINT ZEROFILL", column("test", DBColumnTypeImpl.SHORT_ITYPE, Types.SMALLINT));
  }

  @Test
  public void mediumIntDataType() {
    assertColumnDataType("MEDIUMINT", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("meDiUmiNt", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("MEDIUMINT(1)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("MEDIUMINT(32)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("MEDIUMINT(32) SIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    // TODO - AHK
//    assertColumnDataType("MEDIUMINT UNSIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
//    assertColumnDataType("MEDIUMINT ZEROFILL", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
  }

  // TODO - AHK - Can ZEROFILL and SIGNED/UNSIGNED be combined?

  @Test
  public void intDataType() {
    assertColumnDataType("INT", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("inT", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INT(1)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INT(32)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INT(32) SIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    // TODO - AHK
//    assertColumnDataType("INT(32) UNSIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
//    assertColumnDataType("INT(32) ZEROFILL", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));

    assertColumnDataType("INTEGER", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("inTeGEr", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INTEGER(1)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INTEGER(32)", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("INTEGER(32) SIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    // TODO - AHK
//    assertColumnDataType("INTEGER(32) UNSIGNED", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
//    assertColumnDataType("INTEGER(32) ZEROFILL", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
  }

  @Test
  public void bigIntDataType() {
    assertColumnDataType("BIGINT", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
    assertColumnDataType("bIgINt", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
    assertColumnDataType("BIGINT(1)", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
    assertColumnDataType("BIGINT(64)", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
    assertColumnDataType("BIGINT(64) SIGNED", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
    // TODO - AHK
//    assertColumnDataType("BIGINT(64) UNSIGNED", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
//    assertColumnDataType("BIGINT(64) ZEROFILL", column("test", DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT));
  }

  @Test
  public void doubleDataType() {
    assertColumnDataType("DOUBLE", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("doUbLE", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE(10, 2)", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE(10, 2) SIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE(10, 2) UNSIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE(10, 2) ZEROFILL", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));

    assertColumnDataType("DOUBLE PRECISION", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("doUbLE preCiSion", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE PRECISION(10, 2)", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE PRECISION(10, 2) SIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE PRECISION(10, 2) UNSIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("DOUBLE PRECISION(10, 2) ZEROFILL", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    // TODO - AHK - Assert that DOUBLE(10) is invalid?
  }

  @Test
  public void realDataType() {
    assertColumnDataType("REAL", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("ReAl", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("REAL(10, 2)", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("REAL(10, 2) SIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("REAL(10, 2) UNSIGNED", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    assertColumnDataType("REAL(10, 2) ZEROFILL", column("test", DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE));
    // TODO - AHK - Assert that REAL(10) is invalid?
    // TODO - AHK - Test the REAL_AS_FLOAT flag?
  }

  @Test
  public void floatDataType() {
    assertColumnDataType("FLOAT", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    assertColumnDataType("fLoAt", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    assertColumnDataType("FLOAT(10, 2)", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    assertColumnDataType("FLOAT(10, 2) SIGNED", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    assertColumnDataType("FLOAT(10, 2) UNSIGNED", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    assertColumnDataType("FLOAT(10, 2) ZEROFILL", column("test", DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT));
    // TODO - AHK - Assert that FLOAT(10) is invalid?
  }

  @Test
  public void decimalDataType() {
    assertColumnDataType("DECIMAL", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("deCimAl", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DECIMAL(10)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DECIMAL(10, 2)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DECIMAL(10, 2) sIgNEd", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DECIMAL unSignED", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DECIMAL(10) zEroFILL", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));

    assertColumnDataType("DEC", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("dEC", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DEC(10)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DEC(10, 2)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DEC(10, 2) sIgNEd", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DEC unSignED", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("DEC(10) zEroFILL", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));

    assertColumnDataType("NUMERIC", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("nUMeRIC", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("NUMERIC(10)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("NUMERIC(10, 2)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("NUMERIC(10, 2) sIgNEd", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("NUMERIC unSignED", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("NUMERIC(10) zEroFILL", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));

    assertColumnDataType("FIXED", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("fIxEd", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("FIXED(10)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("FIXED(10, 2)", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("FIXED(10, 2) sIgNEd", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("FIXED unSignED", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));
    assertColumnDataType("FIXED(10) zEroFILL", column("test", DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL));

    // TODO - Test that a numeric data type with zero precision should be a BigInteger instead
  }
  /* private DBColumnTypeImpl parseDateColumnType() {
    if (accept(DATE)) {
      return new DBColumnTypeImpl(DATE, DATE, DBColumnTypeImpl.DATE_ITYPE, Types.DATE, new DateColumnTypePersistenceHandler());
    } else if (accept(TIME)) {
      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + TIME);
      return null;
    } else if (accept(TIMESTAMP)) {
      return new DBColumnTypeImpl(TIMESTAMP, TIMESTAMP, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
    } else if (accept(DATETIME)) {
      return new DBColumnTypeImpl(DATETIME, DATETIME, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
    } else if (accept(YEAR)) {
      return new DBColumnTypeImpl(YEAR, YEAR, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
    } else {
      return null;
    }
  }*/

  /*} else if (accept(DOUBLE) || accept(DOUBLE, PRECISION) || accept(REAL)) {
      // TODO - AHK - If the REAL_AS_FLOAT mode is set on the DB, this will be incorrect
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      return new DBColumnTypeImpl(DOUBLE, DOUBLE, DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE);
    } else if (accept(FLOAT)) {
      // TODO - AHK - It's a different deal if there's a length and a precision versus just a single number
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      return new DBColumnTypeImpl(FLOAT, FLOAT, DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT);
    } else if (accept(DECIMAL) || accept(DEC) || accept(NUMERIC) || accept(FIXED)) {
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      // TODO - AHK - The precision and size are probably important here
      // TODO - AHK - If there precision is 0, should this be a BigInteger?
      return new DBColumnTypeImpl(DECIMAL, DECIMAL, DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL);
    } else {
      return null;
    }*/
  /*data_type:
  | DATE
  | TIME
  | TIMESTAMP
  | DATETIME
  | YEAR
  | CHAR[(length)]
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | VARCHAR(length)
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | BINARY[(length)]
  | VARBINARY(length)
  | TINYBLOB
  | BLOB
  | MEDIUMBLOB
  | LONGBLOB
  | TINYTEXT [BINARY]
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | TEXT [BINARY]
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | MEDIUMTEXT [BINARY]
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | LONGTEXT [BINARY]
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | ENUM(value1,value2,value3,...)
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | SET(value1,value2,value3,...)
      [CHARACTER SET charset_name] [COLLATE collation_name]
  | spatial_type*/

  // ------------------------------- Private helper methods

  private void assertColumnDataType(String dataTypeDefinition, ColumnAssertionData expect) {
    List<TableData> tableData = parse("CREATE TABLE \"Bar\"(\n" +
        "    \"test\" " + dataTypeDefinition + "\n" +
        ");");
    assertSingleTable(table("Bar", expect), tableData);
  }

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
