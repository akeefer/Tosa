package tosa.loader

/**
 * This class effectively serves as a static mix-in for the DBType types.  Each function in here
 * needs to have its first argument be an IDBType object.  In addition, parameters that are
 * typed as IDBObject will be transformed at delegation time to the type in question, i.e.
 * something like tosa.testdb.Foo will be used in place of IDBObject, providing a sort of
 * covariance on the delegated type.  Any getter/setter pairs here will be turned into
 * properties for delegation purposes.  Every function on this class needs to be static.
 *
 * These methods SHOULD NOT be called directly by code:  they're purely here for delegation
 * purposes, to make it easier (and clearer) to write the static methods that are automatically
 * added to every DBType.
 */
class DBTypeDelegate {


}