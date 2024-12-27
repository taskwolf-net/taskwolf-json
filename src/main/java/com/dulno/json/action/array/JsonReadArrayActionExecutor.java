package com.dulno.json.action.array;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@AllArgsConstructor(staticName = "create")
public final class JsonReadArrayActionExecutor implements ActionExecutor {
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
    return CompletableFuture.completedFuture(findArray());
  }

  private ActionResult findArray() {
    try {
      if (path.isEmpty()) {
        return ActionResult.success(buildInformation(new JSONArray(content)));
      }
      var json = new JSONObject(content);
      var parts = path.split(Pattern.quote(separator));
      var end = traceJsonPath(json, parts);
      if (end.isEmpty()) {
        return ActionResult.failure("json.action.read.array.failure.not.found");
      }
      var array = end.get().getJSONArray(parts[parts.length - 1]);
      return ActionResult.success(buildInformation(array));
    } catch (Exception exception) {
      return ActionResult.failure("json.action.read.array.failure.wrong.format");
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

  private Map<String, Object> buildInformation(JSONArray array) {
    return buildInformation(array.toList().stream()
      .map(entry -> Map.of("jsonArrayEntry", entry)).toList());
  }

  private Map<String, Object> buildInformation(List<Map<String, Object>> array) {
    var information = Maps.<String, Object>newHashMap();
    information.put("jsonArray", new JSONArray(array));
    information.put("jsonArrayLength", array.size());
    information.put("jsonContent", content);
    information.put("jsonPath", path);
    information.put("jsonSeparator", separator);
    return information;
  }
}
