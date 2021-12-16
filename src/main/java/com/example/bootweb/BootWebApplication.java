package com.example.bootweb;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

@SpringBootApplication
@CommonsLog
public class BootWebApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootWebApplication.class, args);
  }

//  @Bean
//  public ApplicationRunner runner(UserService service) {
//    return args -> {
//      service.register(new RegistrationRequestDto(
//          "admin", "pass"
//      ));
//    };
//  };

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> applicationTomcatCustomizer(
      @Value("${server.port:-1}") int serverPort,
      @Value("${server-ssl.port:-1}") int serverSslPort
  ) {
    return tomcat -> {
      tomcat.addContextCustomizers(context -> {
        final var securityConstraint = new SecurityConstraint();
        securityConstraint.setUserConstraint("CONFIDENTIAL");
        final var collection = new SecurityCollection();
        collection.addPattern("/*");
        securityConstraint.addCollection(collection);
        context.addConstraint(securityConstraint);
      });
      tomcat.addConnectorCustomizers(connector -> {
        if (connector.getPort() == serverPort || connector.getPort() == tomcat.getPort()) {
          connector.setRedirectPort(serverSslPort);
        }
      });
      tomcat.addAdditionalTomcatConnectors(sslConnector(serverSslPort));
    };
  }

  public Connector sslConnector(int serverSslPort) {
    try {
      final var protocol = new Http11NioProtocol();
      final var connector = new Connector(protocol);
      connector.setPort(serverSslPort);
      connector.setScheme("https");
      connector.setSecure(true);

      protocol.setMaxThreads(150);
      protocol.setSSLEnabled(true);
      protocol.addUpgradeProtocol(new Http2Protocol());

      final var sslHostConfig = new SSLHostConfig();
      sslHostConfig.setProtocols("TLSv1.3");

      final var certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
      certificate.setCertificateKeystoreFile(ResourceUtils.getURL("classpath:server.jks").toString());
      certificate.setCertificateKeystorePassword("passphrase");
      sslHostConfig.addCertificate(certificate);

      connector.addSslHostConfig(sslHostConfig);

      return connector;
    } catch (IOException ex) {
      throw new IllegalStateException("Fail to create ssl connector", ex);
    }
  }

}
