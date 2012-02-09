package tosa;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/8/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TosaLoaderTestBase {

  protected void assertEquals(int expected, int actual) {
    Assert.assertEquals(expected, actual);
  }
  
  protected void assertEquals(long expected, long actual) {
    Assert.assertEquals(expected, actual);
  }
  
  protected void assertEquals(String expected, String actual) {
    Assert.assertEquals(expected, actual);
  }

  protected void assertEquals(Object expected, Object actual) {
    Assert.assertEquals(expected, actual);
  }
  
  protected void assertTrue(boolean condition) {
    Assert.assertTrue(condition);
  }
  
  protected void assertFalse(boolean condition) {
    Assert.assertFalse(condition);
  }
  
  protected void fail() {
    Assert.fail();
  }
  
  protected void fail(String message) {
    Assert.fail(message);
  }
  
  protected void assertNull(Object object) {
    Assert.assertNull(object);
  }
  
  protected void assertNotNull(Object object) {
    Assert.assertNotNull(object);
  }
  
  protected void assertSame(Object expected, Object actual) {
    Assert.assertSame(expected, actual);
  }
}
