package test;

import gw.lang.init.GosuInitialization;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

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
    LoggerFactory.getLogger("Tosa").trace("Here");
  }

  @Override
  public void initializeGosu() {
    GosuInitialization.initializeRuntime(GosuInitHelper.constructPathEntriesFromSystemClasspath("tosa.loader.DBTypeLoader"));
  }
}
