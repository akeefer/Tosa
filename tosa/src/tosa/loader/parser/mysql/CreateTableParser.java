package tosa.loader.parser.mysql;

import tosa.loader.parser.SQLParserConstants;
import tosa.loader.parser.Token;
import tosa.loader.parser.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTableParser implements SQLParserConstants {

  private Token _currentToken;

  public List<CreateTableStatement> parseSQLFile(String fileContents) {
    _currentToken = Token.tokenize(fileContents);
    List<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
    while (!_currentToken.isEOF()) {
      CreateTableStatement statement = parseCreate();
      if (statement == null) {
        // TODO - AHK - Is this correct?
        break;
      }
      statements.add(statement);
    }
    return statements;
  }

  private CreateTableStatement parseCreate() {
    if (match(CREATE)) {
      Token start = lastMatch();
      match(TEMPORARY); // Discard; we don't care
      expectToken(null, TABLE);
      match(IF, NOT, EXISTS);
      Token tableName = takeToken();
      List<SQLParsedElement> children = new ArrayList<SQLParsedElement>();
      if (accept(LIKE)) {
        Token likeTableName = takeToken();
        // TODO - AHK - And what do we do with it, exactly?
      } else if (accept(OPEN_PAREN, LIKE)) {
        Token likeTableName = takeToken();
        expect(CLOSE_PAREN);
        // TODO - AHK - And what do we do with it, exactly?
      } else if (accept(OPEN_PAREN)) {
        children.addAll(parseCreateDefinitions());
        expect(CLOSE_PAREN);
        // TODO - AHK
//        children.addAll(parseTableOptions());
//        children.addAll(parsePartitionOptions());
//        children.addAll(parseSelectStatement());
      } else {
        // No columns, but there must be a select statement
        // TODO - AHK
//        children.addAll(parseTableOptions());
//        children.addAll(parsePartitionOptions());
//        children.addAll(parseSelectStatement());
      }

      expect(SEMI_COLON);

      return new CreateTableStatement(start, lastMatch(), tableName, children);
    } else {
      return null;
    }
  }

  private List<SQLParsedElement> parseCreateDefinitions() {
    // TODO - AHK - Is it legal to have 0 columns?
    List<SQLParsedElement> createDefinitions = new ArrayList<SQLParsedElement>();
    SQLParsedElement createDefinition = parseCreateDefinition();
    if (createDefinition != null) {
      createDefinitions.add(createDefinition);
    }
    while (accept(COMMA)) {
      createDefinition = parseCreateDefinition();
      if (createDefinition != null) {
        createDefinitions.add(createDefinition);
      }
    }
    return createDefinitions;
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
  private SQLParsedElement parseCreateDefinition() {
    TableConstraintDefinition tableConstraintDefinition = parseIndexOrKeyOrConstraint();
    if (tableConstraintDefinition != null) {
      return tableConstraintDefinition;
    }

    CheckExpressionDefinition checkExpressionDefinition = parseCheckExpression();
    if (checkExpressionDefinition != null) {
      return checkExpressionDefinition;
    }

    ColumnDefinition columnDefinition = parseColumnDefinition();
    if (columnDefinition != null) {
      return columnDefinition;
    }

    // TODO - AHK - Add an error
    return null;
  }

//  | [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
//      [index_option] ...
//  | [CONSTRAINT [symbol]] FOREIGN KEY
//      [index_name] (index_col_name,...) reference_definition
//  | [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY]
//      [index_name] [index_type] (index_col_name,...)
//      [index_option] ...
//  | {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
//      [index_option] ...
//  | {FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...)
//      [index_option] ...

  private TableConstraintDefinition parseIndexOrKeyOrConstraint() {
    if (peek(CONSTRAINT) || peek(INDEX) || peek(KEY) || peek(FULLTEXT) || peek(SPATIAL)) {
      Token start = null;
      Token symbol = null;
      TableConstraintDefinition.ConstraintType constraintType = null;
      IndexName indexName = null;
      IndexType indexType = null;
      List<IndexColumnName> columnNames = null;
      List<SQLParsedElement> indexOptions = null;
      if (match(CONSTRAINT)) {
        start = lastMatch();
        if (!peek(PRIMARY) && !peek(FOREIGN) && !peek(UNIQUE)) {
          symbol = takeToken();
        }
      } else {
        start = _currentToken;
      }

      if (match(PRIMARY, KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.PRIMARY_KEY;
      } else if (match(FOREIGN, KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.FOREIGN_KEY;
      } else if (match(UNIQUE, INDEX)) {
        constraintType = TableConstraintDefinition.ConstraintType.UNIQUE_KEY;
      } else if (match(UNIQUE, KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.UNIQUE_INDEX;
      } else if (match(INDEX)) {
        constraintType = TableConstraintDefinition.ConstraintType.INDEX;
      } else if (match(KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.KEY;
      } else if (match(FULLTEXT, INDEX)) {
        constraintType = TableConstraintDefinition.ConstraintType.FULLTEXT_INDEX;
      } else if (match(FULLTEXT, KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.FULLTEXT_KEY;
      } else if (match(SPATIAL, INDEX)) {
        constraintType = TableConstraintDefinition.ConstraintType.SPATIAL_INDEX;
      } else if (match(SPATIAL, KEY)) {
        constraintType = TableConstraintDefinition.ConstraintType.SPATIAL_KEY;
      } else {
        // TODO - AHK - Add an error
      }

      if (constraintType != TableConstraintDefinition.ConstraintType.PRIMARY_KEY) {
        indexName = parseIndexName();
      }
      if (constraintType == TableConstraintDefinition.ConstraintType.PRIMARY_KEY ||
          constraintType == TableConstraintDefinition.ConstraintType.UNIQUE_INDEX ||
          constraintType == TableConstraintDefinition.ConstraintType.UNIQUE_KEY ||
          constraintType == TableConstraintDefinition.ConstraintType.INDEX ||
          constraintType == TableConstraintDefinition.ConstraintType.KEY) {
        indexType = parseIndexType();
      }
      columnNames = parseIndexColumnNames();
      if (constraintType == TableConstraintDefinition.ConstraintType.FOREIGN_KEY) {
        // TODO - AHK - FK reference columns
      } else {
        indexOptions = parseIndexOptions();
      }
      return new TableConstraintDefinition(start, lastMatch(), constraintType, symbol, indexName, indexType, columnNames, indexOptions);
    } else {
      return null;
    }
  }

  private IndexName parseIndexName() {
    // Index names can be followed by an index type or a list of column names, and is optional,
    // so we have to check to see if there's actually an index name specified or if we're
    // moving on to an index type or column list
    if (peek(USING) || peek(OPEN_PAREN)) {
      return null;
    } else {
      return new IndexName(takeToken());
    }
  }

  //  index_type:
  //    USING {BTREE | HASH}
  private IndexType parseIndexType() {
    if (match(USING)) {
      Token start = lastMatch();
      if (match(BTREE)) {
        return new IndexType(start, lastMatch(), IndexType.IndexTypeOption.BTREE);
      } else if (match(HASH)) {
        return new IndexType(start, lastMatch(), IndexType.IndexTypeOption.HASH);
      } else {
        // TODO - AHK - Error!
        return null;
      }
    } else {
      return null;
    }
  }

  private List<IndexColumnName> parseIndexColumnNames() {
    List<IndexColumnName> indexColumnNames = new ArrayList<IndexColumnName>();
    expect(OPEN_PAREN);
    indexColumnNames.add(parseIndexColumnName());
    while (match(COMMA)) {
      indexColumnNames.add(parseIndexColumnName());
    }
    expect(CLOSE_PAREN);
    return indexColumnNames;
  }

  //  index_col_name:
  //    col_name [(length)] [ASC | DESC]
  private IndexColumnName parseIndexColumnName() {
    Token columnName = takeToken();
    Token length = null;
    if (match(OPEN_PAREN)) {
      length = takeToken();
      expect(CLOSE_PAREN);
    }

    IndexColumnName.IndexColumnSortDirection sortDirection = null;
    if (accept(ASC)) {
      sortDirection = IndexColumnName.IndexColumnSortDirection.ASC;
    } else if (accept(DESC)) {
      sortDirection = IndexColumnName.IndexColumnSortDirection.DESC;
    }

    return new IndexColumnName(columnName, lastMatch(), columnName, length, sortDirection);
  }

  private List<SQLParsedElement> parseIndexOptions() {
    List<SQLParsedElement> indexOptions = new ArrayList<SQLParsedElement>();
    while(true) {
      SQLParsedElement option = parseIndexOption();
      if (option != null) {
        indexOptions.add(option);
      } else {
        break;
      }
    }
    return indexOptions;
  }

  //  index_option:
  //    KEY_BLOCK_SIZE [=] value
  //  | index_type
  //  | WITH PARSER parser_name
  private SQLParsedElement parseIndexOption() {
    if (match(KEY_BLOCK_SIZE)) {
      Token start = lastMatch();
      match(EQUALS);
      Token value = takeToken();
      return new KeyBlockSizeIndexOption(start, value);
    } else if (accept(WITH, PARSER)) {
      Token start = lastMatch().previous();
      Token value = takeToken();
      return new WithParserIndexOption(start, value);
    } else {
      return parseIndexType();
    }
  }

  // column_definition:
  //    column_name data_type [NOT NULL | NULL] [DEFAULT default_value]
  //      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
  //      [COMMENT 'string']
  //      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
  //      [STORAGE {DISK|MEMORY|DEFAULT}]
  //      [reference_definition]*/
  private ColumnDefinition parseColumnDefinition() {
    Token columnName = takeToken();
    ColumnDataType dataType = parseDataType();
    List<SQLParsedElement> columnOptions = new ArrayList<SQLParsedElement>();
    while (true) {
      SQLParsedElement columnOption = parseColumnOption();
      if (columnOption != null) {
        columnOptions.add(columnOption);
      } else {
        break;
      }
    }

    return new ColumnDefinition(columnName, lastMatch(), columnName, dataType, columnOptions);
  }

  private ColumnDataType parseDataType() {
    ColumnDataType returnType = parseNumericColumnType();
    if (returnType == null) {
      returnType = parseDateColumnType();
    }
    if (returnType == null) {
      returnType = parseCharacterColumnType();
    }
    // TODO - AHK - Parse spatial data types
    if (returnType == null) {
      // TODO - AHK
    }
    return returnType;
  }

  //    BIT[(length)]
  //  | TINYINT[(length)] [UNSIGNED] [ZEROFILL]
  //  | SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
  //  | MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
  //  | INT[(length)] [UNSIGNED] [ZEROFILL]
  //  | INTEGER[(length)] [UNSIGNED] [ZEROFILL]
  //  | BIGINT[(length)] [UNSIGNED] [ZEROFILL]
  //  | REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
  //  | DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
  //  | FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
  //  | DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
  //  | NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
  private ColumnDataType parseNumericColumnType() {
    // TODO - Handle Serial
    if (accept(BIT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BIT, length, null);
    } else if (accept(TINYINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TINYINT, length, modifiers);
    } else if (accept(BOOL) || accept(BOOLEAN)) {
      // BOOL and BOOLEAN are equivalent to TINYINT(1)
      Token start = lastMatch();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BOOL, null, null);
    } else if (accept(SMALLINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.SMALLINT, length, modifiers);
    } else if (accept(MEDIUMINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.MEDIUMINT, length, modifiers);
    } else if (accept(INT) || accept(INTEGER)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.INT, length, modifiers);
    } else if (accept(BIGINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BIGINT, length, modifiers);
    } else if (accept(REAL)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.REAL, length, modifiers);
    } else if (accept(DOUBLE, PRECISION) || accept(DOUBLE)) {
      Token start = lastMatch();
      if (start.match(PRECISION)) {
        start = start.previous();
      }
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.DOUBLE, length, modifiers);
    } else if (accept(FLOAT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.FLOAT, length, modifiers);
    } else if (accept(DECIMAL) || accept(DEC)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.DECIMAL, length, modifiers);
    } else if (accept(NUMERIC) || accept(FIXED)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.NUMERIC, length, modifiers);
    } else {
      return null;
    }
  }

  private ColumnLengthExpression parseLength() {
    if (accept(OPEN_PAREN)) {
      Token start = lastMatch();
      Token length = takeToken();
      expect(CLOSE_PAREN);
      return new ColumnLengthExpression(start, lastMatch(), length, null);
    } else {
      return null;
    }
  }

  private List<NumericDataTypeModifier> parseNumericModifiers() {
    List<NumericDataTypeModifier> modifiers = new ArrayList<NumericDataTypeModifier>();
    while (true) {
      NumericDataTypeModifier modifier = parseNumericModifier();
      if (modifier != null) {
        modifiers.add(modifier);
      } else {
        break;
      }
    }
    return modifiers;
  }

  private NumericDataTypeModifier parseNumericModifier() {
    if (accept(SIGNED)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.SIGNED);
    } else if (accept(UNSIGNED)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.UNSIGNED);
    } else if (accept(ZEROFILL)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.ZEROFILL);
    } else {
      return null;
    }
  }

  private ColumnLengthExpression parseLengthAndDecimals() {
    // TODO - AHK - Sometimes the comma isn't optional, but I don't think that matters here
    if (accept(OPEN_PAREN)) {
      Token start = lastMatch();
      Token length = takeToken();
      Token decimals = null;
      if (accept(COMMA)) {
        decimals = takeToken();
      }
      expect(CLOSE_PAREN);
      return new ColumnLengthExpression(start, lastMatch(), length, decimals);
    } else {
      return null;
    }
  }

  // TODO - Copy in syntax
  private ColumnDataType parseDateColumnType() {
    if (accept(DATE)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.DATE);
    } else if (accept(TIME)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TIME);
    } else if (accept(TIMESTAMP)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TIMESTAMP);
    } else if (accept(DATETIME)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.DATETIME);
    } else if (accept(YEAR)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.YEAR);
    } else {
      return null;
    }
  }

  //  | CHAR[(length)]
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | VARCHAR(length)
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | BINARY[(length)]
  //  | VARBINARY(length)
  //  | TINYBLOB
  //  | BLOB
  //  | MEDIUMBLOB
  //  | LONGBLOB
  //  | TINYTEXT [BINARY]
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | TEXT [BINARY]
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | MEDIUMTEXT [BINARY]
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | LONGTEXT [BINARY]
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | ENUM(value1,value2,value3,...)
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
  //  | SET(value1,value2,value3,...)
  //      [CHARACTER SET charset_name] [COLLATE collation_name]
    private ColumnDataType parseCharacterColumnType() {
    if (accept(CHAR, BYTE) || accept(BINARY)) {
      Token start = lastMatch();
      if (start.match(BYTE)) {
        start = start.previous();
      }
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BINARY, lengthExpression, null);
    } else if (accept(CHAR) || accept(CHARACTER)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.CHAR, lengthExpression, characterTypeAttributes);
    } else if (accept(NATIONAL, CHAR) || accept(NCHAR)) {
      Token start = lastMatch();
      if (start.match(CHAR)) {
        start = start.previous();
      }
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.NCHAR, lengthExpression, characterTypeAttributes);
    } else if (accept(VARCHAR)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.VARCHAR, lengthExpression, characterTypeAttributes);
    } else if (accept(VARBINARY)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.VARBINARY, lengthExpression, null);
    } else if (accept(TINYBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TINYBLOB);
    } else if (accept(BLOB)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BLOB, lengthExpression, null);
    } else if (accept(MEDIUMBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.MEDIUMBLOB);
    } else if (accept(LONGBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.LONGBLOB);
    } else if (accept(TINYTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TINYTEXT, null, characterTypeAttributes);
    } else if (accept(TEXT)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TEXT, lengthExpression, characterTypeAttributes);
    } else if (accept(MEDIUMTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.MEDIUMTEXT, null, characterTypeAttributes);
    } else if (accept(LONGTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.LONGTEXT, null, characterTypeAttributes);
    } else if (accept(ENUM)) {
      // TODO - AHK
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + ENUM);
      return null;
    } else if (accept(SET)) {
      // TODO - AHK
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + SET);
      return null;
    } else {
      return null;
    }
  }

  private List<SQLParsedElement> parseCharTypeAttributes() {
    List<SQLParsedElement> charTypeAttributes = new ArrayList<SQLParsedElement>();
    while (true) {
      SQLParsedElement charTypeAttribute = parseCharTypeAttribute();
      if (charTypeAttribute != null) {
        charTypeAttributes.add(charTypeAttribute);
      } else {
        break;
      }
    }
    return charTypeAttributes;
  }

  private SQLParsedElement parseCharTypeAttribute() {
    // TODO - AHK - Should be an error if the char set or collation is already set
    if (accept(CHARACTER, SET)) {
      Token start = lastMatch().previous();
      Token charSetName = takeToken();
      return new CharacterSetExpression(start, charSetName, charSetName);
    } else if (accept(COLLATE)) {
      Token start = lastMatch();
      Token collation = takeToken();
      return new CollateExpression(start, collation, collation);
    } else if (accept(ASCII)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.ASCII);
    } else if (accept(UNICODE)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.UNICODE);
    } else if (accept(BINARY)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.BINARY);
    } else {
      return null;
    }
  }

  private SQLParsedElement parseColumnOption() {
    // TODO - AHK
    return null;
  }

  private CheckExpressionDefinition parseCheckExpression() {
    // TODO - AHK
    return null;
  }


  // ==========================================================================
  //                            Helper Methods
  // ==========================================================================

  private Token lastMatch() {
    return _currentToken.previous();
  }

  private boolean accept(String... tokens) {
    return match(tokens);
  }

  private void expect(String token) {
    expectToken(null, token);
  }

  private boolean match(String... tokens) {
    Token matchTarget = _currentToken;
    for (String str : tokens) {
      if (!matchTarget.match(str)) {
        return false;
      }
      // TODO - AHK - Throw if there is no next token
      matchTarget = matchTarget.nextToken();
    }

    _currentToken = matchTarget;
    return true;
  }

  private boolean peek(String... tokens) {
    Token matchTarget = _currentToken;
    for (String str : tokens) {
      if (!matchTarget.match(str)) {
        return false;
      }
      // TODO - AHK - Throw if there is no next token
      matchTarget = matchTarget.nextToken();
    }

    return true;
  }


  private void expectToken(SQLParsedElement elt, String str) {
    if (!match(str)) {
      // TODO - AHK - This is a total hack job
      if (elt == null) {
        throw new IllegalArgumentException("Expected " + str);
      }
      elt.addParseError(new SQLParseError(_currentToken, "Expected " + str));
    }
  }

  private Token takeToken() {
    Token base = _currentToken;
    _currentToken = _currentToken.nextToken();
    return base;
  }
