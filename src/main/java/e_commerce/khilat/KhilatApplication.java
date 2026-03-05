package e_commerce.khilat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching 
public class KhilatApplication {

	public static void main(String[] args) {
		SpringApplication.run(KhilatApplication.class, args);
	}

}
