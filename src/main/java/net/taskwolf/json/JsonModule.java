package net.taskwolf.json;

import net.taskwolf.core.account.AccountLink;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.json.action.array.JsonReadArrayAction;
import net.taskwolf.json.action.array.index.JsonReadArrayIndexAction;
import net.taskwolf.json.action.value.JsonReadValueAction;
import net.taskwolf.workflow.integration.Integration;
import com.google.inject.Injector;

@ModuleDescription(name = "json", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class JsonModule extends Integration {
  private Log log;
  private AccountLink accountLink;

  public JsonModule(Injector injector) {
    super(injector.createChildInjector(JsonInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Json");
    accountLink = JsonAccountLink.create();
  }

  @Override
  public void disable() {

  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("JSON", "", "json.webp",
      ModuleInformation.Type.PUBLIC, ModuleInformation.Novelty.NEW);
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = ActionRepository.create();
    repository.registerAction(JsonReadValueAction.create(databaseConnection,
      databaseKeyspace));
    repository.registerAction(JsonReadArrayAction.create(databaseConnection,
      databaseKeyspace));
    repository.registerAction(JsonReadArrayIndexAction.create(databaseConnection,
      databaseKeyspace));
    return repository;
  }
}