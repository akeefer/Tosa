package tosa.loader.data;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/26/10
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnType {
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

  //  ARRAY(Types.ARRAY, TODO),
  public static final ColumnType BIGINT = new ColumnType(Types.BIGINT, "java.lang.Long");
  public static final ColumnType BINARY = new ColumnType(Types.BINARY, "byte[]");
  public static final ColumnType BIT = new ColumnType(Types.BIT, BOOLEAN_ITYPE);
  public static final ColumnType BLOB = new ColumnType(Types.BLOB, "java.lang.Object");
  public static final ColumnType BOOLEAN = new ColumnType(Types.BOOLEAN, BOOLEAN_ITYPE);
  public static final ColumnType CHAR = new ColumnType(Types.CHAR, STRING_ITYPE);
  public static final ColumnType CLOB = new ColumnType(Types.CLOB, STRING_ITYPE);
//  DATALINK(),
  public static final ColumnType DATE = new ColumnType(Types.DATE, "java.sql.Date");
  public static final ColumnType DECIMAL = new ColumnType(Types.DECIMAL, "java.math.BigDecimal");
//  DISTINCT(),
  public static final ColumnType DOUBLE = new ColumnType(Types.DOUBLE, "java.lang.Double");
  public static final ColumnType FLOAT = new ColumnType(Types.FLOAT, "java.lang.Double"); // TODO - AHK - Is that correct?
  public static final ColumnType INTEGER = new ColumnType(Types.INTEGER, "java.lang.Integer");
//  JAVA_OBJECT(),
//  LONGNVARCHAR(Types.LONGNVARCHAR, TODO),
//  LONGNBINARY(Types.LONGNBINARY, TODO),
//  LONGVARCHAR(Types.LONGVARCHAR, TODO),
//  NCHAR(Types.NCHAR, TODO),
//  NCLOB(Types.NCLOB, TODO),
  public static final ColumnType NULL = new ColumnType(Types.NULL, "void");
  public static final ColumnType NUMERIC = new ColumnType(Types.NUMERIC, "java.math.BigDecimal");
//  NVARCHAR(Types.NVARCHAR, TODO),
//  OTHER(Types.OTHER, TODO),
  public static final ColumnType REAL = new ColumnType(Types.REAL, "java.lang.Float");
//  REF(Types.REF, TODO),
//  ROWID(Types.ROWID, TODO),
  public static final ColumnType SMALLINT = new ColumnType(Types.SMALLINT, "java.lang.Short");
//  SQLXML(),
//  STRUCT(),
  public static final ColumnType TIME = new ColumnType(Types.TIME, "java.sql.Time");
  public static final ColumnType TIMESTAMP = new ColumnType(Types.TIMESTAMP, "java.sql.Timestamp");
  public static final ColumnType TINYINT = new ColumnType(Types.TINYINT, "java.lang.Byte");
  public static final ColumnType VARBINARY = new ColumnType(Types.VARBINARY, "byte[]");
  public static final ColumnType VARCHAR = new ColumnType(Types.VARCHAR, STRING_ITYPE);

  private final int _jdbcTypeNumber;
  private final String _gosuTypeName;

  public ColumnType(int jdbcTypeNumber, String gosuTypeName) {
    _jdbcTypeNumber = jdbcTypeNumber;
    _gosuTypeName = gosuTypeName;
  }

  public ColumnType(int jdbcTypeNumber, String dbTypeName, String gosuTypeName) {
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
