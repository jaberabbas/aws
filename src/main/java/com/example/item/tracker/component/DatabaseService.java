
package com.example.item.tracker.component;

import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import com.example.item.tracker.model.User;
import com.example.item.tracker.model.WorkItem;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        String secretName = "dev/postgres/connection";

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretClient.getSecretValue(valueRequest);
        return valueResponse.secretString();
    }

    // Set the specified item to archive.
    public void flipItemArchive(String id) throws CustomException {
        Connection c = null;
        String query;
        // Get the Amazon RDS credentials from AWS Secrets Manager.
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);
        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            query = "update work set archive = ? where idwork ='" + id + "' ";
            assert c != null;
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
    public List<WorkItem> getItemsDataSQLReport(int flag) throws CustomException {
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
            if (flag == 0) {
                // Retrieves active data database
                int arch = 0;
                query = "Select idwork,username,date,description,guide,status,archive FROM work where archive=?;";
                assert c != null;
                pstmt = c.prepareStatement(query);
                //pstmt.setString(1, username);
                pstmt.setInt(1, arch);
                rs = pstmt.executeQuery();
            } else if (flag == 1) {
                // Retrieves archive data from database
                int arch = 1;
                query = "Select idwork,username,date,description,guide,status,archive  FROM work where archive=?;";
                assert c != null;
                pstmt = c.prepareStatement(query);
                //pstmt.setString(1, username);
                pstmt.setInt(1, arch);
                rs = pstmt.executeQuery();
            } else {
                // Retrieves all data from database
                query = "Select idwork,username,date,description,guide,status, archive FROM work";
                assert c != null;
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

    // Inject a new submission.
    public void injectNewSubmission(WorkItem item) throws CustomException {
        Connection c = null;
        // Get the Amazon RDS credentials from AWS Secrets Manager.
        Gson gson = new Gson();
        User user = gson.fromJson(String.valueOf(getSecretValues()), User.class);

        try {
            c = connectionHelper.getConnection(user.getHost(), user.getUsername(), user.getPassword());
            PreparedStatement ps;

            // Convert rev to int.
            String name = item.getName();
            String guide = item.getGuide();
            String description = item.getDescription();
            String status = item.getStatus();

            // Date conversion.
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String sDate1 = dtf.format(now);
            Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse(sDate1);
            java.sql.Date sqlDate = new java.sql.Date(date1.getTime());

            // Inject an item into the system.
            String insert = "INSERT INTO work (username,date,description, guide, status, archive) VALUES(?, ?,?,?,?,?);";
            assert c != null;
            ps = c.prepareStatement(insert);
            //ps.setString(1, workId);
            ps.setString(1, name);
            ps.setDate(2, sqlDate);
            ps.setString(3, description);
            ps.setString(4, guide);
            ps.setString(5, status);
            ps.setInt(6, 0);
            ps.execute();
        } catch (SQLException | ParseException e) {
            log.error("inject new item failed. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "", "inject new item failed: " + e.getMessage());
        } finally {
            connectionHelper.close(c);
        }
    }
}
