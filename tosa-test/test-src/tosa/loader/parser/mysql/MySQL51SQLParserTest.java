package tosa.loader.parser.mysql;

import junit.framework.TestCase;
import org.junit.Test;
import tosa.DBConnection;
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

  @Test
  public void dateColumnType() {
    assertColumnDataType("DATE", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.DATE));
    assertColumnDataType("dAtE", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.DATE));
  }

  @Test
  public void timeColumnType() {
    // TODO - AHK
  }

  @Test
  public void timestampColumnType() {
    assertColumnDataType("TIMESTAMP", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP));
    assertColumnDataType("tiMeSTamp", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP));
  }

  @Test
  public void datetimeColumnType() {
    assertColumnDataType("DATETIME", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP));
    assertColumnDataType("daTeTIme", column("test", DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP));
  }

  @Test
  public void yearColumnType() {
    assertColumnDataType("YEAR", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
    assertColumnDataType("yEar", column("test", DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER));
  }

  @Test
  public void binaryDataType() {
    assertColumnDataType("BINARY", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
    assertColumnDataType("biNAry", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
    assertColumnDataType("BINARY(200)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));

    assertColumnDataType("CHAR BYTE", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
    assertColumnDataType("cHAr byTE", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
    assertColumnDataType("cHAr byTE(200)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
  }

  @Test
  public void charDataType() {
    assertColumnDataType("CHAR", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("cHaR", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) chARACter sET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) collATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) asCIi", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) uNIcoDE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));

    assertColumnDataType("CHAR COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHAR(200) CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));

    assertColumnDataType("CHAR(200) CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
  }

  @Test
  public void characterDataType() {
    assertColumnDataType("CHARACTER", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("cHarACter", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) chARACter sET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) collATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) asCIi", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) uNIcoDE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));

    assertColumnDataType("CHARACTER COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("CHARACTER(200) CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));

    assertColumnDataType("CHARACTER(200) CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY));
  }

  @Test
  public void nationalCharDataType() {
    // TODO - AHK - Test that a characterset, etc. can't be specified
    assertColumnDataType("NATIONAL CHAR", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("naTIoNaL ChAr", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NATIONAL CHAR(200)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NATIONAL CHAR(200) COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NATIONAL CHAR(200) collATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
  }

  @Test
  public void ncharDataType() {
    // TODO - AHK - Test that a characterset, etc. can't be specified
    assertColumnDataType("NCHAR", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("nChAR", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NCHAR(200)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NCHAR(200) COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
    assertColumnDataType("NCHAR(200) collATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CHAR));
  }

  @Test
  public void varcharDataType() {
    // TODO - AHK - Test that a length is required
    // TODO - AHK - Test that the length is a valid value
    assertColumnDataType("VARCHAR(100)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));
    assertColumnDataType("VARCHAR(100) BINARY", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR));

    assertColumnDataType("VARCHAR(100) CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY));
  }

  @Test
  public void varbinaryDataType() {
    assertColumnDataType("VARBINARY(100)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY));
    assertColumnDataType("vARbinARY(100)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY));
  }

  @Test
  public void tinyBlobDataType() {
    assertColumnDataType("TINYBLOB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("tiNyBLoB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void blobDataType() {
    assertColumnDataType("BLOB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("bLOb", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("BLOB(1024)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("bLoB(1024)", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void mediumBlobDataType() {
    assertColumnDataType("MEDIUMBLOB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("mEdIUMBloB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void longBlobDataType() {
    assertColumnDataType("LONGBLOB", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
    assertColumnDataType("lONGBlob", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void tinyTextDataType() {
    assertColumnDataType("TINYTEXT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("tinYTeXt", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TINYTEXT BINARY", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));

    assertColumnDataType("TINYTEXT CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void textDataType() {
    assertColumnDataType("TEXT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("tExT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT(100)", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("TEXT BINARY", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));

    assertColumnDataType("TEXT CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void mediumTextDataType() {
    assertColumnDataType("MEDIUMTEXT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("meDiUmTexT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("MEDIUMTEXT BINARY", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));

    assertColumnDataType("MEDIUMTEXT CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  @Test
  public void longTextDataType() {
    assertColumnDataType("LONGTEXT", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("lOngTExt", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT CHARACTER SET latin1 COLLATE binary", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT COLLATE binary CHARACTER SET latin1", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT ASCII", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT UNICODE", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));
    assertColumnDataType("LONGTEXT BINARY", column("test", DBColumnTypeImpl.STRING_ITYPE, Types.CLOB));

    assertColumnDataType("LONGTEXT CHARACTER SET binary", column("test", DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB));
  }

  // TODO - AHK - ENUM
  // TODO - AHK - SET

  /*private DBColumnTypeImpl parseCharacterColumnType() {
    if (accept(CHAR, BYTE) || accept(BINARY)) {
      return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
    } else if (accept(CHAR) || accept(CHARACTER)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
      } else {
        return new DBColumnTypeImpl(CHAR, CHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
      }
    } else if (accept(NATIONAL, CHAR) || accept(NCHAR)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      return new DBColumnTypeImpl(NCHAR, NCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
    } else if (accept(VARCHAR)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
      } else {
        return new DBColumnTypeImpl(VARCHAR, VARCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR);
      }
    } else if (accept(VARBINARY)) {
      return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
    } else if (accept(TINYBLOB)) {
      // Max length is 255
      return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
    } else if (accept(BLOB)) {
      // Max length is 2^16 - 1 if not otherwise specified
      Integer length = parseLength();
      return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
    } else if (accept(MEDIUMBLOB)) {
      // Max length is 2^24 - 1
      return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
    } else if (accept(LONGBLOB)) {
      // Max length is 2^32 - 1
      return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
    } else if (accept(TINYTEXT)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      // Max length is 255
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      } else {
        return new DBColumnTypeImpl(TINYTEXT, TINYTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
      }
    } else if (accept(TEXT)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      // Max length is 2^16 - 1 if not otherwise specified
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      } else {
        return new DBColumnTypeImpl(TEXT, TEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
      }
    } else if (accept(MEDIUMTEXT)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      // Max length is 2^24 - 1
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      } else {
        return new DBColumnTypeImpl(MEDIUMTEXT, MEDIUMTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
      }
    } else if (accept(LONGTEXT)) {
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      // Max length is 2^32 - 1
      if ("binary".equals(characterTypeAttributes._charSet)) {
        return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      } else {
        return new DBColumnTypeImpl(LONGTEXT, LONGTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
      }
    } else if (accept(ENUM)) {
      List<String> values = parseEnumOrSetValueList();
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + ENUM);
      return null;
    } else if (accept(SET)) {
      List<String> values = parseEnumOrSetValueList();
      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + SET);
      return null;
    } else {
      return null;
    }
  }

    private CharacterTypeAttributes parseCharTypeAttributes() {
    CharacterTypeAttributes attributes = new CharacterTypeAttributes();
    attributes._length = parseLength();
    while(parseCharTypeAttribute(attributes)) {
      // Loop
    }
    return attributes;
  }

  private boolean parseCharTypeAttribute(CharacterTypeAttributes charTypeAttributes) {
    // TODO - AHK - Should be an error if the char set or collation is already set
    if (accept(CHARACTER, SET)) {
      charTypeAttributes._charSet = consumeToken();
      return true;
    } else if (accept(COLLATE)) {
      charTypeAttributes._collation = consumeToken();
      return true;
    } else if (accept(ASCII)) {
      charTypeAttributes._charSet = "latin1";
      return true;
    } else if (accept(UNICODE)) {
      charTypeAttributes._charSet = "ucs2";
      return true;
    } else if (accept(BINARY)) {
      charTypeAttributes._collation = "binary";
      return true;
    } else {
      return false;
    }
  }
  */

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
