
package com.example.item.tracker.component;

import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import com.example.item.tracker.model.User;
import com.example.item.tracker.model.WorkItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class DatabaseService {

    private final ConnectionHelper connectionHelper;

    public DatabaseService(ConnectionHelper connectionHelper) {
        this.connectionHelper = connectionHelper;
    }

    private SecretsManagerClient getSecretClient() {
        Region region = Region.EU_WEST_3;
        return SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create()) // Automatically uses appropriate credentials
                .build();
    }

    private String getSecretValues() {
        // Get the Amazon RDS creds from Secrets Manager.
        SecretsManagerClient secretClient = getSecretClient();
        String secretName = "dev/postgres/connectiondb";

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretClient.getSecretValue(valueRequest);
        return valueResponse.secretString();
    }

    // Set the specified item to archive.
    public void flipItemArchive(int id) throws CustomException {
        Connection c = null;
        String query;
        // Get the Amazon RDS credentials from AWS Secrets Manager.
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);
        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            query = "update work set archive = ? where idwork ='" + id + "' ";
            PreparedStatement updateForm = c.prepareStatement(query);
            updateForm.setInt(1, 1);
            updateForm.execute();

        } catch (SQLException e) {
            log.error("flipItemArchive failed. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "flipItemArchive failed: ", e.getMessage());
        } finally {
            connectionHelper.close(c);
        }
    }

    // Get Items data from postgress.
    public List<WorkItem> getItemsDataSQLReport(String archived) throws CustomException {
        Connection c = null;
        List<WorkItem> itemList = new ArrayList<>();
        String query;
        //String username = "user";
        WorkItem item;

        // Get the Amazon RDS credentials from AWS Secrets Manager.
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);
        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            if (archived != null && archived.trim().equalsIgnoreCase("false")) {
                // Retrieves active data database
                int arch = 0;
                query = "Select idwork,username,date,description,guide,status,archive FROM work where archive=?;";
                pstmt = c.prepareStatement(query);
                //pstmt.setString(1, username);
                pstmt.setInt(1, arch);
                rs = pstmt.executeQuery();
            } else if (archived != null && archived.trim().equalsIgnoreCase("true")) {
                // Retrieves archive data from database
                int arch = 1;
                query = "Select idwork,username,date,description,guide,status,archive  FROM work where archive=?;";
                pstmt = c.prepareStatement(query);
                pstmt.setInt(1, arch);
                rs = pstmt.executeQuery();
            } else {
                // Retrieves all data from database
                query = "Select idwork,username,date,description,guide,status, archive FROM work";
                pstmt = c.prepareStatement(query);
                rs = pstmt.executeQuery();
            }

            while (rs.next()) {
                item = new WorkItem();
                item.setId(rs.getString(1));
                item.setName(rs.getString(2));
                item.setDate(rs.getDate(3).toString().trim());
                item.setDescription(rs.getString(4));
                item.setGuide(rs.getString(5));
                item.setStatus(rs.getString(6));
                item.setArchived(rs.getBoolean(7));

                // Push the WorkItem Object to the list.
                itemList.add(item);
            }
            return itemList;

        } catch (SQLException e) {
            log.error("getItemsDataSQLReport failed. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "getItemsDataSQLReport failed: ", e.getMessage());
        } finally {
            connectionHelper.close(c);
        }
    }

    public WorkItem getItemDataSQLReport(int id) throws CustomException {
        Connection c = null;
        String query;
        WorkItem workItem = null;
        // Get the Amazon RDS credentials from AWS Secrets Manager.
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);
        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            query = "Select idwork,username,date,description,guide,status,archive FROM work where idwork=?;";
            pstmt = c.prepareStatement(query);
            //pstmt.setString(1, username);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                workItem = new WorkItem();
                workItem.setId(rs.getString(1));
                workItem.setName(rs.getString(2));
                workItem.setDate(rs.getDate(3).toString().trim());
                workItem.setDescription(rs.getString(4));
                workItem.setGuide(rs.getString(5));
                workItem.setStatus(rs.getString(6));
                workItem.setArchived(rs.getBoolean(7));

            }
            return workItem;

        } catch (SQLException e) {
            log.error("getItemDataSQLReport failed. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "getItemsDataSQLReport failed: ", e.getMessage());
        } finally {
            connectionHelper.close(c);
        }
    }

    // Inject a new submission.
    public WorkItem injectNewSubmission(WorkItem item) throws CustomException {
        Connection c = null;
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);
        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now());

            // Prepare SQL
            String insertQuery = "INSERT INTO work (username, date, description, guide, status, archive) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, item.getName());
                ps.setDate(2, sqlDate);
                ps.setString(3, item.getDescription());
                ps.setString(4, item.getGuide());
                ps.setString(5, item.getStatus());
                ps.setInt(6, 0);

                ps.executeUpdate();

                // Retrieve generated keys (e.g., ID)
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(String.valueOf(generatedKeys.getLong(1)));
                    } else {
                        throw new CustomException(ErrorCodes.TEC001.getCode(), "No ID returned", "", "Failed to retrieve generated ID.");
                    }
                }
            }

            // Return the updated WorkItem with the generated ID
            return item;

        } catch (SQLException e) {
            log.error("inject new item failed.", e);
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "", "SQL Error: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse secret values.", e);
            throw new CustomException(ErrorCodes.TEC001.getCode(), "JSON parsing error", "", e.getMessage());
        } finally {
            connectionHelper.close(c);
        }
    }

}
