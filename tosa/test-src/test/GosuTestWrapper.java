package test;

import junit.framework.TestCase;
import org.junit.runner.RunWith;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/4/11
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(GosuTestRunner.class)
public abstract class GosuTestWrapper {

  public GosuTestWrapper() {
    System.out.println("Here");
  }

  public abstract String getWrappedTestName();
}
