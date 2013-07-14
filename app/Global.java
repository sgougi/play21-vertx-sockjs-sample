import java.lang.reflect.Method;
import java.net.URL;

import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Play;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;
import controllers.AppLogger;

public class Global extends GlobalSettings {

	@Override
	public Action onRequest(Request request, Method actionMethod) {
		final Configuration c = Play.application().configuration();
		
		if( request.getHeader("play-vertx-delegate") == null ) {
			final String redirectUrl = "http://" 
					+ request.host().replaceAll(
							":" + c.getInt("http.port", 9000), 
							String.format(":%s%s", c.getInt("vertx.http.port", 8080), request.uri()));
			AppLogger.debug("redirectUrl: %s", redirectUrl);
            return new Action.Simple() {
				@Override
				public Result call(Context arg0) throws Throwable {
                    return redirect(redirectUrl);
				}
            };			
		}

	   return super.onRequest(request, actionMethod);
	}
	
	@Override
	public void onStart(Application app) {
		final Configuration c = Play.application().configuration();
			
		PlatformManager pfm = PlatformLocator.factory.createPlatformManager();
		try {
			URL[] classpath = {};
			pfm.deployVerticle( "verticles.DelegateServer", null, classpath, 
				Integer.valueOf(c.getInt("vertx.instances", 3)), null, null );			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}