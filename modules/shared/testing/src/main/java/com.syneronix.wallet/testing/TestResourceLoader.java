package com.syneronix.wallet.testing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestResourceLoader {

    public static String getFileContentAsString(String resourceName) throws IOException, URISyntaxException {

        URL resourceUrl = TestResourceLoader.class.getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            throw new FileNotFoundException("File not found: " + resourceName);
        }
        Path path = Paths.get(resourceUrl.toURI());
        return Files.readString(path);
    }

    public static InputStream getFileAsInputStream(String resourceName) {
        return TestResourceLoader.class.getClassLoader().getResourceAsStream(resourceName);
    }
}
