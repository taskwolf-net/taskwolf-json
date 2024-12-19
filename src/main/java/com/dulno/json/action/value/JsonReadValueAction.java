package com.dulno.json.action.value;

import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.ComponentNovelty;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class JsonReadValueAction implements Action<JsonReadValueActionExecutor> {
  public static JsonReadValueAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("separator", DatabaseDataType.TEXT));
    return new JsonReadValueAction(ActionContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "action_json_read_value",
      contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "json-read-value-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("json.action.read.value.name")
      .withDescription("json.action.read.value.description")
      .withInputVariable(InputComponentVariable.createRequired("json.action.read.value.input.content.name",
        "jsonContent", "json.action.read.value.input.content.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("json.action.read.value.input.path.name",
        "jsonPath", "json.action.read.value.input.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("json.action.read.value.input.separator.name",
        "jsonSeparator", "json.action.read.value.input.separator.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.value.output.value", "jsonValue"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.value.output.content", "jsonContent"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.value.output.path", "jsonPath"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.value.output.separator", "jsonSeparator"))
      .withNovelty(ComponentNovelty.NEW)
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    var jsonSeparator = content.get("jsonSeparator");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("jsonContent"), content.get("jsonPath"),
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
  public CompletableFuture<JsonReadValueActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      JsonReadValueActionExecutor.create(content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
