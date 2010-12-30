package tosa.loader.data;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ForeignKeyData {
  private final String _name;
  private final String _otherTable;

  public ForeignKeyData(String name, String otherTable) {
    _name = name;
    _otherTable = otherTable;
  }
}
