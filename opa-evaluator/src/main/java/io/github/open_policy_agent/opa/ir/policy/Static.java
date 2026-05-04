package io.github.open_policy_agent.opa.ir.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Static {
  @JsonProperty("strings")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  private List<StringConst> strings;

  @JsonProperty("builtin_funcs")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  private List<BuiltinFunc> builtinFuncs;

  @JsonProperty("files")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  private List<StringConst> files;

  public Static() {}

  public Static(
      List<StringConst> strings, List<BuiltinFunc> builtinFuncs, List<StringConst> files) {
    this.strings = strings;
    this.builtinFuncs = builtinFuncs;
    this.files = files;
  }

  public List<StringConst> getStrings() {
    return strings;
  }

  public void setStrings(List<StringConst> strings) {
    this.strings = strings;
  }

  public List<StringConst> getFiles() {
    return files;
  }

  public void setFiles(List<StringConst> files) {
    this.files = files;
  }

  public List<BuiltinFunc> getBuiltinFuncs() {
    return builtinFuncs;
  }

  public void setBuiltinFuncs(List<BuiltinFunc> builtinFuncs) {
    this.builtinFuncs = builtinFuncs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Static aStatic = (Static) o;

    if (!Objects.equals(strings, aStatic.strings)) return false;
    if (!Objects.equals(builtinFuncs, aStatic.builtinFuncs)) return false;
    return Objects.equals(files, aStatic.files);
  }

  @Override
  public int hashCode() {
    int result = strings != null ? strings.hashCode() : 0;
    result = 31 * result + (builtinFuncs != null ? builtinFuncs.hashCode() : 0);
    result = 31 * result + (files != null ? files.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Static{"
        + "strings="
        + strings
        + ", builtinFuncs="
        + builtinFuncs
        + ", files="
        + files
        + '}';
  }
}
