package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render());
    }
  
}
