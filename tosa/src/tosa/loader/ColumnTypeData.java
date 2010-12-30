package tosa.loader;

import tosa.loader.data.ColumnData;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnTypeData {
  private ColumnData _columnData;

  private boolean _isFK;
  private String _fkTarget;

  public ColumnTypeData(ColumnData columnData) {
    _columnData = columnData;
    initializeBasicProperties();
  }

  private void initializeBasicProperties() {
    String colName = getColumnName();
    // TODO - I don't believe that this is really the correct behavior . . . it seems like this only catches
    // Company_id and not Employer_Company_id
    // TODO - AHK - Also check the type to make sure it's appropriate
    if (colName.endsWith("_id") && !colName.substring(0, colName.length() - 3).contains("_")) {
      _isFK = true;
      _fkTarget = colName.substring(0, colName.length() - 3);
    }
  }

  public String getColumnName() {
    return _columnData.getName();
  }

  public boolean isFK() {
    return _isFK;
  }

  public String getFkTarget() {
    return _fkTarget;
  }

  public boolean isIdColumn() {
    // TODO - AHK - Some day, this should perhaps check to make sure that the column has the right attributes
    return getColumnName().equals("id");
  }
}
