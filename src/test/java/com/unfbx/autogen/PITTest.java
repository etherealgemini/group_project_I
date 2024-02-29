package com.unfbx.autogen;

import com.autogen.service.EvaluationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.FileUtils.getClassesFiles;
import static com.autogen.utils.IOUtils.getPropertiesString;
import static com.unfbx.autogen.GeneralTest.loadPathProperties;

public class PITTest {
    private static HashMap<String,String> systemProperties = new HashMap<>();

    @Before
    public void init() {
        loadPathProperties(systemProperties);
    }

    @Test
    public void getClassesTest() {
        File[] files = getClassesFiles(systemProperties.get("testPath"));

        StringBuilder s = null;
        if (files != null) {
            s = new StringBuilder();
            for (File file : files) {
                String t = file.getName();
                s.append(t, 0, t.length() - 6);
            }
        }

        System.out.println(s);
    }

    @Test
    public void PITCMDTest() throws Exception {
        EvaluationService.getInstance(systemProperties).evaluateTest(201);
    }



}
