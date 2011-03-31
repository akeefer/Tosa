package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 3/4/11
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableConstraintDefinition extends SQLParsedElement {

  public static enum ConstraintType {
    PRIMARY_KEY,
    INDEX,
    KEY,
    UNIQUE_INDEX,
    UNIQUE_KEY,
    FULLTEXT_INDEX,
    FULLTEXT_KEY,
    SPATIAL_INDEX,
    SPATIAL_KEY,
    FOREIGN_KEY
  }

  // TODO - AHK - Track if it's an index or key
  // TODO - AHK - Track if it's fulltext or spatial

  private ConstraintType _constraintType;
  private Token _symbolName;
  private IndexName _indexName;
  private IndexType _indexType;
  private List<IndexColumnName> _columnNames;
  private List<SQLParsedElement> _indexOptions;

  public TableConstraintDefinition(Token start, Token end, ConstraintType constraintType, Token symbolName, IndexName indexName, IndexType indexType, List<IndexColumnName> columnNames, List<SQLParsedElement> indexOptions) {
    super(start, end, collectChildren(indexName, indexType, columnNames,  indexOptions));
    _constraintType = constraintType;
    _symbolName = symbolName;
    _indexName = indexName;
    _indexType = indexType;
    _columnNames = columnNames;
    _indexOptions = indexOptions;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    // TODO - AHK
  }

  private static List<SQLParsedElement> collectChildren(SQLParsedElement child1, SQLParsedElement child2, List<? extends SQLParsedElement> list1, List<? extends SQLParsedElement> list2) {
    List<SQLParsedElement> children = new ArrayList<SQLParsedElement>();
    if (child1 != null) {
      children.add(child1);
    }
    if (child2 != null) {
      children.add(child2);
    }
    children.addAll(list1);
    children.addAll(list2);
    return children;
  }
}
