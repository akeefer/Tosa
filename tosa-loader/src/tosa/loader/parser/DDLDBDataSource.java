package tosa.loader.parser;

import gw.fs.IFile;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import tosa.impl.md.ValidationResult;
import tosa.impl.parser.data.DBDataValidator;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.data.DDLDataTransformer;
import tosa.loader.data.TableData;
import tosa.loader.parser.tree.CreateTableStatement;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DBData source that determines the structure of the tables by parsing the DDL statements used to create the tables.
 *
 * ${Copyright}
 */
public class DDLDBDataSource implements IDBDataSource {
  @Override
  public Map<String, DBData> getDBData(IModule module) {
    Map<String, DBData> results = new HashMap<String, DBData>();
    for (Pair<String, IFile> ddlFile : module.getFileRepository().findAllFilesByExtension(".ddl")) {
      // TODO - AHK - Lots o' error handling
      String path = module.pathRelativeToRoot(ddlFile.getSecond());
      String source = readFile(ddlFile.getSecond());
      Token token = Token.tokenize(source);
      List<CreateTableStatement> createTableStatements = new DDLParser(token).parseDDL();
      List<TableData> tables = new DDLDataTransformer().transformParseTree(createTableStatements);
      String fileName = ddlFile.getFirst();
      String namespace = fileName.substring(0, fileName.length() - ".ddl".length()).replace("/", ".");
      DBData dbData = new DBData(namespace, tables, ddlFile.getSecond());
      validateAndLogResults(dbData);
      results.put(namespace, dbData);
    }
    return results;
  }

  private String readFile(IFile file) {
    try {
      return readFileWithoutHandlingExceptions(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readFileWithoutHandlingExceptions(IFile file) throws IOException {
    byte[] result = new byte[0];
    InputStream inputStream = file.openInputStream();
    try {
      byte[] buffer = new byte[4196];
      while (true) {
        int numRead = inputStream.read(buffer);
        if (numRead != -1) {
          byte[] newResult = new byte[result.length + numRead];
          System.arraycopy(result, 0, newResult, 0, result.length);
          System.arraycopy(buffer, 0, newResult, result.length, numRead);
          result = newResult;
        } else {
          break;
        }
      }
    } finally {
      inputStream.close();
    }
    // TODO - AHK - Verify the charset somehow?
    return new String(result);
  }

  private void validateAndLogResults(DBData dbData) {
    ValidationResult validationResult = DBDataValidator.validate(dbData);
    for (String error : validationResult.getErrors()) {
//      LoggerFactory.getLogger("Tosa").error(error);
    }
    for (String warning : validationResult.getWarnings()) {
//      LoggerFactory.getLogger("Tosa").warn(warning);
    }
  }
}
