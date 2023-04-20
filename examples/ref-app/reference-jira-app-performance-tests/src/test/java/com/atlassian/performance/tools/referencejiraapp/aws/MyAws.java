package com.atlassian.performance.tools.referencejiraapp.aws;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleWithWebIdentitySessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.atlassian.performance.tools.aws.api.Aws;
import com.atlassian.performance.tools.jiraperformancetests.api.ExplainingAwsCredentialsProvider;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MyAws {

    /**
     * This is important.
     * Until https://ecosystem.atlassian.net/browse/JPERF-235 is fixed, you need to set up periodic AWS housekeeping.
     * E.g. run <tt>./mvnw -pl reference-jira-app-performance-tests test-compile exec:java@clean-all-expired</tt>
     * from the <tt>examples/ref-app</tt> directory.
     * When you set up this periodic housekeeping (e.g. a cronjob or a scheduled CI), add the covered region here.
     */
    private final Regions[] regionsWithHousekeeping = {
        Regions.US_EAST_1
    };
    private final String roleArn = "arn:aws:iam::695067801333:role/jpt-bitbucket-pipelines";
    private final Regions region = Regions.US_EAST_1;

    /**
     * The following AWS regions are not yet supported: EU_WEST_3, SA_EAST_1, GovCloud
     */
    public final Aws aws = new Aws.Builder(region)
        .credentialsProvider(
            new AWSCredentialsProviderChain(
                new STSAssumeRoleWithWebIdentitySessionCredentialsProvider.Builder(
                    roleArn,
                    UUID.randomUUID().toString(),
                    Paths.get(System.getenv("HOME"), "web-identity-token").toString()
                ).withStsClient(
                    AWSSecurityTokenServiceClientBuilder.standard().withRegion(region).build()
                ).build(),
                new DefaultAWSCredentialsProviderChain(),
                new ExplainingAwsCredentialsProvider(MyAws.class)
            )
        )
        .regionsWithHousekeeping(listRegionsWithHousekeeping())
        .batchingCloudformationRefreshPeriod(Duration.ofSeconds(20))
        .build();

    private List<Regions> listRegionsWithHousekeeping() {
        String override = System.getProperty("jpt.housekeeping.protection.override");
        if (override != null) {
            return Collections.singletonList(Regions.fromName(override));
        } else {
            return Arrays.asList(regionsWithHousekeeping);
        }
    }
}
