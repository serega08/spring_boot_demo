package com.example;

import com.example.domain.User;
import com.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Random;

@SpringBootApplication
@EnableCaching
public class DemoApplication {

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner prepareData(UserRepository userRepository) {
        return strings -> {
            Arrays.asList("Bill, John, Serega, Anton, Sasha".split(","))
                    .forEach(name -> userRepository.save(new User(name)));

            userRepository.findAll().forEach(System.out::println);
        };
    }

    @Bean
    public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
        return container -> container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/404.html"));
    }

    @Bean
    HealthIndicator healthIndicator(){
        return ()->{
            if (new Random().nextBoolean()){
                return Health.up().build();
            }else{
                return Health.down()
                        .withDetail("pam-pam-pam", 21)
                        .down()
                        .build();
            }

        };
    }
}

@RestController
class HelloController{

    @Autowired
    CounterService counterService;

    @Autowired
    NameGeneratorService nameGeneratorService;

    private void logCounter(String name){
        counterService.increment("magic.metrics." + name);
    }

    @RequestMapping("/hello/{name}")
    public String sayHello(@PathVariable String name){
        logCounter(name);

        return "Hello there " + nameGeneratorService.getRandomNames(name);
    }
}

@Service
class NameGeneratorService {

    @Cacheable("names")
    public String getRandomNames(String name){
       return name + " " + Math.random();
    }

}
