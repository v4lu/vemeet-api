package com.vemeet.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config {

    @Value("\${aws.accessKey}")
    lateinit var accessKeyId: String

    @Value("\${aws.secretKey}")
    lateinit var secretAccessKey: String

    @Value("\${aws.region}")
    lateinit var region: String

    @Bean
    fun s3Client(): S3Client {
        val awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .region(Region.of(region))
            .build()
    }
}