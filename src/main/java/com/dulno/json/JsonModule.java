package com.dulno.json;

import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.json.action.array.JsonReadArrayAction;
import com.dulno.json.action.array.index.JsonReadArrayIndexAction;
import com.dulno.json.action.value.JsonReadValueAction;
import com.google.inject.Injector;

@ModuleDescription(name = "json", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class JsonModule extends Module {
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