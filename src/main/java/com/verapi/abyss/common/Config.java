/*
 *
 *  *  Copyright (C) Verapi Yazilim Teknolojileri A.S. - All Rights Reserved
 *  *
 *  *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  *  Proprietary and confidential
 *  *
 *  *  Written by Halil Özkan <halil.ozkan@verapi.com>, 1 2019
 *
 */

package com.verapi.abyss.common;

import io.vertx.core.json.JsonObject;

public class Config {
    private static Config instance = null;
    private JsonObject config;

    private Config() {
    }

    public static Config getInstance() {
        if (instance == null)
            instance = new Config();
        return instance;
    }

    public JsonObject getConfigJsonObject() {
        if (config == null)
            setConfig(new JsonObject());
        return config;
    }

    public Config setConfig(JsonObject config) {
        this.config = config;
        return this;
    }

    @Override
    public void finalize() {
        if (config != null) {
            config.clear();
        }
    }
}
