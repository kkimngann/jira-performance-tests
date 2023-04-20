package com.atlassian.performance.tools.btftest;

import com.atlassian.performance.tools.jiraperformancetests.api.OnPremisePerformanceTest;
import com.atlassian.performance.tools.virtualusers.api.browsers.HeadlessChromeBrowser;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public class MyJiraOnPremiseIT {

    /**
     * Defaults to yield results quickly. Needs additional configuration to achieve greater meaningfulness.
     */
    @Test
    public void testMyJira() throws URISyntaxException {

        final String testURI = System.getProperty("testURI");
        final String adminUsername = System.getProperty("adminUsername");
        final String adminPassword = System.getProperty("adminPassword");
        final int numberUsers = Integer.parseInt(System.getProperty("numberUsers"));
        final int durationMinute = Integer.parseInt(System.getProperty("durationMinute"));
        
        /*
         * Point this toward tested Jira.
         */
        final URI myJira = new URI(testURI);

        final OnPremisePerformanceTest jiraOnPremiseTest = new OnPremisePerformanceTest(myJira);

        /*
         * Set credentials so the test knows how to access your jira.
         */
        jiraOnPremiseTest.setAdminLogin(adminUsername);
        jiraOnPremiseTest.setAdminPassword(adminPassword);

        /*
         * Optionally, set the number of virtual users that will generate the load.
         */
        jiraOnPremiseTest.setVirtualUsers(numberUsers);

        /*
         * Optionally, change the test duration.
         */
        jiraOnPremiseTest.setTestDuration(Duration.ofMinutes(durationMinute));

        /*
         * Optionally, customize the browser.
         */
        jiraOnPremiseTest.setBrowser(MyCustomBrowser.class);

        /*
         * Optionally, customize the scenario.
         */
        jiraOnPremiseTest.setScenario(MyCustomScenario.class);

        jiraOnPremiseTest.run();
    }
}
