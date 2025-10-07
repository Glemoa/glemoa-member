package hyunsub.glemoamember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GlemoaMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlemoaMemberApplication.class, args);
    }
}
