package tosa.loader.parser.mysql;

import org.slf4j.LoggerFactory;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.TableData;
import tosa.loader.data.types.DateColumnTypePersistenceHandler;
import tosa.loader.data.types.TimestampColumnTypePersistenceHandler;
import tosa.loader.parser.ISQLParser;
import tosa.loader.parser.SQLParserConstants;
import tosa.loader.parser.tree.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/11/11
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySQL51Parser implements ISQLParser, SQLParserConstants {

  @Override
  public List<TableData> parseDDLFile(String fileContents) {
    List<CreateTableStatement> createTableStatements = new MySQL51CreateTableParser().parseSQLFile(fileContents);
    return transformCreateTableStatements(createTableStatements);
  }

  private List<TableData> transformCreateTableStatements(List<CreateTableStatement> statements) {
    List<TableData> tableData = new ArrayList<TableData>();
    for (CreateTableStatement statement : statements) {
      tableData.add(transformCreateTableStatement(statement));
    }
    return tableData;
  }

  private TableData transformCreateTableStatement(CreateTableStatement statement) {
    return new TableData(statement.getTableName().getValue(),
        transformColumnDefinitions(statement.getColumnDefinitions()),
        statement);
  }

  private List<ColumnData> transformColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
    List<ColumnData> results = new ArrayList<ColumnData>();
    for (ColumnDefinition columnDefinition : columnDefinitions) {
      results.add(transformColumnDefinition(columnDefinition));
    }
    return results;
  }

  private ColumnData transformColumnDefinition(ColumnDefinition columnDefinition) {
    return new ColumnData(columnDefinition.getName().getValue(),
        transformDataType(columnDefinition.getColumnDataType()),
        columnDefinition);
  }

  private DBColumnTypeImpl transformDataType(ColumnDataType columnDataType) {
    // TODO - AHK - Pull it out into a different method
    Integer length = null;
    if (columnDataType.getLength() != null) {
      String lengthStr = columnDataType.getLength().getLength().getValue();
      length = Integer.valueOf(lengthStr);
    }
    switch (columnDataType.getType()) {
      case BIT:
        if (length == null || length == 1) {
          return new DBColumnTypeImpl(BIT, BIT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.BIT);
        } else {
          // TODO - AHK - Handle bit fields that have a length greater than 1
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + BIT);
          return null;
        }
      case BOOL:
        // BOOL and BOOLEAN are equivalent to TINYINT(1)
        return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
      case TINYINT:
        if (length != null && length == 1) {
          // Treat TINYINT(1) as a boolean type
          return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BOOLEAN_ITYPE, Types.TINYINT);
        } else if (isNumericTypeSigned(columnDataType)) {
          return new DBColumnTypeImpl(TINYINT, TINYINT, DBColumnTypeImpl.BYTE_ITYPE, Types.TINYINT);
        } else {
          // TODO - AHK - Handle unsigned tiny ints
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + TINYINT);
          return null;
        }
      case SMALLINT:
        if (isNumericTypeSigned(columnDataType)) {
          return new DBColumnTypeImpl(SMALLINT, SMALLINT, DBColumnTypeImpl.SHORT_ITYPE ,Types.SMALLINT);
        } else {
          // TODO - AHK  - Handle unsigned small ints
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + SMALLINT);
          return null;
        }
      case MEDIUMINT:
        if (isNumericTypeSigned(columnDataType)) {
          return new DBColumnTypeImpl(MEDIUMINT, MEDIUMINT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
        } else {
          // TODO - AHK - Handle unsigned medium ints
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + MEDIUMINT);
          return null;
        }
      case INT:
        if (isNumericTypeSigned(columnDataType)) {
          return new DBColumnTypeImpl(INT, INT, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
        } else {
          // TODO - AHK - Handle unsigned integers
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + INTEGER);
          return null;
        }
      case BIGINT:
        if (isNumericTypeSigned(columnDataType)) {
          return new DBColumnTypeImpl(BIGINT, BIGINT, DBColumnTypeImpl.LONG_ITYPE, Types.BIGINT);
        } else {
          // TODO - AHK - Handle unsigned big ints
          LoggerFactory.getLogger("Tosa").debug("***Unhandled column type UNSIGNED " + BIGINT);
          return null;
        }
      case REAL:
        // TODO - AHK - Technically this is wrong if REAL_AS_FLOAT is set in the DB
        return new DBColumnTypeImpl(DOUBLE, DOUBLE, DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE);
      case DOUBLE:
        return new DBColumnTypeImpl(DOUBLE, DOUBLE, DBColumnTypeImpl.DOUBLE_ITYPE, Types.DOUBLE);
      case FLOAT:
        return new DBColumnTypeImpl(FLOAT, FLOAT, DBColumnTypeImpl.FLOAT_ITYPE, Types.FLOAT);
      case DECIMAL:
        // TODO - AHK - Should be BigInteger if the scale is 0
        return new DBColumnTypeImpl(DECIMAL, DECIMAL, DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL);
      case NUMERIC:
        // TODO - AHK - Should be BigInteger if the scale is 0
        return new DBColumnTypeImpl(DECIMAL, DECIMAL, DBColumnTypeImpl.BIG_DECIMAL_ITYPE, Types.DECIMAL);
      case DATE:
        return new DBColumnTypeImpl(DATE, DATE, DBColumnTypeImpl.DATE_ITYPE, Types.DATE, new DateColumnTypePersistenceHandler());
      case TIMESTAMP:
        return new DBColumnTypeImpl(TIMESTAMP, TIMESTAMP, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
      case TIME:
        // TODO - AHK
        LoggerFactory.getLogger("Tosa").debug("***Unhandled column type " + TIME);
        return null;
      case DATETIME:
        return new DBColumnTypeImpl(DATETIME, DATETIME, DBColumnTypeImpl.DATE_ITYPE, Types.TIMESTAMP, new TimestampColumnTypePersistenceHandler());
      case YEAR:
        return new DBColumnTypeImpl(YEAR, YEAR, DBColumnTypeImpl.INTEGER_ITYPE, Types.INTEGER);
      case BINARY:
        return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
      case CHAR:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(BINARY, BINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BINARY);
        } else {
          return new DBColumnTypeImpl(CHAR, CHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
        }
      case NCHAR:
        return new DBColumnTypeImpl(NCHAR, NCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.CHAR);
      case VARCHAR:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
        } else {
          return new DBColumnTypeImpl(VARCHAR, VARCHAR, DBColumnTypeImpl.STRING_ITYPE, Types.VARCHAR);
        }
      case VARBINARY:
        return new DBColumnTypeImpl(VARBINARY, VARBINARY, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.VARBINARY);
      case TINYBLOB:
        return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      case MEDIUMBLOB:
        return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      case BLOB:
        return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      case LONGBLOB:
        return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
      case TINYTEXT:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(TINYBLOB, TINYBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
        } else {
          return new DBColumnTypeImpl(TINYTEXT, TINYTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
        }
      case TEXT:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(BLOB, BLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
        } else {
          return new DBColumnTypeImpl(TEXT, TEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
        }
      case MEDIUMTEXT:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(MEDIUMBLOB, MEDIUMBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
        } else {
          return new DBColumnTypeImpl(MEDIUMTEXT, MEDIUMTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
        }
      case LONGTEXT:
        if ("binary".equals(getCharacterSet(columnDataType))) {
          return new DBColumnTypeImpl(LONGBLOB, LONGBLOB, DBColumnTypeImpl.pBYTE_ARRAY_ITYPE, Types.BLOB);
        } else {
          return new DBColumnTypeImpl(LONGTEXT, LONGTEXT, DBColumnTypeImpl.STRING_ITYPE, Types.CLOB);
        }
      default:
        // TODO - ERROR
    }

    return null;
  }

  private boolean isNumericTypeSigned(ColumnDataType columnDataType) {
    for (SQLParsedElement parsedElement : columnDataType.getModifiers()) {
      if (parsedElement instanceof NumericDataTypeModifier) {
        switch (((NumericDataTypeModifier) parsedElement).getType()) {
          case SIGNED:
            return true;
          case UNSIGNED:
            return false;
          case ZEROFILL:
            return false;
          default:
            // TODO - ERROR
        }
      }
    }

    return true;
  }

  private String getCharacterSet(ColumnDataType columnDataType) {
    for (SQLParsedElement parsedElement : columnDataType.getModifiers()) {
      if (parsedElement instanceof CharacterSetExpression) {
        return ((CharacterSetExpression) parsedElement).getCharSetName().getValue();
      } else if (parsedElement instanceof CharacterTypeAttribute) {
        // TODO - AHK
      }
    }

    return null;
  }
}
