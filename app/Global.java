import play.*;
import play.mvc.*;
import play.libs.F.*;

public class Global extends GlobalSettings {

  public Promise<SimpleResult> onHandlerNotFound(Http.RequestHeader request) {
    // TODO
    return null;
  }

}