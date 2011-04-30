package tosa.loader.parser.tree;

import java.util.Map;

public interface IMightApply {
  public boolean applies(Map<String, Object> values);
}
