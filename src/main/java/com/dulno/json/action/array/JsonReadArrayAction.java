package com.dulno.json.action.array;

import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.ListOutputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class JsonReadArrayAction implements Action<JsonReadArrayActionExecutor> {
  public static JsonReadArrayAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("separator", DatabaseDataType.TEXT));
    return new JsonReadArrayAction(ActionContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "action_json_read_array",
      contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "json-read-array-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("json.action.read.array.name")
      .withDescription("json.action.read.array.description")
      .withInputVariable(InputComponentVariable.createRequired("json.action.read.array.input.content.name",
        "jsonContent", "json.action.read.array.input.content.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("json.action.read.array.input.path.name",
        "jsonPath", "json.action.read.array.input.path.description", "first.second.third", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("json.action.read.array.input.separator.name",
        "jsonSeparator", "json.action.read.array.input.separator.description", ".", InputComponentDataType.TEXT))
      .withOutputVariable(ListOutputComponentVariable.create("json.action.read.array.output.array", "jsonArray",
        OutputComponentVariable.create("json.action.read.array.output.array.entry", "jsonArrayEntry")))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.output.array.length", "jsonArrayLength"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.output.content", "jsonContent"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.output.path", "jsonPath"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.output.separator", "jsonSeparator"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    var jsonPath = content.get("jsonPath");
    var jsonSeparator = content.get("jsonSeparator");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("jsonContent"), jsonPath == null ? "" : jsonPath,
      jsonSeparator == null ? "" : jsonSeparator));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("jsonContent", row.findCell(1).stringValue(),
        "jsonPath", row.findCell(2).stringValue(),
        "jsonSeparator", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<JsonReadArrayActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      JsonReadArrayActionExecutor.create(content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
