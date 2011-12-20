package tosa.impl.md;

import tosa.dbmd.DBColumnImpl;
import tosa.dbmd.DBTableImpl;
import tosa.dbmd.DatabaseImpl;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/28/11
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseImplValidator {

  public ValidationResult validate(DatabaseImpl database) {
    // TODO - AHK - Duplicate type names


    return null;
  }

  private void validate(DBTableImpl table, ValidationResult validationResult) {
    // TODO - AHK - Duplicate columns
    // TODO - AHK - id column with proper type
    // TODO - AHK - Table names have to be valid type names
    // TODO - AHK - Conflicts of table names with reserved words
  }


  private void validate(DBColumnImpl column, ValidationResult validationResult) {
    // TODO - AHK - Column/fk naming conventions
    // TODO - AHK - Column names need to be valid property names
    // TODO - AHK - Conflicts with reserved words in Gosu
    // TODO - AHK - Conflicts with reserved words in other databases
    // TODO - AHK - Conflicts with built-in property names
    // TODO - AHK - Types that aren't handled properly yet
  }
}
