package tosa.loader.parser.mysql;

import org.slf4j.LoggerFactory;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.data.types.DateColumnTypePersistenceHandler;
import tosa.loader.data.types.TimestampColumnTypePersistenceHandler;
import tosa.loader.parser.ISQLParser;
import tosa.loader.parser.SQLParserConstants;
import tosa.loader.parser.SQLTokenizer;
import tosa.loader.parser.Token;
import tosa.loader.parser.tree.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.ArrayList;
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
    LoggerFactory.getLogger("Tosa").trace("Done");
  }

  private SQLTokenizer _tokenizer;

  @Override
  public List<TableData> parseDDLFile(String fileContents) {
    _tokenizer = new SQLTokenizer(fileContents);
    List<TableData> tables = new ArrayList<TableData>();
    // TODO - AHK - Other Create calls?  Other stuff?  Closing semi-colon?
    while (!eof()) {
      TableData table = parseCreate();
      if (table != null) {
        tables.add(table);
      }
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

  private boolean eof() {
    return _tokenizer.eof();
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
      if(accept(TABLE)) {
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
        eatStatement();
        return null;
      }
    } else {
      eatStatement();
      return null;
    }
  }

  private void eatStatement() {
    while (!eof() && !accept(SEMI_COLON)) {
      consumeToken();
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
    DBColumnTypeImpl columnType = parseDataType();
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
  private DBColumnTypeImpl parseDataType() {
    DBColumnTypeImpl returnType = parseNumericColumnType();
    if (returnType == null) {
      returnType = parseDateColumnType();
    }
    if (returnType == null) {
      returnType = parseCharacterColumnType();
    }

    if (returnType == null) {
      LoggerFactory.getLogger("Tosa").debug("***Unexpected column type");
    }

    return returnType;
  }

  private DBColumnTypeImpl parseNumericColumnType() {
    // TODO - Handle Serial
    if (accept(BIT)) {
      Integer length = parseLength();
      if (length == null || length == 1) {
        return new DBColumnTypeImpl(BIT, BIT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT);
      } else {
        // TODO - AHK - Handle bit fields that have a length greater than 1
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + BIT);
        return null;
      }
    } else if (accept(TINYINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (length != null && length == 1) {
        // Treat TINYINT(1) as a boolean type
        return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
      } else if (signed) {
        return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT);
      } else {
        // TODO - AHK - Handle unsigned tiny ints
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + TINYINT);
        return null;
      }
    } else if (accept(BOOL) || accept(BOOLEAN)) {
      // BOOL and BOOLEAN are equivalent to TINYINT(1)
      return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
    } else if (accept(SMALLINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new DBColumnTypeImpl(SMALLINT, SMALLINT, DBColumnTypeImpl.SHORT_ITYPE ,Types.SMALLINT);
      } else {
        // TODO - AHK  - Handle unsigned small ints
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + SMALLINT);
        return null;
      }
    } else if (accept(MEDIUMINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new DBColumnTypeImpl(MEDIUMINT, MEDIUMINT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
      } else {
        // TODO - AHK - Handle unsigned medium ints
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + MEDIUMINT);
        return null;
      }
    } else if (accept(INT) || accept(INTEGER)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new DBColumnTypeImpl(INT, INT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
      } else {
        // TODO - AHK - Handle unsigned integers
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + INTEGER);
        return null;
      }
    } else if (accept(BIGINT)) {
      Integer length = parseLength();
      boolean signed = parseNumericModifiers();
      if (signed) {
        return new DBColumnTypeImpl(BIGINT, BIGINT, DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT);
      } else {
        // TODO - AHK - Handle unsigned big ints
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + BIGINT);
        return null;
      }
    } else if (accept(DOUBLE, PRECISION) || accept(DOUBLE) || accept(REAL)) {
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
    }
  }

  private DBColumnTypeImpl parseDateColumnType() {
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
  }

  private DBColumnTypeImpl parseCharacterColumnType() {
    if (accept(CHAR, BYTE) || accept(BINARY)) {
      Integer length = parseLength();
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
      Integer length = parseLength();
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

  private static class CharacterTypeAttributes {
    private Integer _length;
    private String _charSet;
    private String _collation;
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

  private Token _currentToken;

  @Override
  public SelectStatement parseSQLFile(DBData dbData, String fileContents) {
    _currentToken = Token.tokenize(fileContents);
    if (match(SELECT)) {
      Token start = lastMatch();
      SQLParsedElement quantifier = parseSetQuantifers();
      SQLParsedElement selectList = parseSelectList();
      TableExpression tableExpr = parseTableExpression();
      SelectStatement select = new SelectStatement(start, tableExpr.lastToken(), quantifier, selectList, tableExpr);
      select.verify(dbData);
      return select;
    }
    return null;
  }

  private Token lastMatch() {
    return _currentToken.previous();
  }

  private boolean match(String str) {
    if (_currentToken.match(str)) {
      _currentToken = _currentToken.nextToken();
      return true;
    } else {
      return false;
    }
  }

  private TableExpression parseTableExpression() {
    Token start = _currentToken;
    TableFromClause fromClause = parseFromClause();
    SQLParsedElement whereClause = parseWhereClause();
    TableExpression table = new TableExpression(start, lastMatch(), fromClause, whereClause);
    return table;
  }

  private SQLParsedElement parseWhereClause() {
    if (match(WHERE)) {
      return new WhereClause(lastMatch(), parseSearchOrExpression());
    } else if(_currentToken.isEOF()) {
      return null;
    } else {
      throw new IllegalStateException("This should be a parse error.");
    }
  }

  private SQLParsedElement parseSearchOrExpression() {
    SQLParsedElement lhs = parseSearchAndExpression();
    if (match(OR)) {
      SQLParsedElement rhs = parseSearchOrExpression();
      return new SQLOrExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchAndExpression() {
    SQLParsedElement lhs = parseSearchNotExpression();
    if (match(AND)) {
      SQLParsedElement rhs = parseSearchAndExpression();
      return new SQLAndExpression(lhs, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseSearchNotExpression() {
    if (match(NOT)) {
      Token last = lastMatch();
      SQLParsedElement val = parseBooleanTestExpression();
      return new SQLNotExpression(last, val);
    } else {
      return parseBooleanTestExpression();
    }
  }

  private SQLParsedElement parseBooleanTestExpression() {
    return parseBooleanPrimaryExpression();
    //TODO cgross IS NOT TRUE
  }

  private SQLParsedElement parseBooleanPrimaryExpression() {
    if (match(OPEN_PAREN)) {
      SQLParsedElement elt = parseSearchOrExpression();
      expectToken(elt, CLOSE_PAREN);
      return elt;
    } else {
      return parsePredicate();
    }
  }

  private SQLParsedElement parsePredicate() {
    SQLParsedElement initialValue = parseRowValue();
    if (initialValue != null) {
      if (matchAny(EQ_OP, LT_OP, LTEQ_OP, GT_OP, GTEQ_OP)) {
        SQLParsedElement comparisonValue = parseValueExpression();
        return new ComparisonPredicate(initialValue, initialValue.nextToken(), comparisonValue);
      }
      boolean notFound = match(NOT);
      if (match(LIKE)) {
        SQLParsedElement pattern = parsePattern();
        LikePredicate likePredicate = new LikePredicate(initialValue, pattern, notFound);
        return likePredicate;
      }

      if (_currentToken.match(IS) && _currentToken.nextToken().match(NOT) && _currentToken.nextToken().nextToken().match(NULL)) {
        _currentToken = _currentToken.nextToken().nextToken().nextToken();
        return new IsNotNullPredicate(initialValue, _currentToken.previous(), true);
      }
      if (_currentToken.match(IS) && _currentToken.nextToken().match(NULL)) {
        _currentToken = _currentToken.nextToken().nextToken();
        return new IsNotNullPredicate(initialValue, _currentToken.previous(), false);
      }
    }
    return unexpectedToken();
  }

  private SQLParsedElement parsePattern() {
    return parseRowValue();
  }

  private UnexpectedTokenExpression unexpectedToken() {
    Token unexpectedToken = takeToken();
    UnexpectedTokenExpression utExpr = new UnexpectedTokenExpression(unexpectedToken);
    utExpr.addParseError(new SQLParseError(unexpectedToken, "Unexpected Token"));
    return utExpr;
  }

  private void expectToken(SQLParsedElement elt, String str) {
    if (!match(str)) {
      elt.addParseError(new SQLParseError(_currentToken, "Expected " + str));
    }
  }

  private SQLParsedElement parseRowValue() {
    //TODO NULL, DEFAULT
    return parseValueExpression();
  }

  private SQLParsedElement parseValueExpression() {

    SQLParsedElement elt = parseNumericValueExpression();
    if (elt != null) {
      return elt;
    }

    elt = parseStringValueExpression();
    if (elt != null) {
      return elt;
    }
    
    elt = parseDateTimeValueExpression();
    if (elt != null) {
      return elt;
    }

    elt = parseIntervalValueExpression();
    if (elt != null) {
      return elt;
    }

    return null;
  }

  private SQLParsedElement parseIntervalValueExpression() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseDateTimeValueExpression() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private SQLParsedElement parseStringValueExpression() {
    if (_currentToken.isString()) {
      return new StringLiteralExpression(takeToken());
    } else {
      return null;
    }
  }

  private SQLParsedElement parseNumericValueExpression() {
    SQLParsedElement lhs = parseTerm();
    if (matchAny(PLUS_OP, MINUS_OP)) {
      Token op = lastMatch();
      SQLParsedElement rhs = parseNumericValueExpression();
      return new SQLAdditiveExpression(lhs, op, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseTerm() {
    SQLParsedElement lhs = parseFactor();
    if (matchAny(TIMES_OP, DIV_OP)) {
      Token op = lastMatch();
      SQLParsedElement rhs = parseTerm();
      return new SQLMultiplicitiveExpression(lhs, op, rhs);
    } else {
      return lhs;
    }
  }

  private SQLParsedElement parseFactor() {
    if (matchAny(PLUS_OP, MINUS_OP)) {
      return new SQLSignedExpression(lastMatch(), parseNumericPrimary());
    } else {
      return parseNumericPrimary();
    }
  }

  private SQLParsedElement parseNumericPrimary() {

    SQLParsedElement numericLiteral = parseNumericLiteral();
    if (numericLiteral != null) {
      return numericLiteral;
    }

    SQLParsedElement varRef = parseVariableReference();
    if (varRef != null) {
      return varRef;
    }

    SQLParsedElement columnRef = parseColumnReference();
    if (columnRef != null) {
      return columnRef;
    }
    
    return null;
  }

  private SQLParsedElement parseVariableReference() {
    if (_currentToken.isSymbol() && _currentToken.getValue().startsWith(":")) {
      return new VariableExpression(takeToken());
    } else {
      return null;
    }
  }

  private SQLParsedElement parseColumnReference() {
    if (_currentToken.isSymbol()) {
      Token base = takeToken();
      if (match(".") && _currentToken.isSymbol()) {
        return new ColumnReference(base, takeToken());
      } else {
        return new ColumnReference(base);
      }
    }
    return null;
  }

  private Token takeToken() {
    Token base = _currentToken;
    _currentToken = _currentToken.nextToken();
    return base;
  }

  private SQLParsedElement parseNumericLiteral() {
    if (_currentToken.isNumber()) {
      Token token = takeToken();
      if (token.getValue().contains(".")) {
        return new SQLNumericLiteral(token, new BigDecimal(token.getValue()));
      }
      else
      {
        return new SQLNumericLiteral(token, new BigInteger(token.getValue()));
      }
    }
    return null;
  }

  private TableFromClause parseFromClause() {
    if (match(FROM)) {
      Token start = lastMatch();
      ArrayList<SimpleTableReference> refs = new ArrayList<SimpleTableReference>();
      do {
        SimpleTableReference ref = parseTableReference();
        refs.add(ref);
      }
      while (match(COMMA));
      return new TableFromClause(start, refs);
    } else {
      TableFromClause from = new TableFromClause(_currentToken, Collections.<SimpleTableReference>emptyList());
      expectToken(from, FROM);
      takeToken();
      return from;
    }
  }

  private SimpleTableReference parseTableReference() {
    return new SimpleTableReference(takeToken());
    //TODO cgross more exotic table references
  }

  private SQLParsedElement parseSelectList() {
    if (match(ASTERISK)) {
      return new AsteriskSelectList(lastMatch());
    } else {
      return parseSelectSubList();
    }
  }

  private SQLParsedElement parseSelectSubList() {
    Token start = _currentToken;
    ArrayList<SQLParsedElement> cols = new ArrayList<SQLParsedElement>();
    do {
      SQLParsedElement value = parseValueExpression();
      if (!(value instanceof ColumnReference)) {
        value.addParseError(new SQLParseError(value.firstToken(), value.lastToken(), "Only column references are supported right now."));
      }
      if (value != null) {
        cols.add(value);
      } else {
        break;
      }
    } while (match(COMMA));
    return new ColumnSelectList(start, lastMatch(), cols);
  }

  private SQLParsedElement parseSetQuantifers() {
    if (matchAny(DISTINCT, ALL)) {
      return new QuantifierModifier(lastMatch());
    } else {
      return null;
    }
  }

  private boolean matchAny(String... tokens) {
    for (String s : tokens) {
      if (match(s)) {
        return true;
      }
    }
    return false;
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
