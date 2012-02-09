package test

uses org.junit.runner.RunWith
uses org.junit.runners.Suite
uses org.junit.runners.Suite.SuiteClasses

@RunWith(Suite)
@SuiteClasses({
    tosa.impl.parser.data.DBDataValidatorTest,
    tosa.loader.parser.mysql.MySQL51SQLParserTest,
    tosa.loader.parser.TokenizerTest
})
public class TosaLoaderSuite {}
