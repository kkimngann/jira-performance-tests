package com.atlassian.performance.tools.jiraperformancetests.api

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.STSAssumeRoleWithWebIdentitySessionCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.support.AWSSupportClientBuilder
import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.StorageLocation
import com.atlassian.performance.tools.aws.api.SupportCapacityMediator
import com.atlassian.performance.tools.awsinfrastructure.api.DatasetCatalogue
import com.atlassian.performance.tools.infrastructure.api.app.AppSource
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.io.api.dereference
import com.atlassian.performance.tools.jirasoftwareactions.api.JiraSoftwareScenario
import org.apache.http.message.BasicHeaderValueParser.INSTANCE
import org.apache.http.message.BasicHeaderValueParser.parseHeaderElement
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.*

class AppImpactTestSmokeTest {

    private val roleArn = "arn:aws:iam::695067801333:role/jpt-bitbucket-pipelines"

    /**
     * Don't bump the timeout. If it times out, make it quicker.
     */
    @Test(timeout = 27 * 60 * 1000L)
    fun shouldRun() {
        val region = Regions.US_EAST_1
        val credentialsProvider = AWSCredentialsProviderChain(
            STSAssumeRoleWithWebIdentitySessionCredentialsProvider.Builder(
                roleArn,
                UUID.randomUUID().toString(),
                Paths.get(System.getenv("HOME"), "web-identity-token").toString()
            ).withStsClient(
                AWSSecurityTokenServiceClientBuilder.standard().withRegion(region).build()
            ).build(),
            DefaultAWSCredentialsProviderChain()
        )
        val support = AWSSupportClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()
        val test = AppImpactTest(
            app = MarketplaceUriApp(
                uri = URI("https://marketplace.atlassian.com/download/apps/1217110/version/1000051"),
                label = "Jira training app"
            ),
            aws = Aws.Builder(region)
                .credentialsProvider(credentialsProvider)
                .capacity(
                    SupportCapacityMediator(
                        support = support,
                        region = region
                    )
                )
                .batchingCloudformationRefreshPeriod(Duration.ofMinutes(1))
                .regionsWithHousekeeping(listOf(Regions.US_EAST_1))
                .build(),
            testJar = dereference("jpt.virtual-users.shadow-jar")
        )
        test.scenario = JiraSoftwareScenario::class.java
        test.duration = Duration.ofMinutes(1)
        test.productDistribution = PublicJiraSoftwareDistribution("7.5.0")
        test.dataset = DatasetCatalogue().custom(
            label = "700 issues",
            location = StorageLocation(
                uri = URI.create("s3://jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/dataset-919767fe-55b5-4c06-a3f4-c1d8222b6a2d"),
                region = Regions.EU_WEST_1
            )
        )

        test.run()
    }
}

private class MarketplaceUriApp(
    private val uri: URI,
    private val label: String
) : AppSource {

    override fun acquireFiles(
        directory: File
    ): List<File> = listOf(downloadToDirectory(directory))

    private fun downloadToDirectory(
        directory: File
    ): File {
        val url = uri.toURL()
        val connection = url.openConnection()
        connection.connect()
        val contentDisposition = connection.getHeaderField("Content-Disposition")!!
        val attachmentFilename = parseHeaderElement(contentDisposition, INSTANCE)
            .getParameterByName("filename")
            .value
        val file = directory.resolve(attachmentFilename)
        url.openStream().use { Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING) }
        return file
    }

    override fun getLabel(): String = label
}
