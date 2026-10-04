package net.taskwolf.json.action.array.index;

import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class JsonReadArrayIndexAction implements Action<JsonReadArrayIndexActionExecutor> {
  public static JsonReadArrayIndexAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("separator", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("arrayIndex", DatabaseDataType.TEXT));
    return new JsonReadArrayIndexAction(ActionContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "action_json_read_array_index",
      contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "json-read-array-index-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("json.action.read.array.index.name")
      .withDescription("json.action.read.array.index.description")
      .withInputVariable(InputComponentVariable.createRequired("json.action.read.array.index.input.content.name",
        "jsonContent", "json.action.read.array.index.input.content.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("json.action.read.array.index.input.path.name",
        "jsonPath", "json.action.read.array.index.input.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("json.action.read.array.index.input.separator.name",
        "jsonSeparator", "json.action.read.array.index.input.separator.description", ".", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("json.action.read.array.index.input.index.name",
        "jsonIndex", "json.action.read.array.index.input.index.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.index.output.array.entry", "jsonArrayEntry"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.index.output.content", "jsonContent"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.index.output.path", "jsonPath"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.index.output.separator", "jsonSeparator"))
      .withOutputVariable(OutputComponentVariable.create("json.action.read.array.index.output.index", "jsonIndex"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    var jsonPath = content.get("jsonPath");
    var jsonSeparator = content.get("jsonSeparator");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("jsonContent"), jsonPath == null ? "" : jsonPath,
      jsonSeparator == null ? "" : jsonSeparator, content.get("jsonIndex")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("jsonContent", row.findCell(1).stringValue(),
        "jsonPath", row.findCell(2).stringValue(),
        "jsonSeparator", row.findCell(3).stringValue(),
        "jsonIndex", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<JsonReadArrayIndexActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      JsonReadArrayIndexActionExecutor.create(content.findCell(1).stringValue(),
        content.findCell(2).stringValue(), content.findCell(3).stringValue(),
        content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
