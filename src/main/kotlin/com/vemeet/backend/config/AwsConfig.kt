package com.vemeet.backend.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.AWSKMSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
@ConfigurationProperties(prefix = "aws.cognito")
class AwsConfig {

    @Value("\${aws.region}")
    private lateinit var awsRegion: String

    @Value("\${aws.accessKey}")
    private lateinit var awsAccessKey: String

    @Value("\${aws.secretKey}")
    private lateinit var awsSecretKey: String


    @Bean
    fun cognitoClient(): AWSCognitoIdentityProvider {
        val credentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
        return AWSCognitoIdentityProviderClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(awsRegion)
            .build()
    }

    @Bean
    fun s3Client(): S3Client {
        val awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .region(Region.of(awsRegion))
            .build()
    }

    @Bean
    fun kmsClient(): AWSKMS {
        val credentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
        return AWSKMSClientBuilder.standard()
            .withRegion(awsRegion)
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .build()
    }
}