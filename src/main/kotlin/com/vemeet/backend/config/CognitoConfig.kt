package com.vemeet.backend.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "aws.cognito")
class CognitoConfig {

    private  var cognitoRegion: String = "eu-central-1"

    @Value("\${aws.accessKey}")
    private lateinit var cognitoAccessKey: String

    @Value("\${aws.secretKey}")
    private lateinit var cognitoSecretKey: String

    @Bean
    fun cognitoClient(): AWSCognitoIdentityProvider {
        val credentials = BasicAWSCredentials(cognitoAccessKey, cognitoSecretKey)
        return AWSCognitoIdentityProviderClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(cognitoRegion)
            .build()
    }
}