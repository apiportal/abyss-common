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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verapi.abyss.exception.UnProcessableEntity422Exception;
import io.reactivex.Single;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.ObjectMapperFactory;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.ResolverCache;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RouterFactoryImpl;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAPIUtil {
    private static Logger logger = LoggerFactory.getLogger(OpenAPIUtil.class);

    public static final String OPENAPI_SECTION_SERVERS = "servers";
    private static final String ERRORMESSAGEHEADER = "openAPIParser error for: {}| {}";
    private static final String OPENAPIPARSERLOGMESSAGEHEADER = "openAPIParser {}";

    /**
     * @param apiSpec
     * @return
     * @deprecated
     */
    @Deprecated
    public static Single<SwaggerParseResult> _openAPIParser(JsonObject apiSpec) {
        logger.trace(OPENAPIPARSERLOGMESSAGEHEADER, "invoked");
        ObjectMapper mapper;
        String data = apiSpec.toString();
        try {
            if (data.trim().startsWith("{")) {
                mapper = ObjectMapperFactory.createJson();
            } else {
                mapper = ObjectMapperFactory.createYaml();
            }
            JsonNode rootNode = mapper.readTree(data);
            SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readWithInfo("streamed yaml", rootNode);
            if (swaggerParseResult.getMessages().isEmpty()) {
                logger.trace(OPENAPIPARSERLOGMESSAGEHEADER, "OK");
                return Single.just(swaggerParseResult);
            } else {
                if (swaggerParseResult.getMessages().size() == 1 && swaggerParseResult.getMessages().get(0).matches("unable to read location")) {
                    if (logger.isErrorEnabled())
                        logger.error(ERRORMESSAGEHEADER, data.substring(1, 40), swaggerParseResult.getMessages());
                    return Single.error(RouterFactoryException.createSpecNotExistsException(""));
                } else {
                    if (logger.isErrorEnabled())
                        logger.error(ERRORMESSAGEHEADER, data.substring(1, 40), swaggerParseResult.getMessages());
                    return Single.error(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(), ", ")));
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error(ERRORMESSAGEHEADER, e.getLocalizedMessage(), e.getStackTrace());
            return Single.error(RouterFactoryException.createSpecInvalidException(e.getLocalizedMessage()));
        }
    }

    public static Single<SwaggerParseResult> openAPIParser(JsonObject apiSpec) {
        logger.trace("---openAPIParser invoked");
        String data = apiSpec.toString();
        try {
            //SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readContents(data);
            SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readContents(data, null, OpenApi3Utils.getParseOptions());
            if (swaggerParseResult.getMessages().isEmpty()) {
                logger.trace("openAPIParser OK");
                return Single.just(swaggerParseResult);
            } else {
                if ((swaggerParseResult.getMessages().size() == 1) && (swaggerParseResult.getMessages().get(0) == null)) {
                    logger.trace("openAPIParser OK");
                    return Single.just(swaggerParseResult);
                } else if (swaggerParseResult.getMessages().size() == 1 && swaggerParseResult.getMessages().get(0).matches("unable to read location")) {
                    if (logger.isErrorEnabled())
                        logger.error(ERRORMESSAGEHEADER, data.substring(1, 40), swaggerParseResult.getMessages());
                    return Single.error(RouterFactoryException.createSpecNotExistsException(""));
                } else {
                    if (logger.isErrorEnabled())
                        logger.error(ERRORMESSAGEHEADER, data.substring(1, 40), swaggerParseResult.getMessages());
                    return Single.error(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(), ", ")));
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled())
                logger.error(ERRORMESSAGEHEADER, e.getLocalizedMessage(), e.getStackTrace());
            return Single.error(RouterFactoryException.createSpecInvalidException(e.getLocalizedMessage()));
        }
    }

    public static Single<JsonArray> openAPIParser(String apiSpec) {
        logger.trace("---openAPIParser invoked");
        ObjectMapper mapper;
        try {
            if (apiSpec.trim().startsWith("{")) {
                mapper = ObjectMapperFactory.createJson();
            } else {
                mapper = ObjectMapperFactory.createYaml();
            }
            JsonNode rootNode = mapper.readTree(apiSpec);
            SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readWithInfo("streamed yaml", rootNode);
            JsonArray jsonArray = new JsonArray();
            if (swaggerParseResult.getMessages().isEmpty()) {
                //no result message means openapi spec is valid, it is OK, so return empty error message array
                return Single.just(jsonArray);
            } else {
                //there are parse validation errors, so return eror message array
                swaggerParseResult.getMessages().forEach(jsonArray::add);
                return Single.error(new UnProcessableEntity422Exception(jsonArray.encode()));
            }
        } catch (Exception e) {
            logger.error("openAPIParser error | {} | {}", e.getLocalizedMessage(), e.getStackTrace());
            return Single.error(RouterFactoryException.createSpecInvalidException(e.getLocalizedMessage()));
        }
    }

    public static void createOpenAPI3RouterFactory(io.vertx.reactivex.core.Vertx vertx, String yaml, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
        SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readContents(yaml);
        createOpenAPI3RouterFactory(vertx, swaggerParseResult.getOpenAPI(), handler);
    }

    public static void createOpenAPI3RouterFactory(io.vertx.reactivex.core.Vertx vertx, OpenAPI openAPI, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
        createOpenAPI3RouterFactoryImpl(vertx.getDelegate(), openAPI, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory.newInstance(ar.result())));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private static void createOpenAPI3RouterFactoryImpl(Vertx vertx, OpenAPI openAPI, Handler<AsyncResult<io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory>>
            handler) {
        vertx.executeBlocking((Future<io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory> future) -> {
            future.complete(new OpenAPI3RouterFactoryImpl(vertx, openAPI, new ResolverCache(openAPI, null, null)));
        }, handler);
    }

    public static Single<JsonObject> convertSwaggerToOpenAPIOld(JsonObject apiSpec) {
        String data = apiSpec.toString();
        SwaggerParseResult swaggerParseResult = new SwaggerConverter().readContents(data, null, OpenApi3Utils.getParseOptions());
        JsonArray jsonArray = new JsonArray();
        if (swaggerParseResult.getMessages().isEmpty()) {
            //no result message means openapi spec is valid, it is OK, so return empty error message array
            ObjectMapper mapper = ObjectMapperFactory.createYaml();
            try {

                String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(swaggerParseResult.getOpenAPI());
                return Single.just(new JsonObject().put("openApiYaml", string.replaceAll("extensions: \\{\\}", "")).put("openApiSpecVersion", swaggerParseResult.getOpenAPI().getOpenapi()));
            } catch (JsonProcessingException e) {
                return Single.error(new UnProcessableEntity422Exception(e.getMessage()));
            }
        } else {
            //there are parse validation errors, so return error message array
            swaggerParseResult.getMessages().forEach(jsonArray::add);
            return Single.error(new UnProcessableEntity422Exception(jsonArray.encode()));
        }
    }

    public static Single<String> convertSwaggerToOpenAPI(JsonObject apiSpec) {
        String data = apiSpec.toString();
        SwaggerParseResult swaggerParseResult = new SwaggerConverter().readContents(data, null, OpenApi3Utils.getParseOptions());
        JsonArray jsonArray = new JsonArray();
        if (swaggerParseResult.getMessages().isEmpty()) {
            //no result message means openapi spec is valid, it is OK, so return empty error message array
            ObjectMapper mapper = ObjectMapperFactory.createYaml();
            try {
//                SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
//                        .serializeAllExcept("extensions");
//                FilterProvider filters = new SimpleFilterProvider()
//                        .addFilter("myFilter", theFilter);

                //String string = mapper.writer(filters).writeValueAsString(swaggerParseResult.getOpenAPI());
                //return Single.just(string);
                String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(swaggerParseResult.getOpenAPI());
                return Single.just(string.replaceAll("extensions: \\{\\}", ""));
            } catch (JsonProcessingException e) {
                return Single.error(new UnProcessableEntity422Exception(e.getMessage()));
            }
        } else {
            //there are parse validation errors, so return error message array
            swaggerParseResult.getMessages().forEach(jsonArray::add);
            return Single.error(new UnProcessableEntity422Exception(jsonArray.encode()));
        }
    }

}
