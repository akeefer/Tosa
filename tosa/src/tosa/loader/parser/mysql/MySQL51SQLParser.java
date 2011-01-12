package tosa.loader.parser.mysql;

import tosa.loader.DBTypeData;
import tosa.loader.data.ColumnData;
import tosa.loader.data.ColumnType;
import tosa.loader.data.TableData;
import tosa.loader.parser.ISQLParser;
import tosa.loader.parser.SQLParserConstants;
import tosa.loader.parser.SQLTokenizer;
import tosa.loader.parser.Token;
import tosa.loader.parser.tree.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySQL51SQLParser implements SQLParserConstants, ISQLParser {

  public static void main(String[] args) {
    String testSQL =
        "CREATE TABLE \"BlogInfo\"(\n" +
            "    \"id\" BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
            "    \"Title\" VARCHAR(255)\n" +
            ");\n" +
            "CREATE TABLE \"Post\"(\n" +
            "    \"id\" BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
            "    \"Title\" TEXT,\n" +
            "    \"Body\" TEXT,\n" +
            "    \"Posted\" TIMESTAMP\n" +
            ");\n" +
            "CREATE TABLE \"User\"(\n" +
            "    \"id\" BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
            "    \"Name\" VARCHAR(64),\n" +
            "    \"Hash\" VARCHAR(128),\n" +
            "    \"Salt\" VARCHAR(32)\n" +
            ");\n" +
            "CREATE TABLE \"Comment\"(\n" +
            "    \"id\" BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
            "    \"Post_id\" BIGINT,\n" +
            "    \"Name\" VARCHAR(255),\n" +
            "    \"Text\" TEXT,\n" +
            "    \"Posted\" TIMESTAMP\n" +
            ");\n";

    List<TableData> tables = new MySQL51SQLParser().parseDDLFile(testSQL);
    System.out.println("Done");
  }

  private SQLTokenizer _tokenizer;

  @Override
  public List<TableData> parseDDLFile(String fileContents) {
    _tokenizer = new SQLTokenizer(fileContents);
    List<TableData> tables = new ArrayList<TableData>();
    // TODO - AHK - Other Create calls?  Other stuff?  Closing semi-colon?
    for (TableData table = parseCreate(); table != null; table = parseCreate()) {
      tables.add(table);
    }
    return tables;
  }

  private String consumeToken() {
    return _tokenizer.consumeToken();
  }

  private boolean accept(String... tokens) {
    return _tokenizer.acceptIgnoreCase(tokens);
  }

  private boolean peek(String... tokens) {
    return _tokenizer.peekIgnoreCase(tokens);
  }

  private void expect(String expected) {
    _tokenizer.expectIgnoreCase(expected);
  }

  private String stripQuotes(String str) {
    if (str.startsWith("\"")) {
      str = str.substring(1);
    }
    if (str.endsWith("\"")) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

  // The grammar for this parser is based on the documentation at http://dev.mysql.com/doc/refman/5.1/en/create-table.html

  //================================== Parsing Methods ========================

  /*CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
    (create_definition,...)
    [table_options]
    [partition_options]
Or:

CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
    [(create_definition,...)]
    [table_options]
    [partition_options]
    select_statement
Or:

CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
    { LIKE old_tbl_name | (LIKE old_tbl_name) }*/
  private TableData parseCreate() {
    if (accept(CREATE)) {
      accept(TEMPORARY); // Discard; we don't care
      expect(TABLE);
      accept(IF, NOT, EXISTS);
      String tableName = stripQuotes(consumeToken());
      List<ColumnData> columns = null;
      if (accept(LIKE)) {
        String likeTableName = consumeToken();
        // TODO - AHK - And what do we do with it, exactly?
      } else if (accept(OPEN_PAREN, LIKE)) {
        String likeTableName = consumeToken();
        expect(CLOSE_PAREN);
        // TODO - AHK - And what do we do with it, exactly?
      } else if (accept(OPEN_PAREN)) {
        columns = parseCreateDefinitions();
        expect(CLOSE_PAREN);
        parseTableOptions();
        parsePartitionOptions();
        parseSelectStatement();
      } else {
        // No columns, but there must be a select statement
        parseTableOptions();
        parsePartitionOptions();
        parseSelectStatement();
      }

      expect(SEMI_COLON);

      return new TableData(tableName, columns);
    } else {
      return null;
    }
  }

  private List<ColumnData> parseCreateDefinitions() {
    // TODO - AHK - Is it legal to have 0 columns?
    List<ColumnData> columns = new ArrayList<ColumnData>();
    ColumnData columnData = parseCreateDefinition();
    if (columnData != null) {
      columns.add(columnData);
    }
    while (accept(COMMA)) {
      columnData = parseCreateDefinition();
      if (columnData != null) {
        columns.add(columnData);
      }
    }
    return columns;
  }

  /*create_definition:
    col_name column_definition
  | [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
      [index_option] ...
  | {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
      [index_option] ...
  | [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY]
      [index_name] [index_type] (index_col_name,...)
      [index_option] ...
  | {FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...)
      [index_option] ...
  | [CONSTRAINT [symbol]] FOREIGN KEY
      [index_name] (index_col_name,...) reference_definition
  | CHECK (expr)*/
  private ColumnData parseCreateDefinition() {
    if (accept(CONSTRAINT)) {
      String symbolName;
      if (peek(PRIMARY, KEY) || peek(UNIQUE) || peek(FOREIGN, KEY)) {
        symbolName = null;
      } else {
        symbolName = consumeToken();
      }
    }

    if (accept(PRIMARY, KEY)) {
      parseIndexType();
      parseIndexColumnNames();
      parseIndexOptions();
    } else if (accept(INDEX) || accept(KEY)) {
      parseIndexName();
      parseIndexType();
      parseIndexColumnNames();
      parseIndexOptions();
    } else if (accept(UNIQUE)) {
      // TODO - AHK
      if (!accept(INDEX)) {
        expect(KEY);
      }
      parseIndexName();
      parseIndexType();
      parseIndexColumnNames();
      parseIndexOptions();
    } else if (accept(FULLTEXT) || accept(SPATIAL)) {
      if (!accept(INDEX)) {
        expect(KEY);
      }
      parseIndexName();
      parseIndexColumnNames();
      parseIndexOptions();
    } else if (accept(FOREIGN, KEY)) {
      parseIndexName();
      parseIndexColumnNames();
      parseReferenceDefinition();
    } else if (accept(CHECK)) {
      parseParenthesizedExpression();
    } else {
      return parseColumnDefinition();
    }

    return null;
  }

  private void parseIndexName() {
    if (!(peek(USING) || peek(OPEN_PAREN))) {
      String name = consumeToken();
    }
  }

  private void parseIndexType() {
    if (accept(USING)) {
      if (!accept(BTREE)) {
        expect(HASH);
      }
    }
  }

  private void parseIndexOptions() {
    if (accept(KEY_BLOCK_SIZE)) {
      accept(EQUALS);
      String value = consumeToken();
      parseIndexOptions();
    } else if (accept(USING)) {
      // TODO - AHK - Sould there be an expect() variant for an OR situation like this?
      if (!accept(BTREE)) {
        expect(HASH);
      }
      parseIndexOptions();
    } else if (accept(WITH, PARSER)) {
      String parserName = consumeToken();
    }
  }

  // TODO - AHK - This needs to be order-independent
  private boolean parseReferenceDefinition() {
    if (accept(REFERENCES)) {
      String tableName = consumeToken();
      expect(OPEN_PAREN);
      String columnName = consumeToken();
      while (accept(COMMA)) {
        columnName = consumeToken();
      }
      expect(CLOSE_PAREN);
      if (accept(MATCH, FULL) || accept(MATCH, PARTIAL) || accept(MATCH, SIMPLE)) {
        // Just eat it
      }
      if (accept(ON, DELETE)) {
        parseReferenceOption();
      }
      if (accept(ON, UPDATE)) {
        parseReferenceOption();
      }

      return true;
    }

    return false;
  }

  private void parseReferenceOption() {
    if (accept(RESTRICT) || accept(CASCADE) || accept(SET, NULL) || accept(NO, ACTION)) {
      // Just eat it
    } else {
      // Error case
    }
  }

  private void parseIndexColumnNames() {
    expect(OPEN_PAREN);
    parseIndexColName();
    while (accept(COMMA)) {
      parseIndexColName();
    }
    expect(CLOSE_PAREN);
  }

  private void parseIndexColName() {
    String columnName = consumeToken();
    if (accept(OPEN_PAREN)) {
      String length = consumeToken();
      expect(CLOSE_PAREN);
    }
    if (accept(ASC)) {
      // Just eat it
    } else if (accept(DESC)) {
      // Just eat it
    }
  }

  /*column_definition:
    data_type [NOT NULL | NULL] [DEFAULT default_value]
      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
      [COMMENT 'string']
      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
      [STORAGE {DISK|MEMORY|DEFAULT}]
      [reference_definition]*/
  private ColumnData parseColumnDefinition() {
    // Note:  In the syntax defined in the MySQL docs they don't include the name as part of the column_definition
    // production, but I'm moving it in there for the sake of sanity
    String name = consumeToken();
    name = stripQuotes(name);
    ColumnType columnType = parseDataType();
    while (parseColumnOption()) {
      // Keep looping to consume all the options
    }
    // TODO - AHK
    return new ColumnData(name, columnType);
  }

  /*
    [NOT NULL | NULL] [DEFAULT default_value]
      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
      [COMMENT 'string']
      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
      [STORAGE {DISK|MEMORY|DEFAULT}]
      [reference_definition]*/
  private boolean parseColumnOption() {
    if (accept(NOT, NULL)) {
      return true;
    } else if (accept(NULL)) {
      return true;
    }

    if (accept(DEFAULT)) {
      String defaultValue = consumeToken();
      return true;
    }

    if (accept(AUTO_INCREMENT)) {
      return true;
    }

    if (accept(UNIQUE)) {
      accept(KEY);
      return true;
    } else if (accept(PRIMARY)) {
      expect(KEY);
      return true;
    } else if (accept(KEY)) {
      return true;
    }

    if (accept(COMMENT)) {
      String comment = parseQuotedString();
      return true;
    }

    if (accept(COLUMN_FORMAT)) {
      if (accept(FIXED) || accept(DYNAMIC) || accept(DEFAULT)) {
        return true;
      } else {
        // TODO - AHK - Error case
        return true;
      }
    }

    if (accept(STORAGE)) {
      if (accept(DISK) || accept(MEMORY) || accept(DEFAULT)) {
        return true;
      } else {
        // TODO - AHK - Error case
        return true;
      }
    }

    if (parseReferenceDefinition()) {
      return true;
    }

    return false;
  }

  /*
  data_type:
    BIT[(length)]
  | TINYINT[(length)] [UNSIGNED] [ZEROFILL]
  | SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
  | MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
  | INT[(length)] [UNSIGNED] [ZEROFILL]
  | INTEGER[(length)] [UNSIGNED] [ZEROFILL]
  | BIGINT[(length)] [UNSIGNED] [ZEROFILL]
  | REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
  | DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
  | FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
  | DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
  | NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
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
  | spatial_type
   */
  private ColumnType parseDataType() {
    // TODO - Handle Serial
    if (accept(BIT)) {
      Integer length = parseLength();
      if (length == null || length == 1) {
        return new ColumnType(Types.BIT, BIT, ColumnType.BOOLEAN_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type " + BIT);
        return null;
      }
    } else if (accept(TINYINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (length != null && length == 1) {
        // Treat TINYINT(1) as a boolean type
        return new ColumnType(Types.TINYINT, TINYINT, ColumnType.BOOLEAN_ITYPE);
      } else if (signed) {
        return new ColumnType(Types.TINYINT, TINYINT, ColumnType.BYTE_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type UNSIGNED " + TINYINT);
        return null;
      }
    } else if (accept(BOOL) || accept(BOOLEAN)) {
      // BOOL and BOOLEAN are equivalent to TINYINT(1)
      return new ColumnType(Types.TINYINT, TINYINT, ColumnType.BOOLEAN_ITYPE);
    } else if (accept(SMALLINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new ColumnType(Types.SMALLINT, SMALLINT, ColumnType.SHORT_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type UNSIGNED " + SMALLINT);
        return null;
      }
    } else if (accept(MEDIUMINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new ColumnType(Types.INTEGER, MEDIUMINT, ColumnType.INTEGER_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type UNSIGNED " + MEDIUMINT);
        return null;
      }
    } else if (accept(INT) || accept(INTEGER)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new ColumnType(Types.INTEGER, INT, ColumnType.INTEGER_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type UNSIGNED " + INTEGER);
        return null;
      }
    } else if (accept(BIGINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new ColumnType(Types.BIGINT, BIGINT, ColumnType.LONG_ITYPE);
      } else {
        // TODO - AHK
        System.out.println("***Unhandled column type UNSIGNED " + BIGINT);
        return null;
      }
    } else if (accept(DOUBLE) || accept(DOUBLE, PRECISION) || accept(REAL)) {
      // TODO - AHK - If the REAL_AS_FLOAT mode is set on the DB, this will be incorrect
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      return new ColumnType(Types.DOUBLE, DOUBLE, ColumnType.DOUBLE_ITYPE);
    } else if (accept(FLOAT)) {
      // TODO - AHK - It's a different deal if there's a length and a precision versus just a single number
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      return new ColumnType(Types.FLOAT, FLOAT, ColumnType.FLOAT_ITYPE);
    } else if (accept(DECIMAL) || accept(DEC) || accept(NUMERIC) || accept(FIXED)) {
      parseLengthAndDecimals();
      boolean signed = parseNumericModifiers();
      // TODO - AHK - The precision and size are probably important here
      // TODO - AHK - If there precision is 0, should this be a BigInteger?
      return new ColumnType(Types.DECIMAL, DECIMAL, ColumnType.BIG_DECIMAL_ITYPE);
    } else if (accept(DATE)) {
      // TODO - AHK
//      return new ColumnType(Types.DATE, DATE, ColumnType.DATE_ITYPE);
      return new ColumnType(Types.DATE, DATE, "java.sql.Date");
    } else if (accept(TIME)) {
      // TODO - AHK
      return new ColumnType(Types.TIME, TIME, ColumnType.DATE_ITYPE);
    } else if (accept(TIMESTAMP)) {
      // TODO - AHK
      return new ColumnType(Types.TIMESTAMP, TIMESTAMP, ColumnType.DATE_ITYPE);
    } else if (accept(DATETIME)) {
      // TODO - AHK
      return new ColumnType(Types.TIMESTAMP, DATETIME, ColumnType.DATE_ITYPE);
    } else if (accept(YEAR)) {
      // TODO - AHK
      return new ColumnType(Types.INTEGER, YEAR, ColumnType.INTEGER_ITYPE);
    } else if (accept(CHAR) || accept(CHARACTER) || accept(NATIONAL, CHAR) || accept(NCHAR)) {
      // TODO - AHK - If the charSetName is "binary", then it's really a binary column . . . ugh
      Integer length = parseLength();
      String charSetName = parseCharSet();
      String collation = parseCollation();
      return new ColumnType(Types.CHAR, CHAR, ColumnType.STRING_ITYPE);
    } else if (accept(VARCHAR)) {
      // TODO - AHK - If the charSetName is "binary", then it's really a binary column . . . ugh
      Integer length = parseLength();
      String charSetName = parseCharSet();
      String collation = parseCollation();
      return new ColumnType(Types.VARCHAR, VARCHAR, ColumnType.STRING_ITYPE);
    } else if (accept(BINARY) || accept(CHAR, BYTE)) {
      Integer length = parseLength();
      // TODO - AHK
      System.out.println("***Unhandled column type " + BINARY);
      return null;
    } else if (accept(VARBINARY)) {
      Integer length = parseLength();
      // TODO - AHK
      System.out.println("***Unhandled column type " + VARBINARY);
      return null;
    } else if (accept(TINYBLOB)) {
      // TODO - AHK
      System.out.println("***Unhandled column type " + TINYBLOB);
      return null;
    } else if (accept(BLOB)) {
      // TODO - AHK
      System.out.println("***Unhandled column type " + BLOB);
      return null;
    } else if (accept(MEDIUMBLOB)) {
      // TODO - AHK
      System.out.println("***Unhandled column type " + MEDIUMBLOB);
      return null;
    } else if (accept(LONGBLOB)) {
      // TODO - AHK
      System.out.println("***Unhandled column type " + LONGBLOB);
      return null;
    } else if (accept(TINYTEXT)) {
      accept(BINARY);
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + TINYTEXT);
      return null;
    } else if (accept(TEXT)) {
      accept(BINARY);
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + TEXT);
      return null;
    } else if (accept(MEDIUMTEXT)) {
      accept(BINARY);
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + MEDIUMTEXT);
      return null;
    } else if (accept(LONGTEXT)) {
      accept(BINARY);
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + LONGTEXT);
      return null;
    } else if (accept(ENUM)) {
      List<String> values = parseEnumOrSetValueList();
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + ENUM);
      return null;
    } else if (accept(SET)) {
      List<String> values = parseEnumOrSetValueList();
      String charSetName = parseCharSet();
      String collation = parseCollation();
      // TODO - AHK
      System.out.println("***Unhandled column type " + SET);
      return null;
    } else {
      System.out.println("***Unexpected column type");
      return null;
    }
  }

  // TODO - AHK - Maybe return an int instead?
  private Integer parseLength() {
    if (accept(OPEN_PAREN)) {
      String length = consumeToken();
      expect(CLOSE_PAREN);
      return Integer.valueOf(length);
    } else {
      return null;
    }
  }

  private boolean parseNumericModifiers() {
    boolean signed = true;
    if (accept(SIGNED)) {
      signed = true;
    } else if (accept(UNSIGNED)) {
      signed = false;
    }

    // Zerofill columns are automatically treated as unsigned
    if (accept(ZEROFILL)) {
      signed = false;
    }

    return signed;
  }

  private void parseLengthAndDecimals() {
    // TODO - AHK - Sometimes the comma isn't optional, but I don't think that matters here
    if (accept(OPEN_PAREN)) {
      String length = consumeToken();
      if (accept(COMMA)) {
        String decimals = consumeToken();
      }
      expect(CLOSE_PAREN);
    }
  }

  private String parseCharSet() {
    if (accept(CHARACTER, SET)) {
      return consumeToken();
    } else {
      return null;
    }
  }

  private String parseCollation() {
    if (accept(COLLATE)) {
      return consumeToken();
    } else {
      return null;
    }
  }

  private List<String> parseEnumOrSetValueList() {
    List<String> values = new ArrayList<String>();
    expect(OPEN_PAREN);
    values.add(consumeToken());
    while (accept(COMMA)) {
      values.add(consumeToken());
    }
    expect(CLOSE_PAREN);
    return values;
  }

  /*table_options:
    table_option [[,] table_option] ...*/
  private void parseTableOptions() {
    parseTableOption();
    // TODO - AHK - Are the commas required?  If not, we'll need to see if a table option was
    // actually parsed or not
    while (accept(COMMA)) {
      parseTableOption();
    }
  }

  /*
  table_option:
    ENGINE [=] engine_name
  | AUTO_INCREMENT [=] value
  | AVG_ROW_LENGTH [=] value
  | [DEFAULT] CHARACTER SET [=] charset_name
  | CHECKSUM [=] {0 | 1}
  | [DEFAULT] COLLATE [=] collation_name
  | COMMENT [=] 'string'
  | CONNECTION [=] 'connect_string'
  | DATA DIRECTORY [=] 'absolute path to directory'
  | DELAY_KEY_WRITE [=] {0 | 1}
  | INDEX DIRECTORY [=] 'absolute path to directory'
  | INSERT_METHOD [=] { NO | FIRST | LAST }
  | KEY_BLOCK_SIZE [=] value
  | MAX_ROWS [=] value
  | MIN_ROWS [=] value
  | PACK_KEYS [=] {0 | 1 | DEFAULT}
  | PASSWORD [=] 'string'
  | ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
  | TABLESPACE tablespace_name [STORAGE {DISK|MEMORY|DEFAULT}]
  | UNION [=] (tbl_name[,tbl_name]...)
  */
  private void parseTableOption() {
    if (accept(ENGINE)) {
      accept(EQUALS);
      String engineName = consumeToken();
    } else if (accept(AUTO_INCREMENT)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(AVG_ROW_LENGTH)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(DEFAULT, CHARACTER, SET) || accept(CHARACTER, SET)) {
      accept(EQUALS);
      String charsetName = consumeToken();
    } else if (accept(CHECKSUM)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(DEFAULT, COLLATE) || accept(COLLATE)) {
      accept(EQUALS);
      String collationName = consumeToken();
    } else if (accept(COMMENT)) {
      accept(EQUALS);
      String comment = consumeToken();
    } else if (accept(CONNECTION)) {
      accept(EQUALS);
      String connection = consumeToken();
    } else if (accept(DATA, DICTIONARY)) {
      accept(EQUALS);
      String path = consumeToken();
    } else if (accept(DELAY_KEY_WRITE)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(INDEX, DIRECTORY)) {
      accept(EQUALS);
      String path = consumeToken();
    } else if (accept(INSERT_METHOD)) {
      accept(EQUALS);
      String method = consumeToken();
    } else if (accept(KEY_BLOCK_SIZE)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(MAX_ROWS)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(MIN_ROWS)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(PACK_KEYS)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(PASSWORD)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(ROW_FORMAT)) {
      accept(EQUALS);
      String value = consumeToken();
    } else if (accept(TABLESPACE)) {
      String tablespaceName = consumeToken();
      if (accept(STORAGE)) {
        // TODO - AHK - Do we care if it's one of the correct tokens?
        consumeToken();
      }
    } else if (accept(UNION)) {
      String unionName = consumeToken();
      while (accept(COMMA)) {
        unionName = consumeToken();
      }
    }
  }

  /*
  partition_options:
    PARTITION BY
        { [LINEAR] HASH(expr)
        | [LINEAR] KEY(column_list)
        | RANGE(expr)
        | LIST(expr) }
    [PARTITIONS num]
    [SUBPARTITION BY
        { [LINEAR] HASH(expr)
        | [LINEAR] KEY(column_list) }
      [SUBPARTITIONS num]
    ]
    [(partition_definition [, partition_definition] ...)]
   */
  private void parsePartitionOptions() {
    if (accept(PARTITION, BY)) {
      if (accept(LINEAR, HASH) || accept(HASH)) {
        parseParenthesizedExpression();
      } else if (accept(KEY) || accept(LINEAR, KEY)) {
        parseParenthesizedExpression();
      } else if (accept(RANGE)) {
        parseParenthesizedExpression();
      } else if (accept(LIST)) {
        parseParenthesizedExpression();
      } else {
        // TODO - AHK - Error case?
      }

      if (accept(PARTITIONS)) {
        String num = consumeToken();
      }

      if (accept(SUBPARTITION, BY)) {
        if (accept(HASH) || accept(LINEAR, HASH)) {
          parseParenthesizedExpression();
        } else if (accept(KEY) || accept(LINEAR, KEY)) {
          parseParenthesizedExpression();
        } else {
          // TODO - AHK - Error case?
        }

        if (accept(SUBPARTITIONS)) {
          String num = consumeToken();
        }
      }

      parsePartitionDefinition();
      while (accept(COMMA)) {
        parsePartitionDefinition();
      }
    }
  }

  private void parseParenthesizedExpression() {
    expect(OPEN_PAREN);
    // TODO - AHK - Deal with escaping and such at some point, but for now just
    // eat tokens until we hit the closing )
    while (!_tokenizer.token().equalsIgnoreCase(CLOSE_PAREN)) {
      consumeToken();
    }
    expect(CLOSE_PAREN);
  }

  /*
  partition_definition:
    PARTITION partition_name
        [VALUES
            {LESS THAN {(expr) | MAXVALUE}
            |
            IN (value_list)}]
        [[STORAGE] ENGINE [=] engine_name]
        [COMMENT [=] 'comment_text' ]
        [DATA DIRECTORY [=] 'data_dir']
        [INDEX DIRECTORY [=] 'index_dir']
        [MAX_ROWS [=] max_number_of_rows]
        [MIN_ROWS [=] min_number_of_rows]
        [TABLESPACE [=] tablespace_name]
        [NODEGROUP [=] node_group_id]
        [(subpartition_definition [, subpartition_definition] ...)]
   */
  private void parsePartitionDefinition() {
    if (accept(PARTITION)) {
      String partitionName = consumeToken();
      if (accept(VALUES)) {
        if (accept(LESS, THAN)) {
          if (accept(MAXVALUE)) {
            // Nothing to do
          } else {
            parseParenthesizedExpression();
          }
        } else if (accept(IN)) {
          expect(OPEN_PAREN);
          parseValueList();
          expect(CLOSE_PAREN);
        } else {
          // TODO - AHK - Error case?
        }
      }

      if (accept(ENGINE) || accept(STORAGE, ENGINE)) {
        accept(EQUALS);
        String engineName = consumeToken();
      }

      if (accept(COMMENT)) {
        accept(EQUALS);
        String commentText = parseQuotedString();
      }

      if (accept(DATA, DIRECTORY)) {
        accept(EQUALS);
        String dataDir = parseQuotedString();
      }

      if (accept(INDEX, DIRECTORY)) {
        accept(EQUALS);
        String indexDir = parseQuotedString();
      }

      if (accept(MAX_ROWS)) {
        accept(EQUALS);
        String value = consumeToken();
      }

      if (accept(MIN_ROWS)) {
        accept(EQUALS);
        String value = consumeToken();
      }

      if (accept(TABLESPACE)) {
        accept(EQUALS);
        String tablespaceName = consumeToken();
      }

      if (accept(NODEGROUP)) {
        accept(EQUALS);
        String nodeGroupID = consumeToken();
      }

      parseSubpartitionDefinition();
      while (accept(COMMA)) {
        parseSubpartitionDefinition();
      }
    }
  }

  private void parseValueList() {
    // TODO - AHK
  }

  private String parseQuotedString() {
    // TODO - AHK
    return null;
  }

  /*
  subpartition_definition:
    SUBPARTITION logical_name
        [[STORAGE] ENGINE [=] engine_name]
        [COMMENT [=] 'comment_text' ]
        [DATA DIRECTORY [=] 'data_dir']
        [INDEX DIRECTORY [=] 'index_dir']
        [MAX_ROWS [=] max_number_of_rows]
        [MIN_ROWS [=] min_number_of_rows]
        [TABLESPACE [=] tablespace_name]
        [NODEGROUP [=] node_group_id]
   */
  private void parseSubpartitionDefinition() {
    if (accept(SUBPARTITION)) {
      String logicalName = consumeToken();

      if (accept(ENGINE) || accept(STORAGE, ENGINE)) {
        accept(EQUALS);
        String engineName = consumeToken();
      }

      if (accept(COMMENT)) {
        accept(EQUALS);
        String commentText = parseQuotedString();
      }

      if (accept(DATA, DIRECTORY)) {
        accept(EQUALS);
        String dataDir = parseQuotedString();
      }

      if (accept(INDEX, DIRECTORY)) {
        accept(EQUALS);
        String indexDir = parseQuotedString();
      }

      if (accept(MAX_ROWS)) {
        accept(EQUALS);
        String value = consumeToken();
      }

      if (accept(MIN_ROWS)) {
        accept(EQUALS);
        String value = consumeToken();
      }

      if (accept(TABLESPACE)) {
        accept(EQUALS);
        String tablespaceName = consumeToken();
      }

      if (accept(NODEGROUP)) {
        accept(EQUALS);
        String nodeGroupID = consumeToken();
      }
    }
  }

  private void parseSelectStatement() {
    // TODO - AHK
  }

  @Override
  public SelectStatement parseSQLFile(DBTypeData dbData, String fileContents) {
    Token t = Token.tokenize(fileContents);
    if (t.match(SELECT)) {
      SQLParsedElement quantifier = parseSetQuantifers(t.nextToken());
      SQLParsedElement selectList = parseSelectList(nextToken(quantifier, t));
      TableExpression tableExpr = parseTableExpression(selectList.nextToken());
      SelectStatement select = new SelectStatement(t, tableExpr.lastToken(), quantifier, selectList, tableExpr);
      verify(dbData, select);
      return select;
    }
    return null;
  }

  private void verify(DBTypeData dbData, SelectStatement select) {
    select.verify(dbData);
  }

  private Token nextToken(SQLParsedElement elt, Token t) {
    return elt != null ? elt.nextToken() : t.nextToken();
  }

  private TableExpression parseTableExpression(Token t) {
    TableFromClause fromClause = parseFromClause(t);
    SQLParsedElement whereClause = parseWhereClause(fromClause.nextToken());

    TableExpression table = new TableExpression(t, lastTokenOf(t, fromClause, whereClause), fromClause, whereClause);
    return table;
  }

  private Token lastTokenOf(Token defaultTok, SQLParsedElement... possibleElts) {
    List<SQLParsedElement> lst = new ArrayList<SQLParsedElement>(Arrays.asList(possibleElts));
    Collections.reverse(lst);
    for (SQLParsedElement elt : lst) {
      if (elt != null) {
        return elt.lastToken();
      }
    }
    return defaultTok;
  }

  private SQLParsedElement parseWhereClause(Token token) {
    if (token.match(WHERE)) {
      return new WhereClause(token, parseSearchOrExpression(token.nextToken()));
    } else if(token.isEOF()) {
      return null;
    } else {
      throw new IllegalStateException("This should be a parse error.");
    }
  }

  private SQLParsedElement parseSearchOrExpression(Token token) {
    SQLParsedElement lhs = parseSearchAndExpression(token);
    if (lhs.nextToken().match(OR)) {
      SQLParsedElement rhs = parseSearchOrExpression(lhs.nextToken().nextToken());
      return new SQLOrExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchAndExpression(Token token) {
    SQLParsedElement lhs = parseSearchNotExpression(token);
    if (lhs.nextToken().match(AND)) {
      SQLParsedElement rhs = parseSearchAndExpression(lhs.nextToken().nextToken());
      return new SQLAndExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchNotExpression(Token t) {
    if (t.match(NOT)) {
      SQLParsedElement val = parseBooleanTestExpression(t);
      return new SQLNotExpression(t, val);
    } else {
      return parseBooleanTestExpression(t);
    }
  }

  private SQLParsedElement parseBooleanTestExpression(Token t) {
    return parseBooleanPrimaryExpression(t);
    //TODO cgross IS NOT TRUE
  }

  private SQLParsedElement parseBooleanPrimaryExpression(Token t) {
    if (t.match(OPEN_PAREN)) {
      SQLParsedElement elt = parseSearchOrExpression(t);
      t.match(CLOSE_PAREN); //TODO cgross expect
      return elt;
    }

    SQLParsedElement initialValue = parseRowValue(t);
    if (initialValue != null) {
      if (initialValue.nextToken().matchAny(EQ_OP, LT_OP, LTEQ_OP, GT_OP, GTEQ_OP)) {
        SQLParsedElement comparisonValue = parseRowValueOrVariable(initialValue.nextToken().nextToken());
        return new ComparisonPredicate(initialValue, initialValue.nextToken(), comparisonValue);
      }
    }
    return new UnexpectedTokenExpression(t);
  }

  private SQLParsedElement parseRowValueOrVariable(Token t) {
    if (t.getValue().startsWith(":")) {
      return new VariableExpression(t);
    } else {
      return parseValueExpression(t);
    }
  }

  private SQLParsedElement parseRowValue(Token t) {
    //TODO NULL, DEFAULT
    return parseValueExpression(t);
  }

  private SQLParsedElement parseValueExpression(Token t) {

    SQLParsedElement elt = parseNumericValueExpression(t);
    if (elt != null) {
      return elt;
    }

    elt = parseStringValueExpression(t);
    if (elt != null) {
      return elt;
    }
    
    elt = parseDateTimeValueExpression(t);
    if (elt != null) {
      return elt;
    }

    elt = parseIntervalValueExpression(t);
    if (elt != null) {
      return elt;
    }

    return null;
  }

  private SQLParsedElement parseIntervalValueExpression(Token t) {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseDateTimeValueExpression(Token t) {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseStringValueExpression(Token t) {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseNumericValueExpression(Token t) {
    SQLParsedElement lhs = parseTerm(t);
    if (lhs.nextToken().match(PLUS_OP, MINUS_OP)) {
      SQLParsedElement rhs = parseNumericValueExpression(lhs.nextToken().nextToken());
      return new SQLAdditiveExpression(rhs, lhs.nextToken(), rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseTerm(Token t) {
    SQLParsedElement lhs = parseFactor(t);
    if (lhs.nextToken().match(TIMES_OP, DIV_OP)) {
      SQLParsedElement rhs = parseTerm(lhs.nextToken().nextToken());
      return new SQLMultiplicitiveExpression(rhs, lhs.nextToken(), rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseFactor(Token t) {
    if (t.match(PLUS_OP, MINUS_OP)) {
      return new SQLSignedExpression(t, parseNumericPrimary(t.next()));
    } else {
      return parseNumericPrimary(t);
    }
  }

  private SQLParsedElement parseNumericPrimary(Token token) {
    SQLParsedElement numericLiteral = parseNumericLiteral(token);
    if (numericLiteral != null) {
      return numericLiteral;
    }

    return parseColumnReference(token);
  }

  private SQLParsedElement parseColumnReference(Token token) {
    if (token.nextToken().match(".")) {
      return new ColumnReference(token, token.nextToken().nextToken());
    } else {
      return new ColumnReference(token);
    }
  }

  private SQLParsedElement parseNumericLiteral(Token token) {
    try {
      int i = Integer.parseInt(token.getValue());
      return new SQLNumericLiteral(token, i);
    } catch (NumberFormatException e) {
    }
    return null;
  }

  private TableFromClause parseFromClause(Token t) {
    t.match(FROM); //TODO cgross - expect
    ArrayList<SimpleTableReference> refs = new ArrayList<SimpleTableReference>();
    Token currentToken = t;
    do {
      SimpleTableReference ref = parseTableReference(currentToken.nextToken());
      refs.add(ref);
      currentToken = ref.nextToken();
    }
    while (currentToken.match(COMMA));
    return new TableFromClause(t, refs);
  }

  private SimpleTableReference parseTableReference(Token t) {
    return new SimpleTableReference(t);
    //TODO cgross more exotic table references
  }

  private SQLParsedElement parseSelectList(Token t) {
    if (t.match(ASTERISK)) {
      return new AsteriskSelectList(t);
    } else {
      return parseSelectSubList(t);
    }
  }

  private SQLParsedElement parseSelectSubList(Token t) {
    ColumnSelectList sl = new ColumnSelectList(t);
    do {
      if (true) throw new UnsupportedOperationException("Not yet supported");
    } while (accept(COMMA));
    return sl;
  }

  private SQLParsedElement parseSetQuantifers(Token t) {
    if (t.matchAny(DISTINCT, ALL)) {
      return new QuantifierModifier(t.nextToken());
    } else {
      return null;
    }
  }

  /*CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
(create_definition,...)
[table_options]
[partition_options]
Or:

CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
[(create_definition,...)]
[table_options]
[partition_options]
select_statement
Or:

CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
{ LIKE old_tbl_name | (LIKE old_tbl_name) }
create_definition:
col_name column_definition
| [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
[index_option] ...
| {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
[index_option] ...
| [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY]
[index_name] [index_type] (index_col_name,...)
[index_option] ...
| {FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...)
[index_option] ...
| [CONSTRAINT [symbol]] FOREIGN KEY
[index_name] (index_col_name,...) reference_definition
| CHECK (expr)

column_definition:
data_type [NOT NULL | NULL] [DEFAULT default_value]
[AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
[COMMENT 'string']
[COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
[STORAGE {DISK|MEMORY|DEFAULT}]
[reference_definition]

data_type:
BIT[(length)]
| TINYINT[(length)] [UNSIGNED] [ZEROFILL]
| SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
| MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
| INT[(length)] [UNSIGNED] [ZEROFILL]
| INTEGER[(length)] [UNSIGNED] [ZEROFILL]
| BIGINT[(length)] [UNSIGNED] [ZEROFILL]
| REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
| DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
| FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
| DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
| NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
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
| spatial_type

index_col_name:
col_name [(length)] [ASC | DESC]

index_type:
USING {BTREE | HASH}

index_option:
KEY_BLOCK_SIZE [=] value
| index_type
| WITH PARSER parser_name

reference_definition:
REFERENCES tbl_name (index_col_name,...)
[MATCH FULL | MATCH PARTIAL | MATCH SIMPLE]
[ON DELETE reference_option]
[ON UPDATE reference_option]

reference_option:
RESTRICT | CASCADE | SET NULL | NO ACTION

table_options:
table_option [[,] table_option] ...

table_option:
ENGINE [=] engine_name
| AUTO_INCREMENT [=] value
| AVG_ROW_LENGTH [=] value
| [DEFAULT] CHARACTER SET [=] charset_name
| CHECKSUM [=] {0 | 1}
| [DEFAULT] COLLATE [=] collation_name
| COMMENT [=] 'string'
| CONNECTION [=] 'connect_string'
| DATA DIRECTORY [=] 'absolute path to directory'
| DELAY_KEY_WRITE [=] {0 | 1}
| INDEX DIRECTORY [=] 'absolute path to directory'
| INSERT_METHOD [=] { NO | FIRST | LAST }
| KEY_BLOCK_SIZE [=] value
| MAX_ROWS [=] value
| MIN_ROWS [=] value
| PACK_KEYS [=] {0 | 1 | DEFAULT}
| PASSWORD [=] 'string'
| ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
| TABLESPACE tablespace_name [STORAGE {DISK|MEMORY|DEFAULT}]
| UNION [=] (tbl_name[,tbl_name]...)

partition_options:
PARTITION BY
{ [LINEAR] HASH(expr)
| [LINEAR] KEY(column_list)
| RANGE(expr)
| LIST(expr) }
[PARTITIONS num]
[SUBPARTITION BY
{ [LINEAR] HASH(expr)
| [LINEAR] KEY(column_list) }
[SUBPARTITIONS num]
]
[(partition_definition [, partition_definition] ...)]

partition_definition:
PARTITION partition_name
[VALUES
{LESS THAN {(expr) | MAXVALUE}
|
IN (value_list)}]
[[STORAGE] ENGINE [=] engine_name]
[COMMENT [=] 'comment_text' ]
[DATA DIRECTORY [=] 'data_dir']
[INDEX DIRECTORY [=] 'index_dir']
[MAX_ROWS [=] max_number_of_rows]
[MIN_ROWS [=] min_number_of_rows]
[TABLESPACE [=] tablespace_name]
[NODEGROUP [=] node_group_id]
[(subpartition_definition [, subpartition_definition] ...)]

subpartition_definition:
SUBPARTITION logical_name
[[STORAGE] ENGINE [=] engine_name]
[COMMENT [=] 'comment_text' ]
[DATA DIRECTORY [=] 'data_dir']
[INDEX DIRECTORY [=] 'index_dir']
[MAX_ROWS [=] max_number_of_rows]
[MIN_ROWS [=] min_number_of_rows]
[TABLESPACE [=] tablespace_name]
[NODEGROUP [=] node_group_id]

select_statement:
[IGNORE | REPLACE] [AS] SELECT ...   (Some legal select statement)*/
}
