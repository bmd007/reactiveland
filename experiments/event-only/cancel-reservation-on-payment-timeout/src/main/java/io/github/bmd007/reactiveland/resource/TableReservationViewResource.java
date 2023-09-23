package io.github.bmd007.reactiveland.resource;

import io.github.bmd007.reactiveland.dto.TableReservationDto;
import io.github.bmd007.reactiveland.dto.TableReservationsDto;
import io.github.bmd007.reactiveland.service.TableReservationViewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static io.github.bmd007.reactiveland.service.ViewService.HIGH_LEVEL_QUERY_PARAM_NAME;

@RestController
@RequestMapping("/api/tables/reservations")
public class TableReservationViewResource {

    private final TableReservationViewService tableReservationViewService;

    public TableReservationViewResource(TableReservationViewService tableReservationViewService) {
        this.tableReservationViewService = tableReservationViewService;
    }

    //isHighLevelQuery query param is related to inter instance communication, it should be true in normal operations or not defined
    @GetMapping
    public Mono<TableReservationsDto> getLikes(@RequestParam(required = false, value = HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return tableReservationViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/{customerId}")
    public Mono<TableReservationDto> getLikesByWonderSeekerName(@PathVariable String customerId) {
        return tableReservationViewService.getById(customerId)
                .switchIfEmpty(
                        Mono.error(
                            new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    String.format("%s not found (%s doesn't exist).", "Table reservations", customerId))));
    }
}
