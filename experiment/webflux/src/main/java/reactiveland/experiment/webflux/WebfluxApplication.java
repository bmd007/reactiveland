package reactiveland.experiment.webflux;

import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@RestController
@SpringBootApplication
public class WebfluxApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebfluxApplication.class.getName());
    private static final Base64.Encoder BASE64 = Base64.getEncoder();
    private static MessageDigest messageDigest;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        SpringApplication.run(WebfluxApplication.class, args);
        messageDigest = MessageDigest.getInstance("SHA-256");
    }

    record PossibleFileSizeRequestBody(UUID id) {
    }

    record PossibleFileSizeResponseBody(long gZipSize, long hashSize) {
    }

    @PostMapping(value = "/")
    Mono<PossibleFileSizeResponseBody> possibleFileSize(@RequestBody Mono<PossibleFileSizeRequestBody> requestBodyMono) throws IOException {
        return requestBodyMono
                .map(requestBody -> messageDigest.digest(requestBody.id.toString().getBytes(StandardCharsets.UTF_8)))
                .doOnNext(hash -> LOGGER.info("hash value in base64 {}", BASE64.encodeToString(hash)))
                .publishOn(Schedulers.boundedElastic())
                .map(hash -> {
                    Path tempFile = null;
                    try {
                        tempFile = Files.createTempFile("servlet", "experiment");
                        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {
							final FileChannel outChannel = outputStream.getChannel();
							outChannel.close();
							outputStream.close();
                            gzipOutputStream.write(hash);
                            Metrics.counter("experiment.servlet.handled.request.counter").increment();
                            return new PossibleFileSizeResponseBody(tempFile.length(), hash.length);
                        } catch (Exception e) {
                            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        tempFile.toFile().delete();
                    }
                });
    }
}
