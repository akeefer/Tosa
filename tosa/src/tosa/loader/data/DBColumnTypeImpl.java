package tosa.loader.data;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import tosa.api.IDBColumnType;
import tosa.loader.data.types.GenericDBColumnTypePersistenceHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBColumnTypeImpl implements IDBColumnType {
  public static final String BIG_DECIMAL_ITYPE = "java.math.BigDecimal";
  public static final String BOOLEAN_ITYPE = "java.lang.Boolean";
  public static final String BYTE_ITYPE = "java.lang.Byte";
  public static final String DATE_ITYPE = "java.util.Date";
  public static final String DOUBLE_ITYPE = "java.lang.Double";
  public static final String FLOAT_ITYPE = "java.lang.Float";
  public static final String INTEGER_ITYPE = "java.lang.Integer";
  public static final String LONG_ITYPE = "java.lang.Long";
  public static final String SHORT_ITYPE = "java.lang.Short";
  public static final String STRING_ITYPE = "java.lang.String";

  private String _name;
  private String _description;
  private String _gosuTypeName;
  private int _jdbcType;
  private IDBColumnTypePersistenceHandler _persistenceHandler;

  public DBColumnTypeImpl(String name, String description, String gosuTypeName, int jdbcType) {
    _name = name;
    _description = description;
    _gosuTypeName = gosuTypeName;
    _jdbcType = jdbcType;
    _persistenceHandler = new GenericDBColumnTypePersistenceHandler(jdbcType);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getDescription() {
    return _description;
  }

  @Override
  public String getGosuTypeName() {
    return _gosuTypeName;
  }

  @Override
  public IType getGosuType() {
    // TODO - AHK - We might want to consider caching this
    return TypeSystem.getByFullName(_gosuTypeName);
  }

  @Override
  public int getJdbcType() {
    return _jdbcType;
  }

  @Override
  public Object readFromResultSet(ResultSet resultSet, String name) throws SQLException {
    return _persistenceHandler.readFromResultSet(resultSet, name);
  }

  @Override
  public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
    _persistenceHandler.setParameter(statement, index, value);
  }

  /*switch(sqlType) {
      case Types.BIT:
        return IJavaType.BOOLEAN;

      case Types.TINYINT:
        return IJavaType.BYTE;

      case Types.SMALLINT:
        return IJavaType.SHORT;

      case Types.INTEGER:
        return IJavaType.INTEGER;

      case Types.BIGINT:
        return IJavaType.LONG;

      case Types.FLOAT:
        return IJavaType.DOUBLE;

      case Types.REAL:
        return IJavaType.FLOAT;

      case Types.DOUBLE:
        return IJavaType.DOUBLE;

      case Types.NUMERIC:
        return IJavaType.BIGDECIMAL;

      case Types.DECIMAL:
        return IJavaType.BIGDECIMAL;

      case Types.CHAR:
        return IJavaType.STRING;

      case Types.VARCHAR:
        return IJavaType.STRING;

      case Types.LONGVARCHAR:
        return IJavaType.STRING;

      case Types.BOOLEAN:
        return IJavaType.BOOLEAN;

      case Types.DATE:
        return TypeSystem.get(java.sql.Date.class);

      case Types.TIME:
        return TypeSystem.get(java.sql.Time.class);

      case Types.TIMESTAMP:
        return TypeSystem.get(java.sql.Timestamp.class);

      case Types.BINARY:
        return IJavaType.pBYTE.getArrayType();

      case Types.VARBINARY:
        return IJavaType.pBYTE.getArrayType();

      case Types.LONGVARBINARY:
        return IJavaType.pBYTE.getArrayType();

      case Types.NULL:
        return IJavaType.pVOID;

      case Types.OTHER:
        return IJavaType.OBJECT;

      case Types.JAVA_OBJECT:
        return IJavaType.OBJECT;

      case Types.DISTINCT:
        return IJavaType.OBJECT;

      case Types.STRUCT:
        return IJavaType.OBJECT;

      case Types.ARRAY:
        return IJavaType.OBJECT.getArrayType();

      case Types.BLOB:
        return IJavaType.OBJECT;

      case Types.CLOB:
        return IJavaType.STRING;

      case Types.REF:
        return IJavaType.OBJECT;

      case Types.DATALINK:
        return IJavaType.OBJECT;

    }
    return IJavaType.OBJECT;
  }*/
}
