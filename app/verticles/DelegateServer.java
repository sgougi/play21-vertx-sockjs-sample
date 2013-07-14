package verticles;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import play.Configuration;
import play.Play;
import controllers.AppLogger;

public class DelegateServer extends Verticle {

	public void start() {
		final Configuration c = Play.application().configuration();

		final HttpClient httpClient = vertx.createHttpClient()
			.setHost("localhost")
			.setPort(c.getInt("http.port", 9000))
			.setUsePooledBuffers(true);
		
		final HttpServer server = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
			public void handle(final HttpServerRequest req) {
				AppLogger.debug("Proxying request: %s",  req.uri());
				final HttpClientRequest cReq = getRequest(req, httpClient);
				cReq.headers().set(req.headers());
				cReq.headers().set("play-vertx-delegate", "true");
				cReq.setChunked(true);
				req.dataHandler(new Handler<Buffer>() {
					public void handle(Buffer data) {
						cReq.write(data);
					}
				});
				req.endHandler(new VoidHandler() {
					public void handle() {
						cReq.end();
					}
				});
			}
		});
        
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		JsonObject config = new JsonObject().putString("prefix", "/echo");
		sockJSServer.installApp(config, new Handler<SockJSSocket>() {
		    public void handle(SockJSSocket sock) {
		    	AppLogger.debug("SockJS: start pump : %s",sock.toString());
		        Pump.createPump(sock, sock).start();
		    }
		});
		
		server.listen(Integer.valueOf(c.getInt("vertx.http.port", 8080)));
	}

	private HttpClientRequest getRequest(final HttpServerRequest req, final HttpClient client) {
		final HttpClientRequest cReq = client.request(req.method(), req.uri(), new Handler<HttpClientResponse>() {
			public void handle(final HttpClientResponse cRes) {
				AppLogger.debug("Proxying response: %s", cRes.statusCode());
				req.response().setStatusCode(cRes.statusCode());
				req.response().headers().set(cRes.headers());				
				req.response().setChunked(true);
				cRes.dataHandler(new Handler<Buffer>() {
					public void handle(Buffer data) {
						req.response().write(data);
					}
				});
				cRes.endHandler(new VoidHandler() {
					public void handle() {
						req.response().end();
					}
				});
			}
		});
		return cReq;
	}
}