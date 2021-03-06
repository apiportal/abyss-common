/*
 * Copyright 2019 Verapi Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verapi.abyss.common;


import io.vertx.core.json.JsonObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 *
 */
public class Util {

    /**
     * Null value<br>
     * Returns a value if tested value is null, like Oracle NVL function
     *
     * @param a   value to be checked if null
     * @param b   value to be returned if a is null
     * @param <T> any
     * @return returns a if a is <b>not</b> null <br>returns b if a is null
     */
    public static <T> T nvl(T a, T b) {
        return (a == null) ? b : a;
    }

    /**
     * Not Null Value<br>
     * Returns its calculated value if it is not null
     *
     * @param a   value to be checked if null
     * @param b   value to be returned if a is <b>not</b> null
     * @param <T> any
     * @return returns b if a is <b>not</b> null <br> returns a if a is null
     */
    public static <T> T nnvl(T a, T b) {
        //TODO: replaced with functional interface to disable executing b parameter before initalization
        if (a != null)
            return b;
        else
            return null;
    }

    public static String encodeFileToBase64Binary(File file) throws IOException {
        byte[] bytes;
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            bytes = new byte[(int) file.length()];
            if (fileInputStreamReader.read(bytes) > 0) {
                return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
            } else
                throw new IOException("provided file is read but number of bytes of buffer is not greater then 0");
        }
    }

    public static JsonObject convertYamlToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(yamlString);

        return new JsonObject(map);
    }

    public static JsonObject loadYamlFile(File yamlFileName) throws FileNotFoundException {
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(Objects.requireNonNull(classLoader.getResource(yamlFileName)).getFile());
        InputStream inputStream = new FileInputStream(yamlFileName);
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        return new JsonObject(map);
    }
}
