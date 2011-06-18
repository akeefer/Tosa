package tosa.api;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/12/11
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBJoinArray extends IDBArray {

  IDBTable getJoinTable();

  IDBColumn getSrcColumn();

  IDBColumn getTargetColumn();
}
