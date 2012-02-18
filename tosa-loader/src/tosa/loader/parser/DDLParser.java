package tosa.loader.parser;

import org.slf4j.LoggerFactory;
import tosa.loader.parser.tree.*;

import java.util.*;

public class DDLParser extends SQLParserBase {

  // TODO - AHK - Fill out the list here
  private static final Set<String> GOSU_RESERVED_WORDS = new HashSet<String>(Arrays.asList(
    "new"
  ));

  // Names of built-in Tosa properties.  Stored as lower-case, since we'll compare the lower case
  // column name to this set
  private static final Set<String> SPECIAL_PROPERTY_NAMES = new HashSet<String>(Arrays.asList(
    "dbtable", "new", "_new", "Type"
  ));

  private ArrayList<CreateTableStatement> _tableStatements;
  private ArrayList<SQLParsedElement> _createDefinitions;


  public DDLParser(Token token) {
    super(token);
  }

  public List<CreateTableStatement> parseDDL() {
    _tableStatements = new ArrayList<CreateTableStatement>();
    while (!isEOF()) {
      CreateTableStatement statement = parseCreate();
      if (statement != null) {
        _tableStatements.add(statement);
      }
    }
    return _tableStatements;
  }

  private CreateTableStatement parseCreate() {
    if (match(CREATE)) {
      Token start = lastMatch();
      match(TEMPORARY); // Discard; we don't care
      if(match(TABLE)) {
        match(IF, NOT, EXISTS);
        Token tableName = takeToken();
        List<SQLParsedElement> children = new ArrayList<SQLParsedElement>();
        if (match(LIKE)) {
          Token likeTableName = takeToken();
          // TODO - AHK - And what do we do with it, exactly?
        } else if (match(OPEN_PAREN, LIKE)) {
          Token likeTableName = takeToken();
          expect(CLOSE_PAREN);
          // TODO - AHK - And what do we do with it, exactly?
        } else if (match(OPEN_PAREN)) {
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
        CreateTableStatement createTableStatement = new CreateTableStatement(start, lastMatch(), tableName, children);
        String tableNameStr = maybeUnwrapQuotedIdentifier(tableName);

        for (CreateTableStatement tableStatement : _tableStatements) {
          if (maybeUnwrapQuotedIdentifier(tableStatement.getTableName()).equalsIgnoreCase(tableNameStr)) {
            createTableStatement.addParseError(new SQLParseError(tableName, "Duplicate tables were found with the names " + tableName +
              " and " + tableStatement.getTableName().toString() +
              ".  Table names in Tosa must be case-insensitively unique within a database."));
          }
        }

        if (!Character.isJavaIdentifierStart(tableNameStr.charAt(0))) {
          createTableStatement.addParseError(new SQLParseError(tableName, "The table name " + tableNameStr + " is not a valid type name.  The first character " + tableNameStr.charAt(0) + " is not a valid start to a type name."));
        }

        for (int i = 1; i < tableNameStr.length(); i++) {
          if (!Character.isJavaIdentifierPart(tableNameStr.charAt(i))) {
            createTableStatement.addParseError(new SQLParseError(tableName, "The table name " + tableNameStr + " is not a valid type name.  The character " + tableNameStr.charAt(i) + " is not a valid character in a type name."));
          }
        }

        if (GOSU_RESERVED_WORDS.contains(tableNameStr.toLowerCase())) {
          createTableStatement.addParseError(new SQLParseError(tableName, "The table name " + tableNameStr + " conflicts with a Gosu reserved word."));
        }

        if (tableNameStr.toLowerCase().equals("transaction")) {
          createTableStatement.addParseError(new SQLParseError(tableName, "Tosa tables cannot be named Transaction (in any case), as a built-in type named Transaction is automatically created for every ddl namespace."));
        } else if (tableNameStr.toLowerCase().equals("database")) {
          createTableStatement.addParseError(new SQLParseError(tableName, "Tosa tables cannot be named Database (in any case), as a built-in type named Database is automatically created for every ddl namespace."));
        }

        boolean foundId = false;
        for (SQLParsedElement createDefinition : _createDefinitions) {
          if (createDefinition instanceof ColumnDefinition) {
            Token name = ((ColumnDefinition) createDefinition).getName();
            if ("id".equalsIgnoreCase(maybeUnwrapQuotedIdentifier(name))) {
              foundId = true;
            }
          }
        }

        if (!foundId) {
          createTableStatement.addParseError(new SQLParseError(tableName, "No id column was found on the table " + tableNameStr + ".  Every table in Tosa should have a table named \"id\" of type BIGINT."));
        }
        
        return createTableStatement;
      }
    }
    while (!match(SEMI_COLON)) {
      takeToken();
    }
    return null;
  }

  private List<SQLParsedElement> parseCreateDefinitions() {
    // TODO - AHK - Is it legal to have 0 columns?
    _createDefinitions = new ArrayList<SQLParsedElement>();
    SQLParsedElement createDefinition = parseCreateDefinition();
    if (createDefinition != null) {
      _createDefinitions.add(createDefinition);
    }
    while (match(COMMA)) {
      createDefinition = parseCreateDefinition();
      if (createDefinition != null) {
        _createDefinitions.add(createDefinition);
      }
    }
    return _createDefinitions;
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
        start = getCurrentToken();
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
    if (match(ASC)) {
      sortDirection = IndexColumnName.IndexColumnSortDirection.ASC;
    } else if (match(DESC)) {
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
    } else if (match(WITH, PARSER)) {
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

    ColumnDefinition columnDefinition = new ColumnDefinition(columnName, lastMatch(), columnName, dataType, columnOptions);

    String actualColumnName = maybeUnwrapQuotedIdentifier(columnName);
    
    if (!Character.isJavaIdentifierStart(actualColumnName.charAt(0))) {
      columnDefinition.addParseError(new SQLParseError(columnName, "The column name " + actualColumnName + " is not a valid property name.  The first character " + actualColumnName.charAt(0) + " is not a valid start to a property name."));
    }

    for (int i = 1; i < actualColumnName.length(); i++) {
      if (!Character.isJavaIdentifierPart(actualColumnName.charAt(i))) {
        columnDefinition.addParseError(new SQLParseError(columnName, "The column name " + actualColumnName + " is not a valid property name.  The character " + actualColumnName.charAt(i) + " is not a valid character in a property name."));
      }
    }

    if (SPECIAL_PROPERTY_NAMES.contains(actualColumnName.toLowerCase())) {
      columnDefinition.addParseError(new SQLParseError(columnName, "The column name \"" + actualColumnName + "\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface"));
    }

    // TODO - AHK - Some more reliable way to detect this besides just matching the String "PlaceHolder"
    // TODO cgross - is this correct?
    if (dataType.getType() == null) {
      columnDefinition.addParseError(new SQLParseError(columnName, "The data type for this column is not currently handled.  It will appear in Tosa as a String, but it may not function correctly.", true));
    }

    // TODO cgross - verify w/ keef that we support mixed quoting/non-quoting
//    if (actualColumnName.charAt(0) != '"') {
//      columnDefinition.addParseError(new SQLParseError(columnName, "The column name was not quoted in the DDL file.  Tosa will generate quoted column names in all SQL it generates, so column names should be specified quoted in the DDL as well.", true));
//    }

    if (actualColumnName.equalsIgnoreCase("id")) {
      if (!actualColumnName.equals("id")) {
        columnDefinition.addParseError(new SQLParseError(columnName, "The id column should always be named \"id\" but in this case it was named \"" + actualColumnName + "\""));
      }

      if (dataType.getType() != ColumnDataType.Type.BIGINT) {
        columnDefinition.addParseError(new SQLParseError(columnName, "The id column should always be a BIGINT, since it will be represented as a Long in Tosa.  The id column was found to be of type " + dataType.getType().name()));
      }

      // TODO - AHK - Check for auto-increment
    }

    return columnDefinition;
  }

  private String maybeUnwrapQuotedIdentifier(Token token) {
    String rawColumnName = token.toString();
    //strip quotes
    boolean quoted = rawColumnName.startsWith("\"");
    String actualColumnName;
    if (quoted) {
      actualColumnName = rawColumnName.substring(1, rawColumnName.length() - 1);
    } else {
      actualColumnName = rawColumnName;
    }
    return actualColumnName;
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
    if (match(BIT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BIT, length, null);
    } else if (match(TINYINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TINYINT, length, modifiers);
    } else if (match(BOOL) || match(BOOLEAN)) {
      // BOOL and BOOLEAN are equivalent to TINYINT(1)
      Token start = lastMatch();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BOOL, null, null);
    } else if (match(SMALLINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.SMALLINT, length, modifiers);
    } else if (match(MEDIUMINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.MEDIUMINT, length, modifiers);
    } else if (match(INT) || match(INTEGER)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.INT, length, modifiers);
    } else if (match(BIGINT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLength();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BIGINT, length, modifiers);
    } else if (match(REAL)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.REAL, length, modifiers);
    } else if (match(DOUBLE, PRECISION) || match(DOUBLE)) {
      Token start = lastMatch();
      if (start.match(PRECISION)) {
        start = start.previous();
      }
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.DOUBLE, length, modifiers);
    } else if (match(FLOAT)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.FLOAT, length, modifiers);
    } else if (match(DECIMAL) || match(DEC)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.DECIMAL, length, modifiers);
    } else if (match(NUMERIC) || match(FIXED)) {
      Token start = lastMatch();
      ColumnLengthExpression length = parseLengthAndDecimals();
      List<NumericDataTypeModifier> modifiers = parseNumericModifiers();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.NUMERIC, length, modifiers);
    } else {
      return null;
    }
  }

  private ColumnLengthExpression parseLength() {
    if (match(OPEN_PAREN)) {
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
    if (match(SIGNED)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.SIGNED);
    } else if (match(UNSIGNED)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.UNSIGNED);
    } else if (match(ZEROFILL)) {
      return new NumericDataTypeModifier(lastMatch(), NumericDataTypeModifier.Type.ZEROFILL);
    } else {
      return null;
    }
  }

  private ColumnLengthExpression parseLengthAndDecimals() {
    // TODO - AHK - Sometimes the comma isn't optional, but I don't think that matters here
    if (match(OPEN_PAREN)) {
      Token start = lastMatch();
      Token length = takeToken();
      Token decimals = null;
      if (match(COMMA)) {
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
    if (match(DATE)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.DATE);
    } else if (match(TIME)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TIME);
    } else if (match(TIMESTAMP)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TIMESTAMP);
    } else if (match(DATETIME)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.DATETIME);
    } else if (match(YEAR)) {
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
    if (match(CHAR, BYTE) || match(BINARY)) {
      Token start = lastMatch();
      if (start.match(BYTE)) {
        start = start.previous();
      }
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BINARY, lengthExpression, null);
    } else if (match(CHAR) || match(CHARACTER)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.CHAR, lengthExpression, characterTypeAttributes);
    } else if (match(NATIONAL, CHAR) || match(NCHAR)) {
      Token start = lastMatch();
      if (start.match(CHAR)) {
        start = start.previous();
      }
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.NCHAR, lengthExpression, characterTypeAttributes);
    } else if (match(VARCHAR)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.VARCHAR, lengthExpression, characterTypeAttributes);
    } else if (match(VARBINARY)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.VARBINARY, lengthExpression, null);
    } else if (match(TINYBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.TINYBLOB);
    } else if (match(BLOB)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.BLOB, lengthExpression, null);
    } else if (match(MEDIUMBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.MEDIUMBLOB);
    } else if (match(LONGBLOB)) {
      return new ColumnDataType(lastMatch(), ColumnDataType.Type.LONGBLOB);
    } else if (match(TINYTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TINYTEXT, null, characterTypeAttributes);
    } else if (match(TEXT)) {
      Token start = lastMatch();
      ColumnLengthExpression lengthExpression = parseLength();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.TEXT, lengthExpression, characterTypeAttributes);
    } else if (match(MEDIUMTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.MEDIUMTEXT, null, characterTypeAttributes);
    } else if (match(LONGTEXT)) {
      Token start = lastMatch();
      List<SQLParsedElement> characterTypeAttributes = parseCharTypeAttributes();
      return new ColumnDataType(start, lastMatch(), ColumnDataType.Type.LONGTEXT, null, characterTypeAttributes);
    } else if (match(ENUM)) {
      // TODO - AHK
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      LoggerFactory.getLogger("Tosa").warn("***Unhandled column type " + ENUM);
      return null;
    } else if (match(SET)) {
      // TODO - AHK
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
      LoggerFactory.getLogger("Tosa").warn("***Unhandled column type " + SET);
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
    if (match(CHARACTER, SET)) {
      Token start = lastMatch().previous();
      Token charSetName = takeToken();
      return new CharacterSetExpression(start, charSetName, charSetName);
    } else if (match(COLLATE)) {
      Token start = lastMatch();
      Token collation = takeToken();
      return new CollateExpression(start, collation, collation);
    } else if (match(ASCII)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.ASCII);
    } else if (match(UNICODE)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.UNICODE);
    } else if (match(BINARY)) {
      return new CharacterTypeAttribute(lastMatch(), CharacterTypeAttribute.Attribute.BINARY);
    } else {
      return null;
    }
  }

  /*
    [NOT NULL | NULL] [DEFAULT default_value]
      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
      [COMMENT 'string']
      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
      [STORAGE {DISK|MEMORY|DEFAULT}]
      [reference_definition]*/
  private SQLParsedElement parseColumnOption() {
    if (match(NOT, NULL)) {
      return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.NOT_NULL);
    } else if (match(NULL)) {
      return new ColumnOptionExpression(lastMatch(), lastMatch(), ColumnOptionExpression.ColumnOptionType.NULL);
    }

    if (match(DEFAULT)) {
      Token start = lastMatch();
      // TODO - AHK - This is likely incorrect (i.e. it could be a quoted string or something else)
      Token value = takeToken();
      return new DefaultValueExpression(start, value, value);
    }

    if (match(AUTO_INCREMENT)) {
      return new ColumnOptionExpression(lastMatch(), lastMatch(), ColumnOptionExpression.ColumnOptionType.AUTO_INCREMENT);
    }

    if (match(UNIQUE)) {
      if (match(KEY)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.UNIQUE_KEY);
      } else {
        return new ColumnOptionExpression(lastMatch(), lastMatch(), ColumnOptionExpression.ColumnOptionType.UNIQUE);
      }
    } else if (match(PRIMARY)) {
      expect(KEY);
      return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.PRIMARY_KEY);
    }

    if (match(COMMENT)) {
//      String comment = parseQuotedString();
//      return true;
      // TODO - AHK
      return null;
    }

    if (match(COLUMN_FORMAT)) {
      if (match(FIXED)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.COLUMN_FORMAT_FIXED);
      } else if (match(DYNAMIC)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.COLUMN_FORMAT_DYNAMIC);
      } else if (match(DEFAULT)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.COLUMN_FORMAT_DEFAULT);
      } else {
        // TODO - Error
      }
    }

    if (match(STORAGE)) {
      if (match(DISK)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.STORAGE_DISK);
      } else if (match(MEMORY)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.STORAGE_MEMORY);
      } else if (match(DEFAULT)) {
        return new ColumnOptionExpression(lastMatch().previous(), lastMatch(), ColumnOptionExpression.ColumnOptionType.STORAGE_DEFAULT);
      } else {
        // TODO - Error
      }
    }

    // TODO - AHK
