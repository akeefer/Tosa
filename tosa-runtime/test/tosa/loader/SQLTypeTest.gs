package tosa.loader

uses tosa.TosaDBTestBase
uses org.junit.Test
uses test.testdb.Bar
uses java.util.Date
uses java.io.ByteArrayInputStream
uses gw.fs.IFile
uses java.io.InputStream
uses gw.lang.reflect.TypeSystem
uses java.lang.UnsupportedOperationException
uses java.io.OutputStream
uses gw.fs.IDirectory
uses java.io.File
uses gw.fs.ResourcePath
uses java.net.URI
uses java.text.SimpleDateFormat
uses gw.util.GosuExceptionUtil
uses java.lang.Iterable
uses gw.lang.reflect.java.JavaTypes
uses java.util.Map
uses tosa.api.IDBObject

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/13/12
 * Time: 8:33 AM
 * To change this template use File | Settings | File Templates.
 */
class SQLTypeTest extends TosaDBTestBase {

  @Test
  function testSimpleSelect() {
    insertBar("2010-10-10", "Foo");

    var sType = parse("SELECT * FROM Bar");
    var rs = executeQuery<Map>(sType, {}).iterator();
    assertTrue(rs.hasNext());
    var obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("DATE"));
    assertEquals("Foo", obj.get("MISC"));
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithTwoResults() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-10", "Bar");

    var sType = parse("SELECT * FROM Bar");
    var rs = executeQuery<IDBObject>(sType, {}).iterator();
    assertTrue(rs.hasNext());
    rs.next();
    assertTrue(rs.hasNext());
    rs.next();
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithCondition() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    var sType = parse("SELECT * FROM Bar WHERE Date < '2011-1-1'");
    var rs = executeQuery<Map>(sType, {}).iterator();
    assertTrue(rs.hasNext());
    var obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("DATE"));
    assertEquals("Foo", obj.get("MISC"));
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    var sType = parse("SELECT * FROM Bar WHERE Date < :foo");
    var rs = executeQuery<Map>(sType, {"2011-1-1"}).iterator();
    assertTrue(rs.hasNext());
    var obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("DATE"));
    assertEquals("Foo", obj.get("MISC"));
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithTwoVariable() {
    insertBar("2010-10-10", "Foo");
    insertBar("2012-10-11", "Bar");

    var sType = parse("SELECT * FROM Bar WHERE Date < :foo AND Misc LIKE :bar");
    var rs = executeQuery<Map>(sType, {"2011-1-1", "Foo"}).iterator();
    assertTrue(rs.hasNext());
    var obj = rs.next();
    assertEquals(slqDate("2010-10-10"), obj.get("DATE"));
    assertEquals("Foo", obj.get("MISC"));
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithIsNotNull() {
    insertBar("2010-10-10", "Foo");

    var sType = parse("SELECT * FROM Bar WHERE Date IS NOT NULL");
    var rs = executeQuery<Map>(sType, {}).iterator();
    assertTrue(rs.hasNext());
    rs.next();
    assertFalse(rs.hasNext());
  }

  @Test
  function testSimpleSelectWithIsNull() {
    insertBar("2010-10-10", "Foo");

    var sType = parse("SELECT * FROM Bar WHERE Date IS NULL");
    var rs = executeQuery<Map>(sType, {}).iterator();
    assertFalse(rs.hasNext());
  }

  //===================================================================
  // HELPER STUFF
  //===================================================================

  static private function insertBar(date : String, misc : String) {
    var bar = new Bar(){:Date = new SimpleDateFormat("yyyy-MM-dd").parse(date), :Misc = misc}
    bar.update()
  }

  private function executeQuery<T>(sType : SQLType, args : Object[]) : Iterable<T>  {
    return (Iterable<T>) sType.getTypeInfo().invokeQuery( JavaTypes.MAP().getGenericType().getParameterizedType({JavaTypes.STRING(), JavaTypes.OBJECT()}), args);
  }

  private function parse(sql : String) : SQLType {
    return new SQLType(new SQLFileInfo("test.Test", getDB(), new StringFile(sql)), TypeSystem.getTypeLoader(DBTypeLoader));
  }

  private function slqDate(s : String) : Object {
    return new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(s).getTime());
  }

  private static class StringFile implements IFile {
    private var _content : String;

    public construct(content : String) {
      _content = content;
    }

    override function openInputStream() : InputStream {
      return new ByteArrayInputStream(_content.getBytes());
    }

    override function openOutputStream() : OutputStream {
      throw new UnsupportedOperationException();
    }

    override function openOutputStreamForAppend() : OutputStream {
      throw new UnsupportedOperationException();
    }

    override property get Extension() : String {
      throw new UnsupportedOperationException();
    }

    override property get BaseName() : String {
      throw new UnsupportedOperationException();
    }

    override property get Parent() : IDirectory {
      throw new UnsupportedOperationException();
    }

    override property get Name() : String {
      return "String : " + _content;
    }

    override function exists() : boolean {
      throw new UnsupportedOperationException();
    }

    override function delete() : boolean {
      throw new UnsupportedOperationException();
    }

    override function toURI() : URI {
      throw new UnsupportedOperationException();
    }

    override property get Path() : ResourcePath {
      throw new UnsupportedOperationException();
    }

    override function isChildOf(iDirectory : IDirectory) : boolean {
      throw new UnsupportedOperationException();
    }

    override function isDescendantOf(iDirectory : IDirectory) : boolean {
      throw new UnsupportedOperationException();
    }

    override function toJavaFile() : File {
      throw new UnsupportedOperationException();
    }

    override property get JavaFile() : boolean {
      throw new UnsupportedOperationException();
    }
  }
}