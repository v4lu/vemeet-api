package com.vemeet.backend.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CognitoConfig {

    @Value("\${aws.cognito.region}")
    private lateinit var cognitoRegion: String

    @Value("\${aws.cognito.access-key}")
    private lateinit var cognitoAccessKey: String

    @Value("\${aws.cognito.secret-key}")
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