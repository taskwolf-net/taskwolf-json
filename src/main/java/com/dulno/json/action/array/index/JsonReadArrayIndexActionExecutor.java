package com.dulno.json.action.array.index;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class JsonReadArrayIndexActionExecutor implements ActionExecutor {
  private String content;
  private String path;
  private String separator;
  private String index;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    content = dissolve.dissolve(content);
    path = dissolve.dissolve(path);
    separator = dissolve.dissolve(separator);
    if (separator.isEmpty()) {
      separator = ".";
    }
    index = dissolve.dissolve(index);
    return CompletableFuture.completedFuture(findArrayIndex());
  }

  private ActionResult findArrayIndex() {
    try {
      var index = Integer.parseInt(this.index);
      if (path.isEmpty()) {
        return ActionResult.success(buildInformation(new JSONArray(content), index));
      }
      var json = new JSONObject(content);
      var parts = path.contains(separator) ? path.split(separator) :
        new String[] {path};
      var end = traceJsonPath(json, parts);
      if (end.isEmpty()) {
        return ActionResult.failure("json.action.read.array.index.failure.not.found");
      }
      var array = end.get().getJSONArray(parts[parts.length - 1]);
      return ActionResult.success(buildInformation(array, index));
    } catch (NumberFormatException exception) {
      return ActionResult.failure("json.action.read.array.index.failure.index.wrong.format");
    } catch (Exception exception) {
      return ActionResult.failure("json.action.read.array.index.failure.array.wrong.format");
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

  private Map<String, Object> buildInformation(JSONArray array, int index) {
    return buildInformation(array.get(index).toString());
  }

  private Map<String, Object> buildInformation(String entry) {
    var information = Maps.<String, Object>newHashMap();
    information.put("jsonArrayEntry", entry);
    information.put("jsonContent", content);
    information.put("jsonPath", path);
    information.put("jsonSeparator", separator);
    information.put("jsonIndex", index);
    return information;
  }
}
