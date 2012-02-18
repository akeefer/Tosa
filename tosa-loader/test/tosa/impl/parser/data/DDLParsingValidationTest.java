package tosa.impl.parser.data;

import org.junit.Ignore;
import org.junit.Test;
import tosa.TosaLoaderTestBase;
import tosa.loader.parser.DDLParser;
import tosa.loader.parser.Token;
import tosa.loader.parser.tree.CreateTableStatement;
import tosa.loader.parser.tree.SQLParseError;

import java.util.ArrayList;
import java.util.List;

public class DDLParsingValidationTest extends TosaLoaderTestBase {

  private List<SQLParseError> validate(String ddl) {
    DDLParser ddlParser = new DDLParser(Token.tokenize(ddl));
    List<CreateTableStatement> createTableStatements = ddlParser.parseDDL();
    ArrayList<SQLParseError> sqlParseErrors = new ArrayList<SQLParseError>();
    for (CreateTableStatement createTableStatement : createTableStatements) {
      sqlParseErrors.addAll(createTableStatement.getErrors());
    }
    return sqlParseErrors;
  }

  @Test
  public void testValidateDoesNotReportErrorsForValidTableDefinitions() {
    List<SQLParseError> validationResult = validate(
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
    assertEquals(0, validationResult.size());
  }

  @Test
  public void testValidateReportsErrorForBadStartCharacterInTableName() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"5Bar\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The table name 5Bar is not a valid type name.  The first character 5 is not a valid start to a type name.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForBadCharacterInTableName() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Ba^r\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The table name Ba^r is not a valid type name.  The character ^ is not a valid character in a type name.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForTableNameThatMimicsTransactionTypeName() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Transaction\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("Tosa tables cannot be named Transaction (in any case), as a built-in type named Transaction is automatically created for every ddl namespace.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForTableNameThatMimicsDatabaseTypeName() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Database\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("Tosa tables cannot be named Database (in any case), as a built-in type named Database is automatically created for every ddl namespace.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForTableNameThatConflictsWithGosuReservedWord() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"New\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The table name New conflicts with a Gosu reserved word.", validationResult.get(0).getMessage());
  }

  // TODO - AHK - No error for name that conflicts with SQL keyword, since it's in quotes . . . right?

  @Test
  public void testValidateReportsErrorForDuplicateTableNames() {
    List<SQLParseError> validationResult = validate(
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

    assertEquals(1, validationResult.size());
    assertEquals("Duplicate tables were found with the names \"Bar\" and \"Bar\".  Table names in Tosa must be case-insensitively unique within a database.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForDuplicateTableNamesWithDifferentCases() {
    List<SQLParseError> validationResult = validate(
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

    assertEquals(1, validationResult.size());
    assertEquals("Duplicate tables were found with the names \"bAR\" and \"Bar\".  Table names in Tosa must be case-insensitively unique within a database.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForTableWithoutIdColumn() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Bar\"(\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("No id column was found on the table Bar.  Every table in Tosa should have a column named \"id\" of type BIGINT.", validationResult.get(0).getMessage());
  }

  @Test
  @Ignore
  public void testValidateReportsWarningForTableNameThatsNotQuoted() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE Bar(\n" +
        "    \"Date\" DATE,\n" +
        "    \"Something\" VARCHAR(50)\n" +
        ");");
    assertEquals(1, validationResult.size());
    assertEquals("Table test.testdb.Bar: The table name was not quoted in the DDL file.  Tosa quotes table names in all SQL it generates, so we recommend that you quote the table names in the DDL files as well.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForIdColumnWithWrongCase() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"Id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The id column should always be named \"id\" but in this case it was named \"Id\"", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForIdColumnWithWrongDataType() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" INT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherDate\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The id column should always be a BIGINT, since it will be represented as a Long in Tosa.  The id column was found to be of type INT", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatDoesNotStartWithAValidCharacter() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"8Date\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The column name 8Date is not a valid property name.  The first character 8 is not a valid start to a property name.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatContainsAnInvalidCharacter() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"OtherD@te\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The column name OtherD@te is not a valid property name.  The character @ is not a valid character in a property name.", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatMatchesABuiltInPropertyName() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"_New\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The column name \"_New\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface", validationResult.get(0).getMessage());
  }

  @Test
  public void testValidateReportsErrorForColumnNameThatMatchesABuiltInPropertyNameInADifferentCase() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"_nEw\" DATE,\n" +
        "    \"OtherVarchar\" VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The column name \"_nEw\" conflicts with a built-in Tosa property or property inherited from the IDBObject interface", validationResult.get(0).getMessage());
  }

  @Test
  @Ignore
  public void testValidateReportsWarningForUnhandledDataType() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"MyTime\" TIME,\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The data type for this column is not currently handled.  It will appear in Tosa as a String, but it may not function correctly.", validationResult.get(0).getMessage());
  }

  @Test
  @Ignore
  public void testValidateReportsWarningForColumnNameThatsNotQuoted() {
    List<SQLParseError> validationResult = validate(
        "CREATE TABLE \"Foo\"(\n" +
        "    \"id\" BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
        "    \"MyDate\" DATE,\n" +
        "    OtherVarchar VARCHAR(50)\n" +
        ");");

    assertEquals(1, validationResult.size());
    assertEquals("The column name was not quoted in the DDL file.  Tosa will generate quoted column names in all SQL it generates, so column names should be specified quoted in the DDL as well.", validationResult.get(0).getMessage());
  }
}
