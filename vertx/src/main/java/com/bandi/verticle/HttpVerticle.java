package com.bandi.verticle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.tagresolver.IncludeResolver;
import org.raml.parser.tagresolver.JacksonTagResolver;
import org.raml.parser.tagresolver.JaxbTagResolver;
import org.raml.parser.tagresolver.TagResolver;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.YamlDocumentBuilder;
import org.yaml.snakeyaml.Yaml;

import com.bandi.data.ResponseData;
import com.bandi.http.HttpRequestResponseHandler;
import com.bandi.http.HttpRoutingContext;
import com.bandi.http.HttpStaticHandler;
import com.bandi.log.Logger;
import com.bandi.raml.RAMLParser;
import com.bandi.util.Constants;
import com.bandi.util.Utils;
import com.bandi.validate.Validator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.Getter;

public class HttpVerticle extends AbstractVerticle {
	
	@Getter
	private static Vertx vert;

	@Override
	public void start(Future<Void> startFuture) {

		vert = vertx;
		
		HttpServer httpServer = vertx.createHttpServer();

		RAMLParser ramlParser = new RAMLParser();
		ramlParser.processRAML();

		Router router = Router.router(vertx);
		router.route(Constants.ROOT).handler(StaticHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(new HttpRoutingContext());
		httpServer.requestHandler(router::accept).listen(Constants.PORT);
		

		HttpServer httpServer1 = vertx.createHttpServer();
		Router router1 = Router.router(vertx);
		router1.route().handler(StaticHandler.create());
		httpServer1.requestHandler(router1::accept);
		httpServer1.listen(Constants.ADMIN_PORT);

		Logger.log("Mock Service started!");
	}

	@Override
	public void stop(Future stopFuture) throws Exception {
		Logger.log("Mock Service stopped!");
	}

	private TagResolver[] defaultResolver(TagResolver[] tagResolvers) {
		TagResolver[] defaultResolvers = new TagResolver[] { new IncludeResolver(), new JacksonTagResolver(),
				new JaxbTagResolver() };
		return (TagResolver[]) ArrayUtils.addAll(defaultResolvers, tagResolvers);
	}

	private void yamlParserForExtractingExample(URL url, String ramlLocation, Resource resource) {
		Yaml yaml = (Yaml) new YamlDocumentBuilder(Yaml.class, new DefaultResourceLoader(), defaultResolver(null))
				.build(ramlLocation);

		FileReader fr;
		try {
			fr = new FileReader(url.getFile());

			Map config = (Map) yaml.load(fr);
			Map usersConfig = ((Map) config.get(resource.getUri()));
		} catch (FileNotFoundException e) {
			Logger.log(e);
		}
	}

}
