package com.imperionite.cp2c;

import com.imperionite.cp2c.config.AppConfigurator;
import io.javalin.Javalin;

public class Application {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(4567);

        AppConfigurator configurator = new AppConfigurator();
        configurator.configureAndStart(app);

        app.events(event -> event.serverStopping(() -> {
            System.out.println("Application: Shutting down...");
        }));
    }
}
