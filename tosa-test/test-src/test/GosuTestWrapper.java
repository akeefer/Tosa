package test;

import gw.lang.init.GosuInitialization;
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
public abstract class GosuTestWrapper implements ITestWrapper {

  public GosuTestWrapper() {
    System.out.println("Here");
  }

  @Override
  public void initializeGosu() {
    GosuInitialization.initializeRuntime(GosuInitHelper.constructPathEntriesFromSystemClasspath("tosa.loader.DBTypeLoader"));
  }
}
