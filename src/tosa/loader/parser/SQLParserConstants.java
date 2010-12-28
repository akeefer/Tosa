package tosa.loader.parser;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/27/10
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SQLParserConstants {

  public static final String CLOSE_PAREN = ")";
  public static final String COMMA = ",";
  public static final String OPEN_PAREN = "(";
  public static final String SEMI_COLON = ";";

  /*ENGINE [=] engine_name
  | AUTO_INCREMENT [=] value
  | AVG_ROW_LENGTH [=] value
  | [DEFAULT] CHARACTER SET [=] charset_name
  | CHECKSUM [=] {0 | 1}
  | [DEFAULT] COLLATE [=] collation_name
  | COMMENT [=] 'string'
  | CONNECTION [=] 'connect_string'
  | DATA DIRECTORY [=] 'absolute path to directory'
  | DELAY_KEY_WRITE [=] {0 | 1}
  | INDEX DIRECTORY [=] 'absolute path to directory'
  | INSERT_METHOD [=] { NO | FIRST | LAST }
  | KEY_BLOCK_SIZE [=] value
  | MAX_ROWS [=] value
  | MIN_ROWS [=] value
  | PACK_KEYS [=] {0 | 1 | DEFAULT}
  | PASSWORD [=] 'string'
  | ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
  | TABLESPACE tablespace_name [STORAGE {*/

  public static final String AUTO_INCREMENT = "AUTO_INCREMENT";
  public static final String AVG_ROW_LENGTH = "AVG_ROW_LENGTH";
  public static final String CHARACTER = "CHARACTER";
  public static final String CHECK = "CHECK";
  public static final String CHECKSUM = "CHECKSUM";
  public static final String COLLATE = "COLLATE";
  public static final String COMMENT = "COMMENT";
  public static final String CONNECTION = "CONNECTION";
  public static final String CONSTRAINT = "CONSTRAINT";
  public static final String CREATE = "CREATE";
  public static final String DATA = "DATA";
  public static final String DEFAULT = "DEFAULT";
  public static final String DELAY_KEY_WRITE = "DELAY_KEY_WRITE";
  public static final String DICTIONARY = "DICTIONARY";
  public static final String DIRECTORY = "DIRECTORY";
  public static final String ENGINE = "ENGINE";
  public static final String EXISTS = "EXISTS";
  public static final String GLOBAL = "GLOBAL";
  public static final String IF = "IF";
  public static final String INDEX = "INDEX";
  public static final String INSERT_METHOD = "INSERT_METHOD";
  public static final String KEY = "KEY";
  public static final String KEY_BLOCK_SIZE = "KEY_BLOCK_SIZE";
  public static final String LIKE = "LIKE";
  public static final String LOCAL = "LOCAL";
  public static final String MAX_ROWS = "MAX_ROWS";
  public static final String MIN_ROWS = "MIN_ROWS";
  public static final String NULL = "NULL";
  public static final String NOT = "NOT";
  public static final String PACK_KEYS = "PACK_KEYS";
  public static final String PASSWORD = "PASSWORD";
  public static final String PRIMARY = "PRIMARY";
  public static final String REFERENCES = "REFERENCES";
  public static final String ROW_FORMAT = "ROW_FORMAT";
  public static final String SET = "SET";
  public static final String TABLE = "TABLE";
  public static final String TABLESPACE = "TABLESPACE";
  public static final String TEMP = "TEMP";
  public static final String TEMPORARY = "TEMPORARY";
  public static final String UNIQUE = "UNIQUE";

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
}
