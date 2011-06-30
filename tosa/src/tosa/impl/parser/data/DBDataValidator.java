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

  // TODO - AHK - If perf here is ever an issue, there's plenty of room to combine validations into a single pass

  // TODO - AHK - Fill out the list here
  private static final Set<String> GOSU_RESERVED_WORDS = new HashSet<String>(Arrays.asList(
      "new"
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

        if (column.getName().equals("id")) {
          hasIdColumn = true;
        }
      }

      if (!hasIdColumn) {
        // TODO - AHK - Is this always an error?
      }
    } finally {
      context.popTable();
    }

    // TODO - AHK - Duplicate columns
    // TODO - AHK - id column with proper type
    // TODO - AHK - Table names have to be valid type names
    // TODO - AHK - Conflicts of table names with reserved words
    // TODO - AHK - Quoting of table names
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
    }
  }

  private void validate(ColumnData column, Context context) {
    context.pushColumn(column);
    try {

    } finally {
      context.popColumn();
    }
    // TODO - AHK - Column/fk naming conventions
    // TODO - AHK - Column names need to be valid property names
    // TODO - AHK - Conflicts with reserved words in Gosu
    // TODO - AHK - Conflicts with reserved words in other databases
    // TODO - AHK - Conflicts with built-in property names
    // TODO - AHK - Types that aren't handled properly yet
    // TODO - AHK - Quoting of column names
    // TODO - AHK - Validate the various properties of id columns
  }

}
