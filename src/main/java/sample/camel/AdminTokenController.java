package sample.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AdminTokenController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "/tokens/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAdminToken() throws InterruptedException {

        Thread.sleep(5000);
        String uuid = UUID.randomUUID().toString();

        logger.info(String.format("New token generated: [%s]", uuid));
        return uuid;
    }
}
