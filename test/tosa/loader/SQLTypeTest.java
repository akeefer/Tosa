package tosa.loader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.ResourcePath;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import test.TestEnv;
import tosa.api.IDBConnection;
import tosa.api.IDBObject;
import tosa.dbmd.DatabaseImpl;
import tosa.impl.md.DatabaseImplSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

@Ignore
public class SQLTypeTest {

  @BeforeClass
  static public void resetDB() {
    TestEnv.init();
  }

  @Before
  public void clearDB() {
    getDB().getDBUpgrader().recreateTables();
  }

  @Test
  public void testSimpleSelect() {
    insertBar("2010-10-10", "Foo");

    SQLType sType = parse("SELECT * FROM Bar");
    Iterator<Map> rs = executeQuery(Map.class, sType).iterator();
    assertTrue(rs.hasNext());
    Map obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("Date"));
    assertEquals("Foo", obj.get("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithTwoResults() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-10", "Bar");

    SQLType sType = parse("SELECT * FROM Bar");
    Iterator<IDBObject> rs = executeQuery(IDBObject.class, sType).iterator();
    assertTrue(rs.hasNext());
    rs.next();
    assertTrue(rs.hasNext());
    rs.next();
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithCondition() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date < '2011-1-1'");
    Iterator<Map> rs = executeQuery(Map.class, sType).iterator();
    assertTrue(rs.hasNext());
    Map obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("Date"));
    assertEquals("Foo", obj.get("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date < :foo");
    Iterator<Map> rs = executeQuery(Map.class, sType, "2011-1-1").iterator();
    assertTrue(rs.hasNext());
    Map obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("Date"));
    assertEquals("Foo", obj.get("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithTwoVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date < :foo AND Misc LIKE :bar");
    Iterator<Map> rs = executeQuery(Map.class, sType, "2011-1-1", "Foo").iterator();
    assertTrue(rs.hasNext());
    Map obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("Date"));
    assertEquals("Foo", obj.get("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithIsNotNull() {
    insertBar("2010-10-10", "Foo");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date IS NOT NULL");
    Iterator<Map> rs = executeQuery(Map.class, sType).iterator();
    assertTrue(rs.hasNext());
    rs.next();
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithIsNull() {
    insertBar("2010-10-10", "Foo");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date IS NULL");
    Iterator<Map> rs = executeQuery(Map.class, sType).iterator();
    assertFalse(rs.hasNext());
  }

  //===================================================================
  // HELPER STUFF
  //===================================================================

  static private void insertBar(String date, String misc) {
    try {
      IDBConnection dbConn = getDB().getConnection();
      dbConn.startTransaction();
      Connection rawConnection = dbConn.connect();
      PreparedStatement stmt = rawConnection.prepareStatement("INSERT INTO \"Bar\" (\"Date\", \"Misc\") VALUES (?, ?)");
      stmt.setObject(1, date);
      stmt.setObject(2, misc);
      stmt.execute();
      dbConn.commitTransaction();
      dbConn.endTransaction();
      rawConnection.close();
    } catch (SQLException e) {
      GosuExceptionUtil.forceThrow(e);
    }
  }

  private static DatabaseImpl getDB() {
    return (DatabaseImpl) DatabaseImplSource.getInstance().getDatabase("test.testdb");
  }


  private <T> Iterable<T> executeQuery(Class<T> clz, SQLType sType, Object... args) {
    return (Iterable<T>) sType.getTypeInfo().invokeQuery( JavaTypes.MAP().getGenericType().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), args);
  }

  private SQLType parse(String sql) {
    return new SQLType(new SQLFileInfo("test.Test", getDB(), new StringFile(sql)), TypeSystem.getTypeLoader(DBTypeLoader.class));
  }

  private Object slqDate(String s) {
    try {
      return new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(s).getTime());
    } catch (ParseException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private static class StringFile implements IFile {
    private String _content;

    public StringFile(String content) {
      _content = content;
    }

    @Override
    public InputStream openInputStream() throws IOException {
      return new ByteArrayInputStream(_content.getBytes());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream openOutputStreamForAppend() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getExtension() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getBaseName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public IDirectory getParent() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
      return "String : " + _content;
    }

    @Override
    public boolean exists() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI toURI() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ResourcePath getPath() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChildOf(IDirectory iDirectory) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDescendantOf(IDirectory iDirectory) {
      throw new UnsupportedOperationException();
    }

    @Override
    public File toJavaFile() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isJavaFile() {
      throw new UnsupportedOperationException();
    }
  }
}
