package tosa.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import tosa.impl.parser.data.DDLParsingValidationTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  DDLParsingValidationTest.class,
  tosa.loader.parser.DDLParserTest.class,
  tosa.loader.parser.SelectParsingBootstrapTest.class,
  tosa.loader.parser.TokenizerTest.class
})
public class TosaLoaderSuite {}
