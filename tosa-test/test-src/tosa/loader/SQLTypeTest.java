package tosa.loader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.ResourcePath;
import gw.lang.reflect.TypeSystem;
import gw.util.GosuExceptionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import test.TestEnv;
import tosa.api.IDBConnection;
import tosa.api.IDBObject;
import tosa.dbmd.DatabaseImpl;

import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import static org.junit.Assert.*;

public class SQLTypeTest {

  @BeforeClass
  static public void resetDB() {
    TestEnv.maybeInit();
  }

  @Before
  public void clearDB() {
    getDB().getDBUpgrader().recreateTables();
  }

  @Test
  public void testSimpleSelect() {
    insertBar("2010-10-10", "Foo");

    SQLType sType = parse("SELECT * FROM Bar");
    Iterator<IDBObject> rs = executeQuery(IDBObject.class, sType).iterator();
    assertTrue(rs.hasNext());
    IDBObject obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.getColumnValue("Date"));
    assertEquals("Foo", obj.getColumnValue("Misc"));
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
    Iterator<IDBObject> rs = executeQuery(IDBObject.class, sType).iterator();
    assertTrue(rs.hasNext());
    IDBObject obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.getColumnValue("Date"));
    assertEquals("Foo", obj.getColumnValue("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date < :foo");
    Iterator<IDBObject> rs = executeQuery(IDBObject.class, sType, "2011-1-1").iterator();
    assertTrue(rs.hasNext());
    IDBObject obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.getColumnValue("Date"));
    assertEquals("Foo", obj.getColumnValue("Misc"));
    assertFalse(rs.hasNext());
  }

  @Test
  public void testSimpleSelectWithTwoVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    SQLType sType = parse("SELECT * FROM Bar WHERE Date < :foo AND Misc LIKE :bar");
    Iterator<IDBObject> rs = executeQuery(IDBObject.class, sType, "2011-1-1", "Foo").iterator();
    assertTrue(rs.hasNext());
    IDBObject obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.getColumnValue("Date"));
    assertEquals("Foo", obj.getColumnValue("Misc"));
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
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    DatabaseImpl database = dbTypeLoader.getTypeDataForNamespace("test.testdb");
    return database;
  }


  private <T> Iterable<T> executeQuery(Class<T> clz, SQLType sType, Object... args) {
    return (Iterable<T>) sType.getTypeInfo().invokeQuery(args);
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
      throw new UnsupportedOperationException();
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
