package tosa.api;


/**
 * Interface representing metadata about a column in the database.
 *
 * ${License}
 */
public interface IDBColumn {

  /**
   * Returns the IDBTable that this column belongs to.
   *
   * @return the IDBTable this column belongs to.
   */
  IDBTable getTable();

  /**
   * Returns the name of this column.
   *
   * @return the name of this column
   */
  String getName();

  /**
   * Indicates whether or not this column represents a foreign key to another table.
   *
   * @return true if this column is an fk to another column, false otherwise
   */
  boolean isFK();

  /**
   * Returns the IDBTable that this column is a foreign key to, if any.
   *
   * @return the foreign table, if this is an fk column, and null if this is not an fk
   */
  IDBTable getFKTarget();

  /**
   * Returns the type of this column in the database.
   *
   * @return the column type in the database
   */
  IDBColumnType getColumnType();

}