//
//  private boolean accept(String... tokens) {
//    return true;
//  }
//
//  private void expect(String... tokens) {
//
//  }
//
//  private String consumeToken() {
//    return null;
//  }
//
//  private String stripQuotes(String str) {
//    return str;
//  }
//
//  private boolean peek(String... tokens) {
//    return true;
//  }
//
//  // ----------------------------
//
//  private TableData parseCreate() {
//    if (accept(CREATE)) {
//      accept(TEMPORARY); // Discard; we don't care
//      expect(TABLE);
//      accept(IF, NOT, EXISTS);
//      String tableName = stripQuotes(consumeToken());
//      List<ColumnData> columns = null;
//      if (accept(LIKE)) {
//        String likeTableName = consumeToken();
//        // TODO - AHK - And what do we do with it, exactly?
//      } else if (accept(OPEN_PAREN, LIKE)) {
//        String likeTableName = consumeToken();
//        expect(CLOSE_PAREN);
//        // TODO - AHK - And what do we do with it, exactly?
//      } else if (accept(OPEN_PAREN)) {
//        columns = parseCreateDefinitions();
//        expect(CLOSE_PAREN);
//        parseTableOptions();
//        parsePartitionOptions();
//        parseSelectStatement();
//      } else {
//        // No columns, but there must be a select statement
//        parseTableOptions();
//        parsePartitionOptions();
//        parseSelectStatement();
//      }
//
//      expect(SEMI_COLON);
//
//      return new TableData(tableName, columns);
//    } else {
//      return null;
//    }
//  }
//
//  private List<ColumnData> parseCreateDefinitions() {
//    // TODO - AHK - Is it legal to have 0 columns?
//    List<ColumnData> columns = new ArrayList<ColumnData>();
//    ColumnData columnData = parseCreateDefinition();
//    if (columnData != null) {
//      columns.add(columnData);
//    }
//    while (accept(COMMA)) {
//      columnData = parseCreateDefinition();
//      if (columnData != null) {
//        columns.add(columnData);
//      }
//    }
//    return columns;
//  }
//
//  /*create_definition:
//    col_name column_definition
//  | [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
//      [index_option] ...
//  | {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
//      [index_option] ...
//  | [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY]
//      [index_name] [index_type] (index_col_name,...)
//      [index_option] ...
//  | {FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...)
//      [index_option] ...
//  | [CONSTRAINT [symbol]] FOREIGN KEY
//      [index_name] (index_col_name,...) reference_definition
//  | CHECK (expr)*/
//  private ColumnData parseCreateDefinition() {
//    if (accept(CONSTRAINT)) {
//      String symbolName;
//      if (peek(PRIMARY, KEY) || peek(UNIQUE) || peek(FOREIGN, KEY)) {
//        symbolName = null;
//      } else {
//        symbolName = consumeToken();
//      }
//    }
//
//    if (accept(PRIMARY, KEY)) {
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (accept(INDEX) || accept(KEY)) {
//      parseIndexName();
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (accept(UNIQUE)) {
//      // TODO - AHK
//      if (!accept(INDEX)) {
//        expect(KEY);
//      }
//      parseIndexName();
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (accept(FULLTEXT) || accept(SPATIAL)) {
//      if (!accept(INDEX)) {
//        expect(KEY);
//      }
//      parseIndexName();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (accept(FOREIGN, KEY)) {
//      parseIndexName();
//      parseIndexColumnNames();
//      parseReferenceDefinition();
//    } else if (accept(CHECK)) {
//      parseParenthesizedExpression();
//    } else {
//      return parseColumnDefinition();
//    }
//
//    return null;
//  }
//
//  private void parseIndexName() {
//    if (!(peek(USING) || peek(OPEN_PAREN))) {
//      String name = consumeToken();
//    }
//  }
//
//  private void parseIndexType() {
//    if (accept(USING)) {
//      if (!accept(BTREE)) {
//        expect(HASH);
//      }
//    }
//  }
//
//  private void parseIndexOptions() {
//    if (accept(KEY_BLOCK_SIZE)) {
//      accept(EQUALS);
//      String value = consumeToken();
//      parseIndexOptions();
//    } else if (accept(USING)) {
//      // TODO - AHK - Sould there be an expect() variant for an OR situation like this?
//      if (!accept(BTREE)) {
//        expect(HASH);
//      }
//      parseIndexOptions();
//    } else if (accept(WITH, PARSER)) {
//      String parserName = consumeToken();
//    }
//  }
//
//  // TODO - AHK - This needs to be order-independent
//  private boolean parseReferenceDefinition() {
//    if (accept(REFERENCES)) {
//      String tableName = consumeToken();
//      expect(OPEN_PAREN);
//      String columnName = consumeToken();
//      while (accept(COMMA)) {
//        columnName = consumeToken();
//      }
//      expect(CLOSE_PAREN);
//      if (accept(MATCH, FULL) || accept(MATCH, PARTIAL) || accept(MATCH, SIMPLE)) {
//        // Just eat it
//      }
//      if (accept(ON, DELETE)) {
//        parseReferenceOption();
//      }
//      if (accept(ON, UPDATE)) {
//        parseReferenceOption();
//      }
//
//      return true;
//    }
//
//    return false;
//  }
//
//  private void parseReferenceOption() {
//    if (accept(RESTRICT) || accept(CASCADE) || accept(SET, NULL) || accept(NO, ACTION)) {
//      // Just eat it
//    } else {
//      // Error case
//    }
//  }
//
//  private void parseIndexColumnNames() {
//    expect(OPEN_PAREN);
//    parseIndexColName();
//    while (accept(COMMA)) {
//      parseIndexColName();
//    }
//    expect(CLOSE_PAREN);
//  }
//
//  private void parseIndexColName() {
//    String columnName = consumeToken();
//    if (accept(OPEN_PAREN)) {
//      String length = consumeToken();
//      expect(CLOSE_PAREN);
//    }
//    if (accept(ASC)) {
//      // Just eat it
//    } else if (accept(DESC)) {
//      // Just eat it
//    }
//  }
//
//  /*column_definition:
//    data_type [NOT NULL | NULL] [DEFAULT default_value]
//      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
//      [COMMENT 'string']
//      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
//      [STORAGE {DISK|MEMORY|DEFAULT}]
//      [reference_definition]*/
//  private ColumnData parseColumnDefinition() {
//    // Note:  In the syntax defined in the MySQL docs they don't include the name as part of the column_definition
//    // production, but I'm moving it in there for the sake of sanity
//    String name = consumeToken();
//    name = stripQuotes(name);
//    DBColumnTypeImpl columnType = parseDataType();
//    while (parseColumnOption()) {
//      // Keep looping to consume all the options
//    }
//    // TODO - AHK
//    return new ColumnData(name, columnType);
//  }
//
//  /*
//    [NOT NULL | NULL] [DEFAULT default_value]
//      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
//      [COMMENT 'string']
//      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
//      [STORAGE {DISK|MEMORY|DEFAULT}]
//      [reference_definition]*/
//  private boolean parseColumnOption() {
//    if (accept(NOT, NULL)) {
//      return true;
//    } else if (accept(NULL)) {
//      return true;
//    }
//
//    if (accept(DEFAULT)) {
//      String defaultValue = consumeToken();
//      return true;
//    }
//
//    if (accept(AUTO_INCREMENT)) {
//      return true;
//    }
//
//    if (accept(UNIQUE)) {
//      accept(KEY);
//      return true;
//    } else if (accept(PRIMARY)) {
//      expect(KEY);
//      return true;
//    } else if (accept(KEY)) {
//      return true;
//    }
//
//    if (accept(COMMENT)) {
//      String comment = parseQuotedString();
//      return true;
//    }
//
//    if (accept(COLUMN_FORMAT)) {
//      if (accept(FIXED) || accept(DYNAMIC) || accept(DEFAULT)) {
//        return true;
//      } else {
//        // TODO - AHK - Error case
//        return true;
//      }
//    }
//
//    if (accept(STORAGE)) {
//      if (accept(DISK) || accept(MEMORY) || accept(DEFAULT)) {
//        return true;
//      } else {
//        // TODO - AHK - Error case
//        return true;
//      }
//    }
//
//    if (parseReferenceDefinition()) {
//      return true;
//    }
//
//    return false;
//  }
//
//  /*
//  data_type:
//    BIT[(length)]
//  | TINYINT[(length)] [UNSIGNED] [ZEROFILL]
//  | SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
//  | MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
//  | INT[(length)] [UNSIGNED] [ZEROFILL]
//  | INTEGER[(length)] [UNSIGNED] [ZEROFILL]
//  | BIGINT[(length)] [UNSIGNED] [ZEROFILL]
//  | REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
//  | DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
//  | FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
//  | DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
//  | NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
//  | DATE
//  | TIME
//  | TIMESTAMP
//  | DATETIME
//  | YEAR
//  | CHAR[(length)]
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | VARCHAR(length)
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | BINARY[(length)]
//  | VARBINARY(length)
//  | TINYBLOB
//  | BLOB
//  | MEDIUMBLOB
//  | LONGBLOB
//  | TINYTEXT [BINARY]
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | TEXT [BINARY]
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | MEDIUMTEXT [BINARY]
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | LONGTEXT [BINARY]
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | ENUM(value1,value2,value3,...)
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | SET(value1,value2,value3,...)
//      [CHARACTER SET charset_name] [COLLATE collation_name]
//  | spatial_type
//   */
//  private DBColumnTypeImpl parseDataType() {
//    DBColumnTypeImpl returnType = parseNumericColumnType();
//    if (returnType == null) {
//      returnType = parseDateColumnType();
//    }
//    if (returnType == null) {
//      returnType = parseCharacterColumnType();
//    }
//
//    if (returnType == null) {
//      LoggerFactory.getLogger("Tosa").debug("***Unexpected column type");
//    }
//
//    return returnType;
//  }
//
//  private DBColumnTypeImpl parseNumericColumnType() {
//    // TODO - Handle Serial
//    if (accept(BIT)) {
//      Integer length = parseLength();
//      if (length == null || length == 1) {
//        return new DBColumnTypeImpl(BIT, BIT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT);
//      } else {
//        // TODO - AHK - Handle bit fields that have a length greater than 1
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + BIT);
//        return null;
//      }
//    } else if (accept(TINYINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (length != null && length == 1) {
//        // Treat TINYINT(1) as a boolean type
//        return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
//      } else if (signed) {
//        return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT);
//      } else {
//        // TODO - AHK - Handle unsigned tiny ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + TINYINT);
//        return null;
//      }
//    } else if (accept(BOOL) || accept(BOOLEAN)) {
//      // BOOL and BOOLEAN are equivalent to TINYINT(1)
//      return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
//    } else if (accept(SMALLINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(SMALLINT, SMALLINT, DBColumnTypeImpl.SHORT_ITYPE ,Types.SMALLINT);
//      } else {
//        // TODO - AHK  - Handle unsigned small ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + SMALLINT);
//        return null;
//      }
//    } else if (accept(MEDIUMINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(MEDIUMINT, MEDIUMINT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//      } else {
//        // TODO - AHK - Handle unsigned medium ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + MEDIUMINT);
//        return null;
//      }
//    } else if (accept(INT) || accept(INTEGER)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(INT, INT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//      } else {
//        // TODO - AHK - Handle unsigned integers
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + INTEGER);
//        return null;
//      }
//    } else if (accept(BIGINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(BIGINT, BIGINT, DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT);
//      } else {
//        // TODO - AHK - Handle unsigned big ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + BIGINT);
//        return null;
//      }
//    } else if (accept(DOUBLE, PRECISION) || accept(DOUBLE) || accept(REAL)) {
//      // TODO - AHK - If the REAL_AS_FLOAT mode is set on the DB, this will be incorrect
//      parseLengthAndDecimals();
//      boolean signed = parseNumericModifiers();
//      return new DBColumnTypeImpl(DOUBLE, DOUBLE, DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE);
//    } else if (accept(FLOAT)) {
//      // TODO - AHK - It's a different deal if there's a length and a precision versus just a single number
//      parseLengthAndDecimals();
//      boolean signed = parseNumericModifiers();
//      return new DBColumnTypeImpl(FLOAT, FLOAT, DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT);
//    } else if (accept(DECIMAL) || accept(DEC) || accept(NUMERIC) || accept(FIXED)) {
//      parseLengthAndDecimals();
//      boolean signed = parseNumericModifiers();
//      // TODO - AHK - The precision and size are probably important here
//      // TODO - AHK - If there precision is 0, should this be a BigInteger?
//      return new DBColumnTypeImpl(DECIMAL, DECIMAL, DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL);
//    } else {
//      return null;
//    }
//  }
//
//  private DBColumnTypeImpl parseDateColumnType() {
//    if (accept(DATE)) {
//      return new DBColumnTypeImpl(DATE, DATE, DBColumnTypeImpl.DATE_ITYPE, Types.DATE, new DateColumnTypePersistenceHandler());
//    } else if (accept(TIME)) {
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + TIME);
//      return null;
//    } else if (accept(TIMESTAMP)) {
//      return new DBColumnTypeImpl(TIMESTAMP, TIMESTAMP, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
//    } else if (accept(DATETIME)) {
//      return new DBColumnTypeImpl(DATETIME, DATETIME, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
//    } else if (accept(YEAR)) {
//      return new DBColumnTypeImpl(YEAR, YEAR, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//    } else {
//      return null;
//    }
//  }
//
//  private DBColumnTypeImpl parseCharacterColumnType() {
//    if (accept(CHAR, BYTE) || accept(BINARY)) {
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
//    } else if (accept(CHAR) || accept(CHARACTER)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
//      } else {
//        return new DBColumnTypeImpl(CHAR, CHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
//      }
//    } else if (accept(NATIONAL, CHAR) || accept(NCHAR)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      return new DBColumnTypeImpl(NCHAR, NCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
//    } else if (accept(VARCHAR)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
//      } else {
//        return new DBColumnTypeImpl(VARCHAR, VARCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR);
//      }
//    } else if (accept(VARBINARY)) {
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
//    } else if (accept(TINYBLOB)) {
//      // Max length is 255
//      return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (accept(BLOB)) {
//      // Max length is 2^16 - 1 if not otherwise specified
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (accept(MEDIUMBLOB)) {
//      // Max length is 2^24 - 1
//      return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (accept(LONGBLOB)) {
//      // Max length is 2^32 - 1
//      return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (accept(TINYTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 255
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(TINYTEXT, TINYTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (accept(TEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^16 - 1 if not otherwise specified
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(TEXT, TEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (accept(MEDIUMTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^24 - 1
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(MEDIUMTEXT, MEDIUMTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (accept(LONGTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^32 - 1
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(LONGTEXT, LONGTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (accept(ENUM)) {
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + ENUM);
//      return null;
//    } else if (accept(SET)) {
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + SET);
//      return null;
//    } else {
//      return null;
//    }
//  }
//
//  private static class CharacterTypeAttributes {
//    private Integer _length;
//    private String _charSet;
//    private String _collation;
//  }
//
//  private CharacterTypeAttributes parseCharTypeAttributes() {
//    CharacterTypeAttributes attributes = new CharacterTypeAttributes();
//    attributes._length = parseLength();
//    while(parseCharTypeAttribute(attributes)) {
//      // Loop
//    }
//    return attributes;
//  }
//
//  private boolean parseCharTypeAttribute(CharacterTypeAttributes charTypeAttributes) {
//    // TODO - AHK - Should be an error if the char set or collation is already set
//    if (accept(CHARACTER, SET)) {
//      charTypeAttributes._charSet = consumeToken();
//      return true;
//    } else if (accept(COLLATE)) {
//      charTypeAttributes._collation = consumeToken();
//      return true;
//    } else if (accept(ASCII)) {
//      charTypeAttributes._charSet = "latin1";
//      return true;
//    } else if (accept(UNICODE)) {
//      charTypeAttributes._charSet = "ucs2";
//      return true;
//    } else if (accept(BINARY)) {
//      charTypeAttributes._collation = "binary";
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  // TODO - AHK - Maybe return an int instead?
//  private Integer parseLength() {
//    if (accept(OPEN_PAREN)) {
//      String length = consumeToken();
//      expect(CLOSE_PAREN);
//      return Integer.valueOf(length);
//    } else {
//      return null;
//    }
//  }
//
//  private boolean parseNumericModifiers() {
//    boolean signed = true;
//    if (accept(SIGNED)) {
//      signed = true;
//    } else if (accept(UNSIGNED)) {
//      signed = false;
//    }
//
//    // Zerofill columns are automatically treated as unsigned
//    if (accept(ZEROFILL)) {
//      signed = false;
//    }
//
//    return signed;
//  }
//
//  private void parseLengthAndDecimals() {
//    // TODO - AHK - Sometimes the comma isn't optional, but I don't think that matters here
//    if (accept(OPEN_PAREN)) {
//      String length = consumeToken();
//      if (accept(COMMA)) {
//        String decimals = consumeToken();
//      }
//      expect(CLOSE_PAREN);
//    }
//  }
//
//  private String parseCharSet() {
//    if (accept(CHARACTER, SET)) {
//      return consumeToken();
//    } else {
//      return null;
//    }
//  }
//
//  private String parseCollation() {
//    if (accept(COLLATE)) {
//      return consumeToken();
//    } else {
//      return null;
//    }
//  }
//
//  private List<String> parseEnumOrSetValueList() {
//    List<String> values = new ArrayList<String>();
//    expect(OPEN_PAREN);
//    values.add(consumeToken());
//    while (accept(COMMA)) {
//      values.add(consumeToken());
//    }
//    expect(CLOSE_PAREN);
//    return values;
//  }
//
//  /*table_options:
//    table_option [[,] table_option] ...*/
//  private void parseTableOptions() {
//    parseTableOption();
//    // TODO - AHK - Are the commas required?  If not, we'll need to see if a table option was
//    // actually parsed or not
//    while (accept(COMMA)) {
//      parseTableOption();
//    }
//  }
//
//  /*
//  table_option:
//    ENGINE [=] engine_name
//  | AUTO_INCREMENT [=] value
//  | AVG_ROW_LENGTH [=] value
//  | [DEFAULT] CHARACTER SET [=] charset_name
//  | CHECKSUM [=] {0 | 1}
//  | [DEFAULT] COLLATE [=] collation_name
//  | COMMENT [=] 'string'
//  | CONNECTION [=] 'connect_string'
//  | DATA DIRECTORY [=] 'absolute path to directory'
//  | DELAY_KEY_WRITE [=] {0 | 1}
//  | INDEX DIRECTORY [=] 'absolute path to directory'
//  | INSERT_METHOD [=] { NO | FIRST | LAST }
//  | KEY_BLOCK_SIZE [=] value
//  | MAX_ROWS [=] value
//  | MIN_ROWS [=] value
//  | PACK_KEYS [=] {0 | 1 | DEFAULT}
//  | PASSWORD [=] 'string'
//  | ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
//  | TABLESPACE tablespace_name [STORAGE {DISK|MEMORY|DEFAULT}]
//  | UNION [=] (tbl_name[,tbl_name]...)
//  */
//  private void parseTableOption() {
//    if (accept(ENGINE)) {
//      accept(EQUALS);
//      String engineName = consumeToken();
//    } else if (accept(AUTO_INCREMENT)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(AVG_ROW_LENGTH)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(DEFAULT, CHARACTER, SET) || accept(CHARACTER, SET)) {
//      accept(EQUALS);
//      String charsetName = consumeToken();
//    } else if (accept(CHECKSUM)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(DEFAULT, COLLATE) || accept(COLLATE)) {
//      accept(EQUALS);
//      String collationName = consumeToken();
//    } else if (accept(COMMENT)) {
//      accept(EQUALS);
//      String comment = consumeToken();
//    } else if (accept(CONNECTION)) {
//      accept(EQUALS);
//      String connection = consumeToken();
//    } else if (accept(DATA, DICTIONARY)) {
//      accept(EQUALS);
//      String path = consumeToken();
//    } else if (accept(DELAY_KEY_WRITE)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(INDEX, DIRECTORY)) {
//      accept(EQUALS);
//      String path = consumeToken();
//    } else if (accept(INSERT_METHOD)) {
//      accept(EQUALS);
//      String method = consumeToken();
//    } else if (accept(KEY_BLOCK_SIZE)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(MAX_ROWS)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(MIN_ROWS)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(PACK_KEYS)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(PASSWORD)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(ROW_FORMAT)) {
//      accept(EQUALS);
//      String value = consumeToken();
//    } else if (accept(TABLESPACE)) {
//      String tablespaceName = consumeToken();
//      if (accept(STORAGE)) {
//        // TODO - AHK - Do we care if it's one of the correct tokens?
//        consumeToken();
//      }
//    } else if (accept(UNION)) {
//      String unionName = consumeToken();
//      while (accept(COMMA)) {
//        unionName = consumeToken();
//      }
//    }
//  }
//
//  /*
//  partition_options:
//    PARTITION BY
//        { [LINEAR] HASH(expr)
//        | [LINEAR] KEY(column_list)
//        | RANGE(expr)
//        | LIST(expr) }
//    [PARTITIONS num]
//    [SUBPARTITION BY
//        { [LINEAR] HASH(expr)
//        | [LINEAR] KEY(column_list) }
//      [SUBPARTITIONS num]
//    ]
//    [(partition_definition [, partition_definition] ...)]
//   */
//  private void parsePartitionOptions() {
//    if (accept(PARTITION, BY)) {
//      if (accept(LINEAR, HASH) || accept(HASH)) {
//        parseParenthesizedExpression();
//      } else if (accept(KEY) || accept(LINEAR, KEY)) {
//        parseParenthesizedExpression();
//      } else if (accept(RANGE)) {
//        parseParenthesizedExpression();
//      } else if (accept(LIST)) {
//        parseParenthesizedExpression();
//      } else {
//        // TODO - AHK - Error case?
//      }
//
//      if (accept(PARTITIONS)) {
//        String num = consumeToken();
//      }
//
//      if (accept(SUBPARTITION, BY)) {
//        if (accept(HASH) || accept(LINEAR, HASH)) {
//          parseParenthesizedExpression();
//        } else if (accept(KEY) || accept(LINEAR, KEY)) {
//          parseParenthesizedExpression();
//        } else {
//          // TODO - AHK - Error case?
//        }
//
//        if (accept(SUBPARTITIONS)) {
//          String num = consumeToken();
//        }
//      }
//
//      parsePartitionDefinition();
//      while (accept(COMMA)) {
//        parsePartitionDefinition();
//      }
//    }
//  }
//
//  private void parseParenthesizedExpression() {
//    expect(OPEN_PAREN);
//    // TODO - AHK - Deal with escaping and such at some point, but for now just
//    // eat tokens until we hit the closing )
//    // TODO - AHK - Comment back in
////    while (!_tokenizer.token().equalsIgnoreCase(CLOSE_PAREN)) {
////      consumeToken();
////    }
//    expect(CLOSE_PAREN);
//  }
//
//  /*
//  partition_definition:
//    PARTITION partition_name
//        [VALUES
//            {LESS THAN {(expr) | MAXVALUE}
//            |
//            IN (value_list)}]
//        [[STORAGE] ENGINE [=] engine_name]
//        [COMMENT [=] 'comment_text' ]
//        [DATA DIRECTORY [=] 'data_dir']
//        [INDEX DIRECTORY [=] 'index_dir']
//        [MAX_ROWS [=] max_number_of_rows]
//        [MIN_ROWS [=] min_number_of_rows]
//        [TABLESPACE [=] tablespace_name]
//        [NODEGROUP [=] node_group_id]
//        [(subpartition_definition [, subpartition_definition] ...)]
//   */
//  private void parsePartitionDefinition() {
//    if (accept(PARTITION)) {
//      String partitionName = consumeToken();
//      if (accept(VALUES)) {
//        if (accept(LESS, THAN)) {
//          if (accept(MAXVALUE)) {
//            // Nothing to do
//          } else {
//            parseParenthesizedExpression();
//          }
//        } else if (accept(IN)) {
//          expect(OPEN_PAREN);
//          parseValueList();
//          expect(CLOSE_PAREN);
//        } else {
//          // TODO - AHK - Error case?
//        }
//      }
//
//      if (accept(ENGINE) || accept(STORAGE, ENGINE)) {
//        accept(EQUALS);
//        String engineName = consumeToken();
//      }
//
//      if (accept(COMMENT)) {
//        accept(EQUALS);
//        String commentText = parseQuotedString();
//      }
//
//      if (accept(DATA, DIRECTORY)) {
//        accept(EQUALS);
//        String dataDir = parseQuotedString();
//      }
//
//      if (accept(INDEX, DIRECTORY)) {
//        accept(EQUALS);
//        String indexDir = parseQuotedString();
//      }
//
//      if (accept(MAX_ROWS)) {
//        accept(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (accept(MIN_ROWS)) {
//        accept(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (accept(TABLESPACE)) {
//        accept(EQUALS);
//        String tablespaceName = consumeToken();
//      }
//
//      if (accept(NODEGROUP)) {
//        accept(EQUALS);
//        String nodeGroupID = consumeToken();
//      }
//
//      parseSubpartitionDefinition();
//      while (accept(COMMA)) {
//        parseSubpartitionDefinition();
//      }
//    }
//  }
//
//  private void parseValueList() {
//    // TODO - AHK
//  }
//
//  private String parseQuotedString() {
//    // TODO - AHK
//    return null;
//  }
//
//  /*
//  subpartition_definition:
//    SUBPARTITION logical_name
//        [[STORAGE] ENGINE [=] engine_name]
//        [COMMENT [=] 'comment_text' ]
//        [DATA DIRECTORY [=] 'data_dir']
//        [INDEX DIRECTORY [=] 'index_dir']
//        [MAX_ROWS [=] max_number_of_rows]
//        [MIN_ROWS [=] min_number_of_rows]
//        [TABLESPACE [=] tablespace_name]
//        [NODEGROUP [=] node_group_id]
//   */
//  private void parseSubpartitionDefinition() {
//    if (accept(SUBPARTITION)) {
//      String logicalName = consumeToken();
//
//      if (accept(ENGINE) || accept(STORAGE, ENGINE)) {
//        accept(EQUALS);
//        String engineName = consumeToken();
//      }
//
//      if (accept(COMMENT)) {
//        accept(EQUALS);
//        String commentText = parseQuotedString();
//      }
//
//      if (accept(DATA, DIRECTORY)) {
//        accept(EQUALS);
//        String dataDir = parseQuotedString();
//      }
//
//      if (accept(INDEX, DIRECTORY)) {
//        accept(EQUALS);
//        String indexDir = parseQuotedString();
//      }
//
//      if (accept(MAX_ROWS)) {
//        accept(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (accept(MIN_ROWS)) {
//        accept(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (accept(TABLESPACE)) {
//        accept(EQUALS);
//        String tablespaceName = consumeToken();
//      }
//
//      if (accept(NODEGROUP)) {
//        accept(EQUALS);
//        String nodeGroupID = consumeToken();
//      }
//    }
//  }
//
//  private void parseSelectStatement() {
//    // TODO - AHK
//  }
}
