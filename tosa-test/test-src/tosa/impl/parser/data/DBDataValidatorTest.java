package tosa.impl.parser.data;

import org.junit.Test;
import tosa.impl.md.ValidationResult;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.mysql.NewMySQL51Parser;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/29/11
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBDataValidatorTest {

  private DBData parse(String ddl) {
    List<TableData> tables = new NewMySQL51Parser().parseDDLFile(ddl);
    return new DBData("test.testdb", tables, "jdbc:h2:mem:testdb", null);
  }

  private ValidationResult validate(String ddl) {
    return DBDataValidator.validate(parse(ddl));
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
}