//    if (parseReferenceDefinition()) {
//      return true;
//    }

    return null;
  }


  private CheckExpressionDefinition parseCheckExpression() {
    // TODO - AHK
    return null;
  }

  // TODO - AHK - Kill the _New property?

//
//  private boolean match(String... tokens) {
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
//    if (match(CREATE)) {
//      match(TEMPORARY); // Discard; we don't care
//      expect(TABLE);
//      match(IF, NOT, EXISTS);
//      String tableName = stripQuotes(consumeToken());
//      List<ColumnData> columns = null;
//      if (match(LIKE)) {
//        String likeTableName = consumeToken();
//        // TODO - AHK - And what do we do with it, exactly?
//      } else if (match(OPEN_PAREN, LIKE)) {
//        String likeTableName = consumeToken();
//        expect(CLOSE_PAREN);
//        // TODO - AHK - And what do we do with it, exactly?
//      } else if (match(OPEN_PAREN)) {
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
//    while (match(COMMA)) {
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
//    if (match(CONSTRAINT)) {
//      String symbolName;
//      if (peek(PRIMARY, KEY) || peek(UNIQUE) || peek(FOREIGN, KEY)) {
//        symbolName = null;
//      } else {
//        symbolName = consumeToken();
//      }
//    }
//
//    if (match(PRIMARY, KEY)) {
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (match(INDEX) || match(KEY)) {
//      parseIndexName();
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (match(UNIQUE)) {
//      // TODO - AHK
//      if (!match(INDEX)) {
//        expect(KEY);
//      }
//      parseIndexName();
//      parseIndexType();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (match(FULLTEXT) || match(SPATIAL)) {
//      if (!match(INDEX)) {
//        expect(KEY);
//      }
//      parseIndexName();
//      parseIndexColumnNames();
//      parseIndexOptions();
//    } else if (match(FOREIGN, KEY)) {
//      parseIndexName();
//      parseIndexColumnNames();
//      parseReferenceDefinition();
//    } else if (match(CHECK)) {
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
//    if (match(USING)) {
//      if (!match(BTREE)) {
//        expect(HASH);
//      }
//    }
//  }
//
//  private void parseIndexOptions() {
//    if (match(KEY_BLOCK_SIZE)) {
//      match(EQUALS);
//      String value = consumeToken();
//      parseIndexOptions();
//    } else if (match(USING)) {
//      // TODO - AHK - Sould there be an expect() variant for an OR situation like this?
//      if (!match(BTREE)) {
//        expect(HASH);
//      }
//      parseIndexOptions();
//    } else if (match(WITH, PARSER)) {
//      String parserName = consumeToken();
//    }
//  }
//
//  // TODO - AHK - This needs to be order-independent
//  private boolean parseReferenceDefinition() {
//    if (match(REFERENCES)) {
//      String tableName = consumeToken();
//      expect(OPEN_PAREN);
//      String columnName = consumeToken();
//      while (match(COMMA)) {
//        columnName = consumeToken();
//      }
//      expect(CLOSE_PAREN);
//      if (match(MATCH, FULL) || match(MATCH, PARTIAL) || match(MATCH, SIMPLE)) {
//        // Just eat it
//      }
//      if (match(ON, DELETE)) {
//        parseReferenceOption();
//      }
//      if (match(ON, UPDATE)) {
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
//    if (match(RESTRICT) || match(CASCADE) || match(SET, NULL) || match(NO, ACTION)) {
//      // Just eat it
//    } else {
//      // Error case
//    }
//  }
//
//  private void parseIndexColumnNames() {
//    expect(OPEN_PAREN);
//    parseIndexColName();
//    while (match(COMMA)) {
//      parseIndexColName();
//    }
//    expect(CLOSE_PAREN);
//  }
//
//  private void parseIndexColName() {
//    String columnName = consumeToken();
//    if (match(OPEN_PAREN)) {
//      String length = consumeToken();
//      expect(CLOSE_PAREN);
//    }
//    if (match(ASC)) {
//      // Just eat it
//    } else if (match(DESC)) {
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
//    if (match(NOT, NULL)) {
//      return true;
//    } else if (match(NULL)) {
//      return true;
//    }
//
//    if (match(DEFAULT)) {
//      String defaultValue = consumeToken();
//      return true;
//    }
//
//    if (match(AUTO_INCREMENT)) {
//      return true;
//    }
//
//    if (match(UNIQUE)) {
//      match(KEY);
//      return true;
//    } else if (match(PRIMARY)) {
//      expect(KEY);
//      return true;
//    } else if (match(KEY)) {
//      return true;
//    }
//
//    if (match(COMMENT)) {
//      String comment = parseQuotedString();
//      return true;
//    }
//
//    if (match(COLUMN_FORMAT)) {
//      if (match(FIXED) || match(DYNAMIC) || match(DEFAULT)) {
//        return true;
//      } else {
//        // TODO - AHK - Error case
//        return true;
//      }
//    }
//
//    if (match(STORAGE)) {
//      if (match(DISK) || match(MEMORY) || match(DEFAULT)) {
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
//    if (match(BIT)) {
//      Integer length = parseLength();
//      if (length == null || length == 1) {
//        return new DBColumnTypeImpl(BIT, BIT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT);
//      } else {
//        // TODO - AHK - Handle bit fields that have a length greater than 1
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + BIT);
//        return null;
//      }
//    } else if (match(TINYINT)) {
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
//    } else if (match(BOOL) || match(BOOLEAN)) {
//      // BOOL and BOOLEAN are equivalent to TINYINT(1)
//      return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
//    } else if (match(SMALLINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(SMALLINT, SMALLINT, DBColumnTypeImpl.SHORT_ITYPE ,Types.SMALLINT);
//      } else {
//        // TODO - AHK  - Handle unsigned small ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + SMALLINT);
//        return null;
//      }
//    } else if (match(MEDIUMINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(MEDIUMINT, MEDIUMINT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//      } else {
//        // TODO - AHK - Handle unsigned medium ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + MEDIUMINT);
//        return null;
//      }
//    } else if (match(INT) || match(INTEGER)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(INT, INT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//      } else {
//        // TODO - AHK - Handle unsigned integers
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + INTEGER);
//        return null;
//      }
//    } else if (match(BIGINT)) {
//      Integer length = parseLength();
//      boolean signed = parseNumericModifiers();
//      if (signed) {
//        return new DBColumnTypeImpl(BIGINT, BIGINT, DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT);
//      } else {
//        // TODO - AHK - Handle unsigned big ints
//        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + BIGINT);
//        return null;
//      }
//    } else if (match(DOUBLE, PRECISION) || match(DOUBLE) || match(REAL)) {
//      // TODO - AHK - If the REAL_AS_FLOAT mode is set on the DB, this will be incorrect
//      parseLengthAndDecimals();
//      boolean signed = parseNumericModifiers();
//      return new DBColumnTypeImpl(DOUBLE, DOUBLE, DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE);
//    } else if (match(FLOAT)) {
//      // TODO - AHK - It's a different deal if there's a length and a precision versus just a single number
//      parseLengthAndDecimals();
//      boolean signed = parseNumericModifiers();
//      return new DBColumnTypeImpl(FLOAT, FLOAT, DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT);
//    } else if (match(DECIMAL) || match(DEC) || match(NUMERIC) || match(FIXED)) {
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
//    if (match(DATE)) {
//      return new DBColumnTypeImpl(DATE, DATE, DBColumnTypeImpl.DATE_ITYPE, Types.DATE, new DateColumnTypePersistenceHandler());
//    } else if (match(TIME)) {
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + TIME);
//      return null;
//    } else if (match(TIMESTAMP)) {
//      return new DBColumnTypeImpl(TIMESTAMP, TIMESTAMP, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
//    } else if (match(DATETIME)) {
//      return new DBColumnTypeImpl(DATETIME, DATETIME, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
//    } else if (match(YEAR)) {
//      return new DBColumnTypeImpl(YEAR, YEAR, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
//    } else {
//      return null;
//    }
//  }
//
//  private DBColumnTypeImpl parseCharacterColumnType() {
//    if (match(CHAR, BYTE) || match(BINARY)) {
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
//    } else if (match(CHAR) || match(CHARACTER)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
//      } else {
//        return new DBColumnTypeImpl(CHAR, CHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
//      }
//    } else if (match(NATIONAL, CHAR) || match(NCHAR)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      return new DBColumnTypeImpl(NCHAR, NCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
//    } else if (match(VARCHAR)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
//      } else {
//        return new DBColumnTypeImpl(VARCHAR, VARCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR);
//      }
//    } else if (match(VARBINARY)) {
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
//    } else if (match(TINYBLOB)) {
//      // Max length is 255
//      return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (match(BLOB)) {
//      // Max length is 2^16 - 1 if not otherwise specified
//      Integer length = parseLength();
//      return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (match(MEDIUMBLOB)) {
//      // Max length is 2^24 - 1
//      return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (match(LONGBLOB)) {
//      // Max length is 2^32 - 1
//      return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//    } else if (match(TINYTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 255
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(TINYTEXT, TINYTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (match(TEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^16 - 1 if not otherwise specified
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(TEXT, TEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (match(MEDIUMTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^24 - 1
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(MEDIUMTEXT, MEDIUMTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (match(LONGTEXT)) {
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      // Max length is 2^32 - 1
//      if ("binary".equals(characterTypeAttributes._charSet)) {
//        return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
//      } else {
//        return new DBColumnTypeImpl(LONGTEXT, LONGTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
//      }
//    } else if (match(ENUM)) {
//      List<String> values = parseEnumOrSetValueList();
//      CharacterTypeAttributes characterTypeAttributes = parseCharTypeAttributes();
//      LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + ENUM);
//      return null;
//    } else if (match(SET)) {
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
//    if (match(CHARACTER, SET)) {
//      charTypeAttributes._charSet = consumeToken();
//      return true;
//    } else if (match(COLLATE)) {
//      charTypeAttributes._collation = consumeToken();
//      return true;
//    } else if (match(ASCII)) {
//      charTypeAttributes._charSet = "latin1";
//      return true;
//    } else if (match(UNICODE)) {
//      charTypeAttributes._charSet = "ucs2";
//      return true;
//    } else if (match(BINARY)) {
//      charTypeAttributes._collation = "binary";
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  // TODO - AHK - Maybe return an int instead?
//  private Integer parseLength() {
//    if (match(OPEN_PAREN)) {
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
//    if (match(SIGNED)) {
//      signed = true;
//    } else if (match(UNSIGNED)) {
//      signed = false;
//    }
//
//    // Zerofill columns are automatically treated as unsigned
//    if (match(ZEROFILL)) {
//      signed = false;
//    }
//
//    return signed;
//  }
//
//  private void parseLengthAndDecimals() {
//    // TODO - AHK - Sometimes the comma isn't optional, but I don't think that matters here
//    if (match(OPEN_PAREN)) {
//      String length = consumeToken();
//      if (match(COMMA)) {
//        String decimals = consumeToken();
//      }
//      expect(CLOSE_PAREN);
//    }
//  }
//
//  private String parseCharSet() {
//    if (match(CHARACTER, SET)) {
//      return consumeToken();
//    } else {
//      return null;
//    }
//  }
//
//  private String parseCollation() {
//    if (match(COLLATE)) {
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
//    while (match(COMMA)) {
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
//    while (match(COMMA)) {
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
//    if (match(ENGINE)) {
//      match(EQUALS);
//      String engineName = consumeToken();
//    } else if (match(AUTO_INCREMENT)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(AVG_ROW_LENGTH)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(DEFAULT, CHARACTER, SET) || match(CHARACTER, SET)) {
//      match(EQUALS);
//      String charsetName = consumeToken();
//    } else if (match(CHECKSUM)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(DEFAULT, COLLATE) || match(COLLATE)) {
//      match(EQUALS);
//      String collationName = consumeToken();
//    } else if (match(COMMENT)) {
//      match(EQUALS);
//      String comment = consumeToken();
//    } else if (match(CONNECTION)) {
//      match(EQUALS);
//      String connection = consumeToken();
//    } else if (match(DATA, DICTIONARY)) {
//      match(EQUALS);
//      String path = consumeToken();
//    } else if (match(DELAY_KEY_WRITE)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(INDEX, DIRECTORY)) {
//      match(EQUALS);
//      String path = consumeToken();
//    } else if (match(INSERT_METHOD)) {
//      match(EQUALS);
//      String method = consumeToken();
//    } else if (match(KEY_BLOCK_SIZE)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(MAX_ROWS)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(MIN_ROWS)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(PACK_KEYS)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(PASSWORD)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(ROW_FORMAT)) {
//      match(EQUALS);
//      String value = consumeToken();
//    } else if (match(TABLESPACE)) {
//      String tablespaceName = consumeToken();
//      if (match(STORAGE)) {
//        // TODO - AHK - Do we care if it's one of the correct tokens?
//        consumeToken();
//      }
//    } else if (match(UNION)) {
//      String unionName = consumeToken();
//      while (match(COMMA)) {
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
//    if (match(PARTITION, BY)) {
//      if (match(LINEAR, HASH) || match(HASH)) {
//        parseParenthesizedExpression();
//      } else if (match(KEY) || match(LINEAR, KEY)) {
//        parseParenthesizedExpression();
//      } else if (match(RANGE)) {
//        parseParenthesizedExpression();
//      } else if (match(LIST)) {
//        parseParenthesizedExpression();
//      } else {
//        // TODO - AHK - Error case?
//      }
//
//      if (match(PARTITIONS)) {
//        String num = consumeToken();
//      }
//
//      if (match(SUBPARTITION, BY)) {
//        if (match(HASH) || match(LINEAR, HASH)) {
//          parseParenthesizedExpression();
//        } else if (match(KEY) || match(LINEAR, KEY)) {
//          parseParenthesizedExpression();
//        } else {
//          // TODO - AHK - Error case?
//        }
//
//        if (match(SUBPARTITIONS)) {
//          String num = consumeToken();
//        }
//      }
//
//      parsePartitionDefinition();
//      while (match(COMMA)) {
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
//    if (match(PARTITION)) {
//      String partitionName = consumeToken();
//      if (match(VALUES)) {
//        if (match(LESS, THAN)) {
//          if (match(MAXVALUE)) {
//            // Nothing to do
//          } else {
//            parseParenthesizedExpression();
//          }
//        } else if (match(IN)) {
//          expect(OPEN_PAREN);
//          parseValueList();
//          expect(CLOSE_PAREN);
//        } else {
//          // TODO - AHK - Error case?
//        }
//      }
//
//      if (match(ENGINE) || match(STORAGE, ENGINE)) {
//        match(EQUALS);
//        String engineName = consumeToken();
//      }
//
//      if (match(COMMENT)) {
//        match(EQUALS);
//        String commentText = parseQuotedString();
//      }
//
//      if (match(DATA, DIRECTORY)) {
//        match(EQUALS);
//        String dataDir = parseQuotedString();
//      }
//
//      if (match(INDEX, DIRECTORY)) {
//        match(EQUALS);
//        String indexDir = parseQuotedString();
//      }
//
//      if (match(MAX_ROWS)) {
//        match(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (match(MIN_ROWS)) {
//        match(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (match(TABLESPACE)) {
//        match(EQUALS);
//        String tablespaceName = consumeToken();
//      }
//
//      if (match(NODEGROUP)) {
//        match(EQUALS);
//        String nodeGroupID = consumeToken();
//      }
//
//      parseSubpartitionDefinition();
//      while (match(COMMA)) {
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
//    if (match(SUBPARTITION)) {
//      String logicalName = consumeToken();
//
//      if (match(ENGINE) || match(STORAGE, ENGINE)) {
//        match(EQUALS);
//        String engineName = consumeToken();
//      }
//
//      if (match(COMMENT)) {
//        match(EQUALS);
//        String commentText = parseQuotedString();
//      }
//
//      if (match(DATA, DIRECTORY)) {
//        match(EQUALS);
//        String dataDir = parseQuotedString();
//      }
//
//      if (match(INDEX, DIRECTORY)) {
//        match(EQUALS);
//        String indexDir = parseQuotedString();
//      }
//
//      if (match(MAX_ROWS)) {
//        match(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (match(MIN_ROWS)) {
//        match(EQUALS);
//        String value = consumeToken();
//      }
//
//      if (match(TABLESPACE)) {
//        match(EQUALS);
//        String tablespaceName = consumeToken();
//      }
//
//      if (match(NODEGROUP)) {
//        match(EQUALS);
//        String nodeGroupID = consumeToken();
//      }
//    }
//  }
//
//  private void parseSelectStatement() {
//    // TODO - AHK
//  }
}
