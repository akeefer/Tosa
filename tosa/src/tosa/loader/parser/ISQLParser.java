package tosa.loader.parser;

import tosa.loader.data.DBData;
import tosa.loader.query.SQLQuery;
import tosa.loader.data.TableData;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISQLParser {

  DBData parseDDLFile(String fileContents);

  SQLQuery parseQuery(String fileContents);

  List<TableData> parseDDLFile(String fileContents);
}
