package tosa.impl.parser.data;

import tosa.dbmd.DBColumnImpl;
import tosa.dbmd.DBTableImpl;
import tosa.dbmd.DatabaseImpl;
import tosa.impl.md.ValidationResult;
import tosa.loader.data.ColumnData;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/29/11
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBDataValidator {

  // TODO - AHK - Fill out the list here
  private static final Set<String> GOSU_RESERVED_WORDS = new HashSet<String>(Arrays.asList(
      "new"
  ));

  // Names of built-in Tosa properties.  Stored as lower-case, since we'll compare the lower case
  // column name to this set
  private static final Set<String> SPECIAL_PROPERTY_NAMES = new HashSet<String>(Arrays.asList(
      "dbtable", "new", "_new", "Type"
  ));

  private static class Context {
    private ValidationResult _validationResult;
    private DBData _dbData;
    private TableData _tableData;
    private ColumnData _columnData;

    private Context(ValidationResult validationResult, DBData dbData) {
      _validationResult = validationResult;
      _dbData = dbData;
    }

    public void pushTable(TableData tableData) {
      _tableData = tableData;
    }

    public void popTable() {
      _tableData = null;
    }

    public void pushColumn(ColumnData columnData) {
      _columnData = columnData;
    }

    public void popColumn() {
      _columnData = null;
    }

    public void error(String msg) {
      _validationResult.addError(createContextString() + msg);
    }

    public void warn(String msg) {
      _validationResult.addWarning(createContextString() + msg);
    }

    private String createContextString() {
      if (_columnData != null) {
        return "Column " + _dbData.getNamespace() + "." + _tableData.getName() + "." + _columnData.getName() + ": ";
      } else if (_tableData != null) {
        return "Table " + _dbData.getNamespace() + "." + _tableData.getName() + ": ";
      } else {
        return "Database " + _dbData.getNamespace() + ": ";
      }
    }
  }

  public static ValidationResult validate(DBData database) {
    Context context = new Context(new ValidationResult(), database);
    DBDataValidator validator = new DBDataValidator();
    validator.validate(database, context);
    return context._validationResult;
  }

  private void validate(DBData database, Context context) {
    Map<String, String> tableNames = new HashMap<String, String>();
    for (TableData table : database.getTables()) {
      if (!tableNames.containsKey(table.getName().toLowerCase())) {
        tableNames.put(table.getName().toLowerCase(), table.getName());
      } else {
        context.error("Duplicate tables were found with the names " + table.getName() + " and " + tableNames.get(table.getName().toLowerCase()) + ".  Table names in Tosa must be case-insensitively unique within a database.");
        continue;
      }

      validate(table, context);
    }

    // TODO - Check for ANSI QUOTE MODE on MySQL
  }

  private void validate(TableData table, Context context) {
    context.pushTable(table);
    try {
      checkForValidTypeNameAsTableName(table, context);
      checkForGosuReservedWordAsTableName(table, context);
      checkForSpecialTypeNameAsTableName(table, context);
      checkTableNameIsQuoted(table, context);

      boolean hasIdColumn = false;
      Map<String, String> columnNames = new HashMap<String, String>();
      for (ColumnData column : table.getColumns()) {
        if (!columnNames.containsKey(column.getName().toLowerCase())) {
          columnNames.put(column.getName().toLowerCase(), column.getName());
        } else {
          context.error("Duplicate columns were found on the table " + table.getName() + ", with the names " + column.getName() + " and " + columnNames.get(column.getName().toLowerCase()) + ".  Column names in Tosa must be case-insensitively unique within a table.");
          continue;  // No point in validating the table if the name is wrong
        }

        validate(column, context);

        if (column.getName().equalsIgnoreCase("id")) {
          // Note that if the case is wrong, it will be caught by the column validation
          hasIdColumn = true;
        }
      }

      if (!hasIdColumn) {
        context.error("No id column was found on the table " + table.getName() + ".  Every table in Tosa should have a table named \"id\" of type BIGINT.");
      }
    } finally {
      context.popTable();
    }
  }

  private void checkForValidTypeNameAsTableName(TableData table, Context context) {
    if (!Character.isJavaIdentifierStart(table.getName().charAt(0))) {
      context.error("The table name " + table.getName() + " is not a valid type name.  The first character " + table.getName().charAt(0) + " is not a valid start to a type name.");
      return;
    }

    for (int i = 1; i < table.getName().length(); i++) {
      if (!Character.isJavaIdentifierPart(table.getName().charAt(i))) {
        context.error("The table name " + table.getName() + " is not a valid type name.  The character " + table.getName().charAt(i) + " is not a valid character in a type name.");
        return;
      }
    }
  }

  private void checkForGosuReservedWordAsTableName(TableData table, Context context) {
    if (GOSU_RESERVED_WORDS.contains(table.getName().toLowerCase())) {
      context.error("The table name " + table.getName() + " conflicts with a Gosu reserved word.");
    }
  }

  private void checkForSpecialTypeNameAsTableName(TableData table, Context context) {
    if (table.getName().toLowerCase().equals("transaction")) {
      context.error("Tosa tables cannot be named Transaction (in any case), as a built-in type named Transaction is automatically created for every ddl namespace.");
    } else if (table.getName().toLowerCase().equals("database")) {
      context.error("Tosa tables cannot be named Database (in any case), as a built-in type named Database is automatically created for every ddl namespace.");
    }
  }

  private void checkTableNameIsQuoted(TableData table, Context context) {
    if (table.getOriginalDefinition().getTableName().getValue().charAt(0) != '"') {
      context.warn("The table name was not quoted in the DDL file.  Tosa quotes table names in all SQL it generates, so we recommend that you quote the table names in the DDL files as well.");
    }
  }

  private void validate(ColumnData column, Context context) {
    context.pushColumn(column);
    try {
      // TODO - AHK
      checkForValidPropertyName(column, context);
//      checkForGosuReservedWordAsColumnName(column, context);
//      checkForDatabaseReservedNameAsColumnName(column, context);
      checkForBuiltInPropertyName(column, context);
      checkForUnhandledDataType(column, context);
      checkColumnNameIsQuoted(column, context);
      checkForValidIdColumn(column, context);
    } finally {
      context.popColumn();
    }
  }

  private void checkForValidPropertyName(ColumnData columnData, Context context) {
    if (!Character.isJavaIdentifierStart(columnData.getName().charAt(0))) {
      context.error("The column name " + columnData.getName() + " is not a valid property name.  The first character " + columnData.getName().charAt(0) + " is not a valid start to a property name.");
      return;
    }

    for (int i = 1; i < columnData.getName().length(); i++) {
      if (!Character.isJavaIdentifierPart(columnData.getName().charAt(i))) {
        context.error("The column name " + columnData.getName() + " is not a valid property name.  The character " + columnData.getName().charAt(i) + " is not a valid character in a property name.");
        return;
      }
    }
  }

  // TODO - AHK - Kill the _New property?

  private void checkForBuiltInPropertyName(ColumnData columnData, Context context) {
    if (SPECIAL_PROPERTY_NAMES.contains(columnData.getName().toLowerCase())) {
      context.error("The column name \"" + columnData.getName() + "\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface");
    }
  }

  private void checkForUnhandledDataType(ColumnData columnData, Context context) {
    // TODO - AHK - Some more reliable way to detect this besides just matching the String "PlaceHolder"
    if (columnData.getColumnType().getName().equals("PlaceHolder")) {
      context.warn("The data type for this column is not currently handled.  It will appear in Tosa as a String, but it may not function correctly.");
    }
  }

  private void checkColumnNameIsQuoted(ColumnData columnData, Context context) {
    if (columnData.getOriginalDefinition().getName().toString().charAt(0) != '"') {
      context.warn("The column name was not quoted in the DDL file.  Tosa will generate quoted column names in all SQL it generates, so column names should be specified quoted in the DDL as well.");
    }
  }

  private void checkForValidIdColumn(ColumnData columnData, Context context) {
    if (columnData.getName().equalsIgnoreCase("id")) {
      if (!columnData.getName().equals("id")) {
        context.error("The id column should always be named \"id\" but in this case it was named \"" + columnData.getName() + "\"");
      }

      if (!columnData.getColumnType().getGosuTypeName().equals("java.lang.Long")) {
        context.error("The id column should always be a BIGINT, since it will be represented as a Long in Tosa.  The id column was found to be of type " + columnData.getColumnType().getName());
      }

      // TODO - AHK - Check for auto-increment
    }
  }

}
