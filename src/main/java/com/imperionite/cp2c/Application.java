package com.imperionite.cp2c;

import com.imperionite.cp2c.config.AppConfigurator;
import io.javalin.Javalin;

public class Application {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;

            // Enable CORS for specified origins only
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.allowHost("http://localhost:5173");
                    rule.allowHost("http://127.0.0.1:5173");
                    // rule.allowHost("https://myapp.com");
                    // Add more allowed origins as needed:
                    // rule.allowHost("https://anotherdomain.com");
                    rule.allowCredentials = true;
                });
            });
        }).start(4567);

        AppConfigurator configurator = new AppConfigurator();
        configurator.configureAndStart(app);

        app.events(event -> event.serverStopping(() -> {
            System.out.println("Application: Shutting down...");
        }));
    }
}
