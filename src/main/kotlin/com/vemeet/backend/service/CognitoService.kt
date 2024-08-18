package com.vemeet.backend.service

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class CognitoService(private val cognitoClient: AWSCognitoIdentityProvider) {

    @Value("\${aws.cognito.user-pool-id}")
    private lateinit var userPoolId: String

    @Value("\${aws.cognito.client-id}")
    private lateinit var clientId: String

    @Value("\${aws.cognito.client-secret}")
    private lateinit var clientSecret: String

    fun signUp(email: String, password: String, additionalAttributes: Map<String, String>): SignUpResult {
        val userAttributes = additionalAttributes.map { (key, value) ->
            AttributeType().withName(key).withValue(value)
        }.toMutableList()

        userAttributes.add(AttributeType().withName("email").withValue(email))

        val secretHash = calculateSecretHash(email)

        val request = SignUpRequest()
            .withClientId(clientId)
            .withUsername(email)
            .withPassword(password)
            .withUserAttributes(userAttributes)
            .withSecretHash(secretHash)

        return cognitoClient.signUp(request)
    }

    fun confirmSignUp(email: String, confirmationCode: String) {
        val secretHash = calculateSecretHash(email)

        val request = ConfirmSignUpRequest()
            .withClientId(clientId)
            .withUsername(email)
            .withConfirmationCode(confirmationCode)
            .withSecretHash(secretHash)

        cognitoClient.confirmSignUp(request)
    }

    fun initiateAuth(email: String, password: String): InitiateAuthResult {
        val secretHash = calculateSecretHash(email)

        val authParameters = mapOf(
            "USERNAME" to email,
            "PASSWORD" to password,
            "SECRET_HASH" to secretHash
        )

        val request = InitiateAuthRequest()
            .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .withClientId(clientId)
            .withAuthParameters(authParameters)

        return cognitoClient.initiateAuth(request)
    }

    private fun calculateSecretHash(username: String): String {
        val message = username + clientId
        val secretKey = SecretKeySpec(clientSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }
}