package tosa.loader.parser;

import tosa.loader.data.DBData;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISQLParser {
  DBData parseDDLFile(String fileContents);
}
