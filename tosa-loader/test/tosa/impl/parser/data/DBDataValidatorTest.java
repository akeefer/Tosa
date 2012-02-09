package tosa.impl.parser.data;

import org.junit.Test;
import tosa.TosaLoaderTestBase;
import tosa.impl.md.ValidationResult;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.mysql.MySQL51Parser;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/29/11
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBDataValidatorTest extends TosaLoaderTestBase {

  private DBData parse(String ddl) {
    List<TableData> tables = new MySQL51Parser().parseDDLFile(ddl);
    return new DBData("test.testdb", tables, null);
  }

  private ValidationResult validate(String ddl) {
    return DBDataValidator.validate(parse(ddl));
  }

  @Test
  public void testValidateDoesNotReportErrorsForValidTableDefinitions() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");\n" +
        "CREATE TABLE \"Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");
    assertEquals(0, validationResult.getErrors().size());
  }

  @Test
  public void testValidateReportsErrorForBadStartCharacterInTableName() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"5Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.5Bar: The table name 5Bar is not a valid type name.  The first character 5 is not a valid start to a type name.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForBadCharacterInTableName() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Ba^r\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.Ba^r: The table name Ba^r is not a valid type name.  The character ^ is not a valid character in a type name.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForTableNameThatMimicsTransactionTypeName() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Transaction\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.Transaction: Tosa tables cannot be named Transaction (in any case), as a built-in type named Transaction is automatically created for every ddl namespace.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForTableNameThatMimicsDatabaseTypeName() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Database\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.Database: Tosa tables cannot be named Database (in any case), as a built-in type named Database is automatically created for every ddl namespace.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForTableNameThatConflictsWithGosuReservedWord() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"New\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.New: The table name New conflicts with a Gosu reserved word.", validationResult.getErrors().get(0));
  }

  // TODO - AHK - No error for name that conflicts with SQL keyword, since it's in quotes . . . right?

  @Test
  public void testValidateReportsErrorForDuplicateTableNames() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");\n" +
        "CREATE TABLE \"Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Database test.testdb: Duplicate tables were found with the names Bar and Bar.  Table names in Tosa must be case-insensitively unique within a database.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForDuplicateTableNamesWithDifferentCases() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");\n" +
        "CREATE TABLE \"bAR\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Database test.testdb: Duplicate tables were found with the names bAR and Bar.  Table names in Tosa must be case-insensitively unique within a database.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForTableWithoutIdColumn() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Bar\"(\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Table test.testdb.Bar: No id column was found on the table Bar.  Every table in Tosa should have a table named \"id\" of type BIGINT.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsWarningForTableNameThatsNotQuoted() {
    ValidationResult validationResult = validate(
        "CREATE TABLE Bar(\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");
    assertEquals(1, validationResult.getWarnings().size());
    assertEquals("Table test.testdb.Bar: The table name was not quoted in the DDL file.  Tosa quotes table names in all SQL it generates, so we recommend that you quote the table names in the DDL files as well.", validationResult.getWarnings().get(0));
  }

  @Test
  public void testValidateReportsErrorForIdColumnWithWrongCase() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"Id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo.Id: The id column should always be named \"id\" but in this case it was named \"Id\"", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForIdColumnWithWrongDataType() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" INT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo.id: The id column should always be a BIGINT, since it will be represented as a Long in Tosa.  The id column was found to be of type INT", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatDoesNotStartWithAValidCharacter() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"8Date\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo.8Date: The column name 8Date is not a valid property name.  The first character 8 is not a valid start to a property name.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatContainsAnInvalidCharacter() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherD@te\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo.OtherD@te: The column name OtherD@te is not a valid property name.  The character @ is not a valid character in a property name.", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatMatchesABuiltInPropertyName() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"_New\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo._New: The column name \"_New\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatMatchesABuiltInPropertyNameInADifferentCase() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"_nEw\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getErrors().size());
    assertEquals("Column test.testdb.Foo._nEw: The column name \"_nEw\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface", validationResult.getErrors().get(0));
  }

  @Test
  public void testValidateReportsWarningForUnhandledDataType() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"MyTime\" TIME,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getWarnings().size());
    assertEquals("Column test.testdb.Foo.MyTime: The data type for this column is not currently handled.  It will appear in Tosa as a String, but it may not function correctly.", validationResult.getWarnings().get(0));
  }

  @Test
  public void testValidateReportsWarningForColumnNameThatsNotQuoted() {
    ValidationResult validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"MyDate\" DATE,\n" +
        "    OtherVarchar VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.getWarnings().size());
    assertEquals("Column test.testdb.Foo.OtherVarchar: The column name was not quoted in the DDL file.  Tosa will generate quoted column names in all SQL it generates, so column names should be specified quoted in the DDL as well.", validationResult.getWarnings().get(0));
  }
}
