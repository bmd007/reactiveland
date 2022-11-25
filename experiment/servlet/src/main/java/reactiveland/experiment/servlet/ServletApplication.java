package reactiveland.experiment.servlet;

import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@RestController
@SpringBootApplication
public class ServletApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletApplication.class.getName());
    private static final Base64.Encoder BASE64 = Base64.getEncoder();
    private static MessageDigest messageDigest;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        SpringApplication.run(ServletApplication.class, args);
        messageDigest = MessageDigest.getInstance("SHA-256");
    }

    record PossibleFileSizeRequestBody(UUID id) {
    }

    record PossibleFileSizeResponseBody(long gZipSize, long hashSize) {
    }

    @PostMapping(value = "/")
    PossibleFileSizeResponseBody possibleFileSize(@RequestBody PossibleFileSizeRequestBody requestBody) throws IOException {
        var hash = messageDigest.digest(requestBody.id.toString().getBytes(StandardCharsets.UTF_8));
        LOGGER.info("hash value in base64 {}", BASE64.encodeToString(hash));
        File tempFile = File.createTempFile("servlet", "experiment");
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {
            gzipOutputStream.write(hash);
            Metrics.counter("experiment.servlet.handled.request.counter").increment();
            return new PossibleFileSizeResponseBody(tempFile.length(), hash.length);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED);
        } finally {
            tempFile.delete();
        }
    }
}
