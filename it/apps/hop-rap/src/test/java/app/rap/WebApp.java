package app.rap;

import org.apache.hop.ui.hopgui.HopWebServletContextListener;
import org.apache.hop.www.HopServerServlet;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;

@SpringBootApplication
public class WebApp extends SpringBootServletInitializer {

  @Override
  public void onStartup(ServletContext servletContext) {
    servletContext.setInitParameter(
        "org.eclipse.rap.applicationConfiguration", "org.apache.hop.ui.hopgui.HopWeb");
    servletContext.addListener(HopWebServletContextListener.class);
    servletContext.addServlet("HopGui", RWTServlet.class)
            .addMapping("/ui");
    servletContext
        .addServlet("welcome", "/docs/English/welcome/index.html")
        .addMapping("/docs/English/welcome/index.html");
    servletContext.addServlet("Server", HopServerServlet.class).addMapping("/hop/*");
//    servletContext.addJspFile("*", "/index.jsp").addMapping("/*");
  }

  public static void main(String[] args) {
    SpringApplication.run(WebApp.class, args);
  }

}
