package tosa.loader.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SQLParserConstants {

  class OPS {
    private static final List<String> OPS = new ArrayList<String>();
    public static List<String> get(){
      return OPS;
    }

    private static String reg(String op) {
      OPS.add(op);
      return op;
    }
  }

  public static final String CLOSE_PAREN = OPS.reg(")");
  public static final String COMMA = OPS.reg(",");
  public static final String EQUALS = OPS.reg("=");
  public static final String OPEN_PAREN = OPS.reg("(");
  public static final String SEMI_COLON = OPS.reg(";");
  public static final String ASTERISK = OPS.reg("*");
  public static final String EQ_OP = OPS.reg("=");
  public static final String LT_OP = OPS.reg("<");
  public static final String LTEQ_OP = OPS.reg("<=");
  public static final String GT_OP = OPS.reg(">");
  public static final String GTEQ_OP = OPS.reg(">=");
  public static final String PLUS_OP = OPS.reg("+");
  public static final String MINUS_OP = OPS.reg("-");
  public static final String TIMES_OP = OPS.reg("*");
  public static final String DIV_OP = OPS.reg("/");
  public static final String CONCAT_OP = OPS.reg("||");

  public static final String ACTION = "ACTION";
  public static final String ASC = "ASC";
  public static final String ASCII = "ASCII";
  public static final String AUTO_INCREMENT = "AUTO_INCREMENT";
  public static final String AVG_ROW_LENGTH = "AVG_ROW_LENGTH";
  public static final String BIGINT = "BIGINT";
  public static final String BINARY = "BINARY";
  public static final String BIT = "BIT";
  public static final String BLOB = "BLOB";
  public static final String BOOL = "BOOL";
  public static final String BOOLEAN = "BOOLEAN";
  public static final String BTREE = "BTREE";
  public static final String BY = "BY";
  public static final String BYTE = "BYTE";
  public static final String CASCADE = "CASCADE";
  public static final String CHAR = "CHAR";
  public static final String CHARACTER = "CHARACTER";
  public static final String CHECK = "CHECK";
  public static final String CHECKSUM = "CHECKSUM";
  public static final String COLLATE = "COLLATE";
  public static final String COLUMN_FORMAT = "COLUMN_FORMAT";
  public static final String COMMENT = "COMMENT";
  public static final String CONNECTION = "CONNECTION";
  public static final String CONSTRAINT = "CONSTRAINT";
  public static final String CREATE = "CREATE";
  public static final String DATA = "DATA";
  public static final String DATE = "DATE";
  public static final String DATETIME = "DATETIME";
  public static final String DEC = "DEC";
  public static final String DECIMAL = "DECIMAL";
  public static final String DEFAULT = "DEFAULT";
  public static final String DELAY_KEY_WRITE = "DELAY_KEY_WRITE";
  public static final String DELETE = "DELETE";
  public static final String DESC = "DESC";
  public static final String DICTIONARY = "DICTIONARY";
  public static final String DIRECTORY = "DIRECTORY";
  public static final String DISK = "DISK";
  public static final String DOUBLE = "DOUBLE";
  public static final String DYNAMIC = "DYNAMIC";
  public static final String ENGINE = "ENGINE";
  public static final String ENUM = "ENUM";
  public static final String EXISTS = "EXISTS";
  public static final String FIXED = "FIXED";
  public static final String FLOAT = "FLOAT";
  public static final String FOREIGN = "FOREIGN";
  public static final String FULL = "FULL";
  public static final String FULLTEXT = "FULLTEXT";
  public static final String GLOBAL = "GLOBAL";
  public static final String HASH = "HASH";
  public static final String IF = "IF";
  public static final String IN = "IN";
  public static final String INDEX = "INDEX";
  public static final String INSERT_METHOD = "INSERT_METHOD";
  public static final String INT = "INT";
  public static final String INTEGER = "INTEGER";
  public static final String KEY = "KEY";
  public static final String KEY_BLOCK_SIZE = "KEY_BLOCK_SIZE";
  public static final String LESS = "LESS";
  public static final String LIKE = "LIKE";
  public static final String LINEAR = "LINEAR";
  public static final String LIST = "LIST";
  public static final String LOCAL = "LOCAL";
  public static final String LONGBLOB = "LONGBLOB";
  public static final String LONGTEXT = "LONGTEXT";
  public static final String MATCH = "MATCH";
  public static final String MAX_ROWS = "MAX_ROWS";
  public static final String MAXVALUE = "MAXVALUE";
  public static final String MEDIUMBLOB = "MEDIUMBLOB";
  public static final String MEDIUMINT = "MEDIUMINT";
  public static final String MEDIUMTEXT = "MEDIUMTEXT";
  public static final String MEMORY = "MEMORY";
  public static final String MIN_ROWS = "MIN_ROWS";
  public static final String NATIONAL = "NATIONAL";
  public static final String NCHAR = "NCHAR";
  public static final String NODEGROUP = "NODEGROUP";
  public static final String NO = "NO";
  public static final String NOT = "NOT";
  public static final String NULL = "NULL";
  public static final String NUMERIC = "NUMERIC";
  public static final String ON = "ON";
  public static final String PACK_KEYS = "PACK_KEYS";
  public static final String PARSER = "PARSER";
  public static final String PARTIAL = "PARTIAL";
  public static final String PARTITION = "PARTITION";
  public static final String PARTITIONS = "PARTITIONS";
  public static final String PASSWORD = "PASSWORD";
  public static final String PRECISION = "PRECISION";
  public static final String PRIMARY = "PRIMARY";
  public static final String RANGE = "RANGE";
  public static final String REAL = "REAL";
  public static final String REFERENCES = "REFERENCES";
  public static final String RESTRICT = "RESTRICT";
  public static final String ROW_FORMAT = "ROW_FORMAT";
  public static final String SET = "SET";
  public static final String SIMPLE = "SIMPLE";
  public static final String SIGNED = "SIGNED";
  public static final String SMALLINT = "SMALLINT";
  public static final String SPATIAL = "SPATIAL";
  public static final String STORAGE = "STORAGE";
  public static final String SUBPARTITION = "SUBPARTITION";
  public static final String SUBPARTITIONS = "SUBPARTITIONS";
  public static final String TABLE = "TABLE";
  public static final String TABLESPACE = "TABLESPACE";
  public static final String TEMP = "TEMP";
  public static final String TEMPORARY = "TEMPORARY";
  public static final String TEXT = "TEXT";
  public static final String THAN = "THAN";
  public static final String TIME = "TIME";
  public static final String TIMESTAMP = "TIMESTAMP";
  public static final String TINYBLOB = "TINYBLOB";
  public static final String TINYINT = "TINYINT";
  public static final String TINYTEXT = "TINYTEXT";
  public static final String UNICODE = "UNICODE";
  public static final String UNION = "UNION";
  public static final String UNIQUE = "UNIQUE";
  public static final String UNSIGNED = "UNSIGNED";
  public static final String UPDATE = "UPDATE";
  public static final String USING = "USING";
  public static final String VALUES = "VALUES";
  public static final String VARBINARY = "VARBINARY";
  public static final String VARCHAR = "VARCHAR";
  public static final String WITH = "WITH";
  public static final String ZEROFILL = "ZEROFILL";
  public static final String IS = "IS";


  public static final String SELECT = "SELECT";

  public static final String bigint = "bigint";
  public static final String int8 = "int8";
  public static final String bigserial = "bigserial";
  public static final String serial8 = "serial8";
  public static final String bit = "bit";
  public static final String varying = "varying";
  public static final String varbit = "varbit";
  public static final String _boolean = "boolean";
  public static final String bool = "bool";
  public static final String box = "box";
  public static final String bytea = "bytea";
  public static final String character = "character";
  public static final String varchar = "varchar";
  public static final String _char = "char";
  public static final String cidr = "cidr";
  public static final String circle = "circle";
  public static final String date = "date";
  public static final String _double = "double";
  public static final String precision = "precision";
  public static final String float8 = "float8";
  public static final String inet = "inet";
  public static final String integer = "integer";
  public static final String _int = "int";
  public static final String int4 = "int4";
  public static final String interval = "interval";
  public static final String line = "line";
  public static final String lseg = "lseg";
  public static final String macaddr = "macaddr";
  public static final String money = "money";
  public static final String numeric = "numeric";
  public static final String decimal = "decimal";
  public static final String path = "path";
  public static final String point = "point";
  public static final String polygon = "polygon";
  public static final String real = "real";
  public static final String float4 = "float4";
  public static final String smallint = "smallint";
  public static final String int2 = "int2";
  public static final String serial = "serial";
  public static final String serial4 = "serial4";
  public static final String text = "text";
  public static final String time = "time";
  public static final String without = "without";
  public static final String zone = "zone";
  public static final String with = "with";
  public static final String timestamp = "timestamp";
  public static final String timetz = "timetz";
  public static final String timestamptz = "timestamptz";
  public static final String tsquery = "tsquery";
  public static final String tsvector = "tsvector";
  public static final String txid_snapshot = "txid_snapshot";
  public static final String uuid = "uuid";
  public static final String xml = "xml";
  public static final String YEAR = "YEAR";
  public static final String MONTH = "MONTH";
  public static final String DAY = "DAY";
  public static final String HOUR = "HOUR";
  public static final String MINUTE = "MINUTE";
  public static final String SECOND = "SECOND";
  public static final String TO = "TO";

  public static final String DISTINCT = "DISTINCT";
  public static final String ALL = "ALL";
  public static final String FROM = "FROM";
  public static final String WHERE = "WHERE";
  public static final String OR = "OR";
  public static final String AND = "AND";
  public static final String AS = "AS";
  public static final String BETWEEN = "BETWEEN";
  public static final String SYMMETRIC = "SYMMETRIC";
  public static final String ASYMMETRIC = "ASYMMETRIC";
  public static final String JOIN = "JOIN";
  public static final String INNER = "INNER";
  public static final String LEFT = "LEFT";
  public static final String RIGHT = "RIGHT";
  public static final String OUTER = "OUTER";
  public static final String ORDER = "ORDER";
  public static final String GROUP = "GROUP";

  public static final String COUNT = "COUNT";
  public static final String AVG = "AVG";
  public static final String MAX = "MAX";
  public static final String MIN = "MIN";
  public static final String SUM = "SUM";
  public static final String MOD = "MOD";
  public static final String ABS = "ABS";

  public static final String SOME = "SOME";

  public static final String TRUE = "TRUE";
  public static final String FALSE = "FALSE";

  public static final String UPPER = "UPPER";
  public static final String LOWER = "LOWER";

  public static final String OPTIONAL = "OPTIONAL";
}
