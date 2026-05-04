package io.github.openpolicyagent.opa.ir.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * Func represents a named plan (function) that can be invoked. Functions accept one or more
 * parameters and return a value. By convention, the input document and data documents are always
 * passed as the first and second arguments (respectively).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Func {
  private int maxLocal = Integer.MIN_VALUE;

  @JsonProperty("name")
  private String name;

  @JsonProperty("params")
  private List<Integer> params;

  @JsonProperty("return")
  private int returnVal;

  @JsonProperty("blocks")
  private List<Block> blocks;

  @JsonProperty("path")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  private List<String> path;

  public Func() {}

  public Func(
      String name, List<Integer> params, int returnVal, List<Block> blocks, List<String> path) {
    this.name = name;
    this.params = params;
    this.returnVal = returnVal;
    this.blocks = blocks;
    this.path = path;
  }

  public int getMaxLocalForFunction() {
    if (maxLocal == Integer.MIN_VALUE) {
      int funcStmtMax = getBlocks().stream().mapToInt(Block::maxLocal).max().orElse(-1);
      maxLocal = Math.max(funcStmtMax, getReturnVal());
    }
    return maxLocal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Func func = (Func) o;

    if (returnVal != func.returnVal) return false;
    if (!Objects.equals(name, func.name)) return false;
    if (!Objects.equals(params, func.params)) return false;
    if (!Objects.equals(blocks, func.blocks)) return false;
    return Objects.equals(path, func.path);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (params != null ? params.hashCode() : 0);
    result = 31 * result + returnVal;
    result = 31 * result + (blocks != null ? blocks.hashCode() : 0);
    result = 31 * result + (path != null ? path.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Func{"
        + "name='"
        + name
        + '\''
        + ", params="
        + params
        + ", returnVal="
        + returnVal
        + ", blocks="
        + blocks
        + ", path="
        + path
        + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Integer> getParams() {
    return params;
  }

  public void setParams(List<Integer> params) {
    this.params = params;
  }

  public int getReturnVal() {
    return returnVal;
  }

  public void setReturnVal(int returnVal) {
    this.returnVal = returnVal;
  }

  public List<Block> getBlocks() {
    return blocks;
  }

  public void setBlocks(List<Block> blocks) {
    this.blocks = blocks;
  }

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }
}
