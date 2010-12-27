package tosa.loader.data;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ColumnType {
//  ARRAY(Types.ARRAY, TODO),
  BIGINT(Types.BIGINT, "java.lang.Long"),
  BINARY(Types.BINARY, "java.lang.Byte[]"),
  BIT(Types.BIT, "java.lang.Boolean"),
  BLOB(Types.BLOB, "java.lang.Object"),
  BOOLEAN(Types.BOOLEAN, "java.lang.Boolean"),
  CHAR(Types.CHAR, "java.lang.String"),
  CLOB(Types.CLOB, "java.lang.String"),
//  DATALINK(),
  DATE(Types.DATE, "java.sql.Date"),
  DECIMAL(Types.DECIMAL, "java.math.BigDecimal"),
//  DISTINCT(),
  DOUBLE(Types.DOUBLE, "java.lang.Double"),
  FLOAT(Types.FLOAT, "java.lang.Double"), // TODO - AHK - Is that correct?
  INTEGER(Types.INTEGER, "java.lang.Integer"),
//  JAVA_OBJECT(),
//  LONGNVARCHAR(Types.LONGNVARCHAR, TODO),
//  LONGNBINARY(Types.LONGNBINARY, TODO),
//  LONGVARCHAR(Types.LONGVARCHAR, TODO),
//  NCHAR(Types.NCHAR, TODO),
//  NCLOB(Types.NCLOB, TODO),
  NULL(Types.NULL, "void"),
  NUMERIC(Types.NUMERIC, "java.math.BigDecimal"),
//  NVARCHAR(Types.NVARCHAR, TODO),
//  OTHER(Types.OTHER, TODO),
  REAL(Types.REAL, "java.lang.Float"),
//  REF(Types.REF, TODO),
//  ROWID(Types.ROWID, TODO),
  SMALLINT(Types.SMALLINT, "java.lang.Short"),
//  SQLXML(),
//  STRUCT(),
  TIME(Types.TIME, "java.sql.Time"),
  TIMESTAMP(Types.TIMESTAMP, "java.sql.Timestamp"),
  TINYINT(Types.TINYINT, "java.lang.Byte"),
  VARBINARY(Types.VARBINARY, "java.lang.Byte[]"),
  VARCHAR(Types.VARCHAR, "java.lang.String");

  private int _jdbcTypeNumber;
  private String _gosuTypeName;

  private ColumnType(int jdbcTypeNumber, String gosuTypeName) {
    _jdbcTypeNumber = jdbcTypeNumber;
    _gosuTypeName = gosuTypeName;
  }

  public int getJdbcTypeNumber() {
    return _jdbcTypeNumber;
  }

  public String getGosuTypeName() {
    return _gosuTypeName;
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
