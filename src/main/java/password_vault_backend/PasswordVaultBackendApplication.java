package password_vault_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PasswordVaultBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasswordVaultBackendApplication.class, args);
	}

}
