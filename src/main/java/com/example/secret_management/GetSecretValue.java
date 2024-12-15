package com.example.secret_management;

import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

public class GetSecretValue {
    public static void main(String[] args) {
        String secretName = "dev/sql/pass";
        Region region = Region.EU_WEST_3;
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();

        Gson gson = new Gson();
        String secret = getValue(secretsClient, secretName);
        if (secret != null) {
            User user = gson.fromJson(String.valueOf(secret), User.class);
            System.out.println("object: " + user.getUsername() + " " + user.getPassword() + " " + user.getHost());
        }
        secretsClient.close();
    }

    public static String getValue(SecretsManagerClient secretsClient, String secretName) {
        String secret = null;
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            secret = valueResponse.secretString();
            System.out.println("json format: " + secret);

        } catch (SecretsManagerException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return secret;
    }
}

