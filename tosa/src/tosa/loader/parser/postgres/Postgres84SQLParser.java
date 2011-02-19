package tosa.loader.parser.postgres;

import org.slf4j.LoggerFactory;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBColumnTypeImpl;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.SQLParserConstants;
import tosa.loader.parser.SQLTokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Postgres84SQLParser implements SQLParserConstants {

  /*

  Grammar definition from http://www.postgresql.org/docs/8.4/interactive/sql-createtable.html

  CREATE [ [ GLOBAL | LOCAL ] { TEMPORARY | TEMP } ] TABLE table_name ( [
  { column_name data_type [ DEFAULT default_expr ] [ column_constraint [ ... ] ]
    | table_constraint
    | LIKE parent_table [ { INCLUDING | EXCLUDING } { DEFAULTS | CONSTRAINTS | INDEXES } ] ... }
    [, ... ]
] )
[ INHERITS ( parent_table [, ... ] ) ]
[ WITH ( storage_parameter [= value] [, ... ] ) | WITH OIDS | WITHOUT OIDS ]
[ ON COMMIT { PRESERVE ROWS | DELETE ROWS | DROP } ]
[ TABLESPACE tablespace ]

where column_constraint is:

[ CONSTRAINT constraint_name ]
{ NOT NULL |
  NULL |
  UNIQUE index_parameters |
  PRIMARY KEY index_parameters |
  CHECK ( expression ) |
  REFERENCES reftable [ ( refcolumn ) ] [ MATCH FULL | MATCH PARTIAL | MATCH SIMPLE ]
    [ ON DELETE action ] [ ON UPDATE action ] }
[ DEFERRABLE | NOT DEFERRABLE ] [ INITIALLY DEFERRED | INITIALLY IMMEDIATE ]

and table_constraint is:

[ CONSTRAINT constraint_name ]
{ UNIQUE ( column_name [, ... ] ) index_parameters |
  PRIMARY KEY ( column_name [, ... ] ) index_parameters |
  CHECK ( expression ) |
  FOREIGN KEY ( column_name [, ... ] ) REFERENCES reftable [ ( refcolumn [, ... ] ) ]
    [ MATCH FULL | MATCH PARTIAL | MATCH SIMPLE ] [ ON DELETE action ] [ ON UPDATE action ] }
[ DEFERRABLE | NOT DEFERRABLE ] [ INITIALLY DEFERRED | INITIALLY IMMEDIATE ]

index_parameters in UNIQUE and PRIMARY KEY constraints are:

[ WITH ( storage_parameter [= value] [, ... ] ) ]
[ USING INDEX TABLESPACE tablespace ]*/


  private SQLTokenizer _tokenizer;

  public static void main(String[] args) {
    Postgres84SQLParser parser = new Postgres84SQLParser();
    DBData dbData = parser.parseFile("CREATE TABLE films (\n" +
        "    code        char(5) CONSTRAINT firstkey PRIMARY KEY,\n" +
        "    title       varchar(40) NOT NULL,\n" +
        "    did         integer NOT NULL,\n" +
        "    date_prod   date,\n" +
        "    kind        varchar(10),\n" +
        "    len         interval hour to minute\n" +
        ");");

//    DBData dbData = parser.parseFile("CREATE TABLE films (\n" +
//        "    code        char(5) CONSTRAINT firstkey PRIMARY KEY,\n" +
//        "    title       varchar(40) NOT NULL,\n" +
//        "    did         integer NOT NULL,\n" +
//        "    date_prod   date,\n" +
//        "    kind        varchar(10)\n" +
//        ");");
    LoggerFactory.getLogger("Tosa").trace("Here");
  }

  public DBData parseFile(String fileContents) {
    _tokenizer = new SQLTokenizer(fileContents);
    List<TableData> tables = new ArrayList<TableData>();
    // TODO - AHK - Other Create calls?  Other stuff?  Closing semi-colon?
    TableData table = parseCreate();
    tables.add(table);
    return new DBData(tables, null, null);
  }

  private TableData parseCreate() {
    expect(CREATE);
    parseTableModifiers();
    expect(TABLE);
    String name = parseTableName();
    expect(OPEN_PAREN);
    List<ColumnData> columns = parseColumns();
    expect(CLOSE_PAREN);

    return new TableData(name, columns);
  }

  private void parseTableModifiers() {
    if (accept(GLOBAL) || accept(LOCAL) || accept(TEMPORARY) || accept(TEMP)) {
      parseTableModifiers();
    }
  }

  private String parseTableName() {
    String tableName = consumeToken();
    return tableName;
  }

  /**
   * { column_name data_type [ DEFAULT default_expr ] [ column_constraint [ ... ] ]
   * | table_constraint
   * | LIKE parent_table [ { INCLUDING | EXCLUDING } { DEFAULTS | CONSTRAINTS | INDEXES } ] ... }
   */
  private List<ColumnData> parseColumns() {
    List<ColumnData> columns = new ArrayList<ColumnData>();
    if (accept(LIKE)) {
      parseLike();
    } else if (accept(CONSTRAINT)) {
      parseTableConstraint();
    } else {
      columns.add(parseColumn());
    }

    if (accept(COMMA)) {
      columns.addAll(parseColumns());
    }

    return columns;
  }

  private void parseLike() {
    // TODO - AHK
  }

  private void parseTableConstraint() {
    // TODO - AHK
  }


  private ColumnData parseColumn() {
    // column_name data_type [ DEFAULT default_expr ] [ column_constraint [ ... ] ]
    String name = parseColumnName();
    DBColumnTypeImpl type = parseDataType();
    parseDefault();
    parseColumnConstraint();

    return new ColumnData(name, type);
  }

  private String parseColumnName() {
    String name = consumeToken();
    return name;
  }

  private DBColumnTypeImpl parseDataType() {
    if (accept(bigint) || accept(int8)) {
//      return DBColumnTypeImpl.BIGINT;
      return null;
    } else if (accept(bigserial) || accept(serial8)) {
//      return DBColumnTypeImpl.BIGINT;
      return null;
    } else if (accept(bit)) {
      if (accept(varying)) {

      } else {

      }
    } else if (accept(varbit)) {

    } else if (accept(_boolean) || accept(bool)) {

    } else if (accept(box)) {

    } else if (accept(bytea)) {

    } else if (accept(character)) {
      if (accept(varying)) {
        Integer size = parseOptionalSize();
//        return DBColumnTypeImpl.VARCHAR;
        return null;
      } else {
        Integer size = parseOptionalSize();
//        return DBColumnTypeImpl.CHAR;
        return null;
      }
    } else if (accept(varchar)) {
      Integer size = parseOptionalSize();
//      return DBColumnTypeImpl.VARCHAR;
      return null;
    } else if (accept(_char)) {
      Integer size = parseOptionalSize();
//      return DBColumnTypeImpl.CHAR;
      return null;
    } else if (accept(cidr)) {

    } else if (accept(circle)) {

    } else if (accept(date)) {
//      return DBColumnTypeImpl.DATE;
      return null;
    } else if ((accept(_double) && accept(precision)) || accept(float8)) {
//      return DBColumnTypeImpl.DOUBLE;
      return null;
    } else if (accept(inet)) {

    } else if (accept(integer) || accept(_int) || accept(int4)) {
//      return DBColumnTypeImpl.INTEGER;
    } else if (accept(interval)) {
      parseIntervalFields();
      Integer precision = parseOptionalSize();
      // TODO - AHK - I have no idea what this data type is
//      return new DBColumnTypeImpl(_int, "java.lang.Integer");
    } else if (accept(line)) {

    } else if (accept(lseg)) {

    } else if (accept(macaddr)) {

    } else if (accept(money)) {

    } else if (accept(numeric) || accept(decimal)) {
//      return DBColumnTypeImpl.NUMERIC;
    } else if (accept(path)) {

    } else if (accept(point)) {

    } else if (accept(polygon)) {

    } else if (accept(real)) {

    } else if (accept(smallint)) {

    } else if (accept(serial)) {

    } else if (accept(text)) {

    } else if (accept(time)) {

    } else if (accept(timetz)) {

    } else if (accept(timestamp)) {

    } else if (accept(timestamptz)) {

    } else if (accept(tsquery)) {

    } else if (accept(tsvector)) {

    } else if (accept(txid_snapshot)) {

    } else if (accept(uuid)) {

    } else if (accept(xml)) {

    } else {
      // TODO - AHK - Error handling
      throw new IllegalStateException();
    }

    return null;
  }

  private Integer parseOptionalSize() {
    Integer size = null;
    if (accept(OPEN_PAREN)) {
      size = Integer.valueOf(consumeToken());
      expect(CLOSE_PAREN);
    }
    return size;
  }

  private int parseSize() {
    expect(OPEN_PAREN);
    int size = Integer.parseInt(consumeToken());
    expect(CLOSE_PAREN);
    return size;
  }

  private void parseIntervalFields() {
//    YEAR
//    MONTH
//    DAY
//    HOUR
//    MINUTE
//    SECOND
//    YEAR TO MONTH
//    DAY TO HOUR
//    DAY TO MINUTE
//    DAY TO SECOND
//    HOUR TO MINUTE
//    HOUR TO SECOND
//    MINUTE TO SECOND
    if (accept(YEAR)) {
      if (accept(TO)) {
        expect(MONTH);
      }
    } else if (accept(MONTH)) {

    } else if (accept(DAY)) {
      if (accept(TO)) {
        if (accept(HOUR)) {

        } else if (accept(MINUTE)) {

        } else if (accept(SECOND)) {

        } else {
          throw new IllegalStateException();
        }
      }
    } else if (accept(HOUR)) {
      if (accept(TO)) {
        if (accept(MINUTE)) {

        } else if (accept(SECOND)) {

        } else {
          throw new IllegalStateException();
        }
      }
    } else if (accept(MINUTE)) {
      if (accept(TO)) {
        expect(SECOND);
      }
    } else if (accept(SECOND)) {

    } else {
      throw new IllegalStateException();
    }
  }

  private void parseDefault() {
    // TODO - AHK
  }


  private void parseColumnConstraint() {
    /*[ CONSTRAINT constraint_name ]
      { NOT NULL |
        NULL |
        UNIQUE index_parameters |
        PRIMARY KEY index_parameters |
        CHECK ( expression ) |
        REFERENCES reftable [ ( refcolumn ) ] [ MATCH FULL | MATCH PARTIAL | MATCH SIMPLE ]
          [ ON DELETE action ] [ ON UPDATE action ] }
      [ DEFERRABLE | NOT DEFERRABLE ] [ INITIALLY DEFERRED | INITIALLY IMMEDIATE ]
      */
    parseConstraintName();
    parseConstraintContents();
    parseConstraintModifiers();
  }

  private void parseConstraintName() {
    if (accept(CONSTRAINT)) {
      String constraintName = consumeToken();
      LoggerFactory.getLogger("Tosa").trace("Constraint name is " + constraintName);
    }
  }

  private void parseConstraintContents() {
    if (accept(NOT) && accept(NULL)) {
      // TODO - AHK - Deal with two words being one logical token
      // Nothing else to do
    } else if (accept(NULL)) {
      // Nothing else to do
    } else if (accept(UNIQUE)) {
      parseIndexParameters();
    } else if (accept(PRIMARY) && accept(KEY)) {
      // TODO - AHK - Deal with two words being one logical token
      parseIndexParameters();
    } else if (accept(CHECK)) {
      expect(OPEN_PAREN);
      parseCheckExpression();
      expect(CLOSE_PAREN);
    } else if (accept(REFERENCES)) {
      parseReferencesConstraint();
    }
  }

  private void parseIndexParameters() {
    // TODO - AHK
  }

  private void parseConstraintModifiers() {
    // TODO - AHK
  }

  private void parseCheckExpression() {
    // TODO - AHK
  }

  private void parseReferencesConstraint() {
    // TODO - AHK
  }

  private boolean accept(String... potentialMatches) {
    return _tokenizer.acceptIgnoreCase(potentialMatches);
  }

  private void expect(String expected) {
    _tokenizer.expectIgnoreCase(expected);
  }

  private String consumeToken() {
    return _tokenizer.consumeToken();
  }
}
