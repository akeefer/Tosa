package tosa.api;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/11/11
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IQueryResultProcessor<T> {
  T processResult(ResultSet result) throws SQLException;
}
