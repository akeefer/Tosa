package test

uses org.junit.runner.RunWith
uses org.junit.runners.Suite
uses tosa.loader.SQLTypeInfoTest
uses org.junit.runners.Suite.SuiteClasses

@RunWith(Suite)
@SuiteClasses({
    tosa.impl.JoinArrayEntityCollectionImplTest,
    tosa.impl.ReverseFkEntityCollectionImplTest,
    tosa.impl.QueryResultImplTest,
    tosa.loader.DatabaseAccessTypeTest,
    tosa.loader.DBTypeInfoSelectTest,
    tosa.loader.DBTypeInfoTest,
    tosa.loader.SQLTypeInfoTest
})
public class TosaSuite {}
