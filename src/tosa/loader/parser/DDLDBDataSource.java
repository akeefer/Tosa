package tosa.loader.parser;

import gw.fs.IFile;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import org.slf4j.LoggerFactory;
import tosa.impl.md.ValidationResult;
import tosa.impl.parser.data.DBDataValidator;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.data.TableData;
import tosa.loader.parser.mysql.MySQL51SQLParser;
import tosa.loader.parser.mysql.NewMySQL51Parser;

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
      // TODO - AHK - Select the correct parser somehow
      String path = module.pathRelativeToRoot(ddlFile.getSecond());
      IFile connectionFile = module.getFileRepository().findFirstFile(path.substring(0, path.length() - ".ddl".length()) + ".dbc");
      String connectionString = null;
      if (connectionFile != null && connectionFile.exists()) {
        connectionString = readFile(connectionFile);
      }
      List<TableData> tables = new NewMySQL51Parser().parseDDLFile(readFile(ddlFile.getSecond()));
      String fileName = ddlFile.getFirst();
      String namespace = fileName.substring(0, fileName.length() - ".ddl".length()).replace("/", ".");
      DBData dbData = new DBData(namespace, tables, connectionString, ddlFile.getSecond());
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
      LoggerFactory.getLogger("Tosa").error(error);
    }
    for (String warning : validationResult.getWarnings()) {
      LoggerFactory.getLogger("Tosa").warn(warning);
    }
  }
}
