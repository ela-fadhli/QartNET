package tn.enicarthage.qartnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class QartnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(QartnetApplication.class, args);
    }

}
