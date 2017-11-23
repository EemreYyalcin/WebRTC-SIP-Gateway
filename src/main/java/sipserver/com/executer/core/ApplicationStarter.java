package sipserver.com.executer.core;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

import sipserver.com.server.transport.ws.ProgrammaticEchoEnpoint;

@SpringBootApplication
public class ApplicationStarter {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationStarter.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("commandLineRunner 0");
			ServerCore.gettinStarted(null);
			System.out.println("commandLineRunner 1");
		};
	}

	// This bean is automatically picked up by ServerEndpointExporter
	@Bean
	public ServerEndpointRegistration addEndpointRegistration() {
		return new ServerEndpointRegistration("/echo", new ProgrammaticEchoEnpoint());
	}

	// @ServerEndpoint
	@Bean
	public ServerEndpointExporter endpointExporter() {
		return new ServerEndpointExporter();
	}

}
