package com.vemeet.backend.service

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.*
import com.vemeet.backend.exception.ConfirmationCodeExpiredException
import com.vemeet.backend.exception.EmailAlreadyExistsException
import com.vemeet.backend.exception.InvalidConfirmationCodeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class CognitoService(private val cognitoClient: AWSCognitoIdentityProvider) {

    @Value("\${AWS_COGNITO_USER_POOL}")
    private lateinit var userPoolId: String

    @Value("\${AWS_COGNITO_CLIENT_ID}")
    private lateinit var clientId: String

    @Value("\${AWS_COGNITO_SECRET}")
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

        try {
            return cognitoClient.signUp(request)
        } catch (e: UsernameExistsException) {
            throw EmailAlreadyExistsException("Email already exists in Cognito")
        }
    }

    fun deleteUser(username: String) {
        val request = AdminDeleteUserRequest()
            .withUserPoolId(userPoolId)
            .withUsername(username)

        cognitoClient.adminDeleteUser(request)
    }

    fun confirmSignUp(email: String, confirmationCode: String) {
        val secretHash = calculateSecretHash(email)

        val request = ConfirmSignUpRequest()
            .withClientId(clientId)
            .withUsername(email)
            .withConfirmationCode(confirmationCode)
            .withSecretHash(secretHash)

        try {
            cognitoClient.confirmSignUp(request)
        } catch (e: ExpiredCodeException) {
            throw ConfirmationCodeExpiredException("Confirmation code has expired")
        } catch (e: CodeMismatchException) {
            throw InvalidConfirmationCodeException("Invalid confirmation code")
        } catch (e: UserNotFoundException) {
            throw UserNotFoundException("User not found")
        } catch (e: Exception) {
            throw Exception("Failed to confirm user: ${e.message}")
        }
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

    fun getUserSub(accessToken: String): String {
        val request = GetUserRequest().withAccessToken(accessToken)
        val result = cognitoClient.getUser(request)
        return result.userAttributes.find { it.name == "sub" }?.value
            ?: throw Exception("Unable to find user sub")
    }

    private fun calculateSecretHash(username: String): String {
        val message = username + clientId
        val secretKey = SecretKeySpec(clientSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }

    fun getCognitoUserByEmail(email: String): String {
        val request = AdminGetUserRequest()
            .withUserPoolId(userPoolId)
            .withUsername(email)
        try {
            val result = cognitoClient.adminGetUser(request)
             return result.username // Cognito User ID (sub)
        } catch (e: UserNotFoundException) {
            throw UserNotFoundException("User not found in Cognito")
        } catch (e: Exception) {
            throw Exception("Failed to get Cognito user: ${e.message}")
        }
    }

    fun refreshAccessToken(refreshToken: String, awsId: String): InitiateAuthResult {
        val authParameters = mapOf(
            "REFRESH_TOKEN" to refreshToken,
            "SECRET_HASH" to calculateSecretHash(awsId)
        )

        val request = InitiateAuthRequest()
            .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
            .withClientId(clientId)
            .withAuthParameters(authParameters)

        return cognitoClient.initiateAuth(request)
    }


}