package com.dulno.json.action.value;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class JsonReadValueActionExecutor implements ActionExecutor {
  private String content;
  private String path;
  private String separator;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    content = dissolve.dissolve(content);
    path = dissolve.dissolve(path);
    separator = dissolve.dissolve(separator);
    if (separator.isEmpty()) {
      separator = ".";
    }
    return CompletableFuture.completedFuture(findValue());
  }

  private ActionResult findValue() {
    try {
      var json = new JSONObject(content);
      var parts = path.contains(separator) ? path.split(separator) :
        new String[] {path};
      var end = traceJsonPath(json, parts);
      if (end.isEmpty()) {
        return ActionResult.failure("json.action.read.value.failure.not.found");
      }
      var value = end.get().get(parts[parts.length - 1]).toString();
      return ActionResult.success(buildInformation(value));
    } catch (Exception exception) {
      return ActionResult.failure("json.action.read.value.failure.wrong.format");
    }
  }

  private Optional<JSONObject> traceJsonPath(JSONObject json, String[] parts) {
    for (var i = 0;  i < parts.length; i++) {
      var part = parts[i];
      if (!json.has(part)) {
        return Optional.empty();
      }
      if (i == parts.length - 1) {
        break;
      }
      json = json.getJSONObject(parts[i]);
    }
    return Optional.of(json);
  }

  private Map<String, Object> buildInformation(String value) {
    var information = Maps.<String, Object>newHashMap();
    information.put("jsonValue", value);
    information.put("jsonContent", content);
    information.put("jsonPath", path);
    information.put("jsonSeparator", separator);
    return information;
  }
}
