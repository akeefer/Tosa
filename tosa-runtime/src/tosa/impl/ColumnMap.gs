package tosa.impl

uses java.util.Map
uses java.util.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/23/12
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
class ColumnMap {
  
  var _originalColumnValues : Map<String, Object>
  var _changedColumnValues : Map<String, Object>
  
  construct(originalColumnValues : Map<String, Object>) {
    _originalColumnValues = originalColumnValues
    _changedColumnValues = {}
  }
  
  function containsKey(key : String) : boolean {
    return _changedColumnValues.containsKey(key) || _originalColumnValues?.containsKey(key)  
  }
  
  function get(key : String) : Object {
    if (_changedColumnValues.containsKey(key)) {
      return _changedColumnValues.get(key)
    } else {
      return _originalColumnValues?.get(key)
    } 
  }
  
  function put(key : String, value : Object) {
    if (_originalColumnValues?.containsKey(key) && _originalColumnValues.get(key) == value) {
      _changedColumnValues.remove(key)
    } else {
      _changedColumnValues.put(key, value)
    } 
  }

  function isValueChanged(key : String) : boolean {
    return _changedColumnValues.containsKey(key)
  }
  
  property get HasOriginalValues() : boolean {
    return _originalColumnValues != null
  }

  function acceptChanges() {
    if (_originalColumnValues == null) {
      _originalColumnValues = _changedColumnValues
    } else {
      _originalColumnValues.putAll(_changedColumnValues)
    }
      
    _changedColumnValues = {}
  }
  
  override function toString() : String {
    var combinedColumns = new HashMap<String, Object>()
    combinedColumns.putAll(_originalColumnValues)
    combinedColumns.putAll(_changedColumnValues)
    return combinedColumns.toString()
  }
 
}