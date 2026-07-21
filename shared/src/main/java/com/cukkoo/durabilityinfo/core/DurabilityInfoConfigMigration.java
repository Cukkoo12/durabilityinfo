package com.cukkoo.durabilityinfo.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class DurabilityInfoConfigMigration {
    private DurabilityInfoConfigMigration() {}

    public static DurabilityInfoConfig migrate(JsonObject source, Gson gson) {
        return ConfigMigration.migrate(source, gson);
    }
}
