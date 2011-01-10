package tosa.api;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/9/11
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBColumn {

  IDBTable getTable();

  String getName();

  // TODO - Column type
  // TODO - Additional attributes
}
