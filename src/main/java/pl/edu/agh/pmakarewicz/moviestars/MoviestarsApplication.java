package pl.edu.agh.pmakarewicz.moviestars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class MoviestarsApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MoviestarsApplication.class);
        try {
            app.setDefaultProperties(Collections
                    .singletonMap("server.port", System.getenv("PORT")));
        } finally {
            app.run(args);
        }

    }

}
