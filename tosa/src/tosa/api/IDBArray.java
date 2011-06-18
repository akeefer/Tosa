package tosa.api;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/13/11
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBArray {

  String getPropertyName();

  IDBTable getOwnerTable();

  IDBTable getTargetTable();
}
