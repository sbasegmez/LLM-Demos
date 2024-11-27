package com.developi.jnx.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class Utils {

    public static void initDotenv() {
        // This is first time. It will load the .env file.
        Dotenv dotenv = Dotenv.configure()
                              .directory(System.getProperty("user.home"))
                              .filename(".env")
                              .ignoreIfMalformed()
                              .ignoreIfMissing()
                              .load();

        // But we don't want to use dotenv any more. So we will just dump everything to system properties.
        dotenv.entries()
              .forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

}
