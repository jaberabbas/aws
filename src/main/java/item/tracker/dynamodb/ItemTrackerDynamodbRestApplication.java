package item.tracker.dynamodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemTrackerDynamodbRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemTrackerDynamodbRestApplication.class, args);
		System.out.println("Hello AWS DynamoDB");
	}

}
