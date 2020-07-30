package main;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath ("/resources")
public class RESTApplication extends Application{
   private Set<Object> singletons = new HashSet<Object>();

   public RESTApplication() {
      singletons.add(new GereGuias());
      singletons.add(new GereUtilizadores());
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }    
    
}
