package tosa.impl.query;

import org.slf4j.profiler.Profiler;
import tosa.api.IDatabase;
import tosa.api.IPreparedStatementParameter;
import tosa.api.IQueryResultProcessor;
import tosa.api.QueryResult;
import tosa.loader.Util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryResultImpl<T> implements QueryResult<T> {

  private String _profilerTag;
  private String _originalQuery;
  private IPreparedStatementParameter[] _parameters;
  private IDatabase _db;
  private IQueryResultProcessor<T> _resultProcessor;

  // TODO - AHK - Paging, all that stuff
  private List<T> _results;

  public QueryResultImpl(String profilerTag, String originalQuery, IPreparedStatementParameter[] parameters, IDatabase db, IQueryResultProcessor<T> resultProcessor) {
    _profilerTag = profilerTag;
    _originalQuery = originalQuery;
    _parameters = parameters;
    _db = db;
    _resultProcessor = resultProcessor;
  }

  public int getCount() {
    maybeLoadResults();
    return _results.size();
  }

  public int size() {
    return getCount();
  }

  @Override
  public Iterator<T> iterator() {
    maybeLoadResults();
    return _results.iterator();
  }

  @Override
  public T get(int index) {
    maybeLoadResults();
    return _results.get(index);
  }

  private void maybeLoadResults() {
    if (_results == null) {
      _results = executeQuery();
    }
  }

  private List<T> executeQuery() {
    Profiler profiler = Util.newProfiler(_profilerTag);
    profiler.start(_originalQuery + " (" + Arrays.asList(_parameters) + ")");
    try {
      return _db.getDBExecutionKernel().executeSelect(_originalQuery,
          _resultProcessor,
          _parameters);
    } finally {
      profiler.stop();
    }
  }
}
