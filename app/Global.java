import play.*;
import play.mvc.*;

public class Global extends GlobalSettings {

  public SimpleResult onHandlerNotFound(Http.RequestHeader request) {
    System.out.println(Http.Context.current());
    return null;
  }

}