package io.github.bmd007.reactiveland.service;

import io.github.bmd007.reactiveland.configuration.StateStores;
import io.github.bmd007.reactiveland.domain.TableReservation;
import io.github.bmd007.reactiveland.dto.TableReservationDto;
import io.github.bmd007.reactiveland.dto.TableReservationsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class TableReservationViewService extends ViewService<TableReservationsDto, TableReservationDto, TableReservation> {

    private static final Function<TableReservationsDto, List<TableReservationDto>> LIST_EXTRACTOR = TableReservationsDto::tableReservations;
    private static final Function<List<TableReservationDto>, TableReservationsDto> LIST_WRAPPER = TableReservationsDto::new;
    private static final BiFunction<String, TableReservation, TableReservationDto> DTO_MAPPER = (customerId, tableReservation) ->
                    new TableReservationDto(tableReservation.getCustomerId(), tableReservation.getTableId(), tableReservation.getStatus().toString());

    public TableReservationViewService(StreamsBuilderFactoryBean streams,
                                       @Value("${kafka.streams.server.config.app-ip}") String ip,
                                       @Value("${kafka.streams.server.config.app-port}") int port,
                                       ViewResourcesClient commonClient) {
        super(ip, port, streams, StateStores.RESERVATION_STATUS_IN_MEMORY_STATE_STORE,
                TableReservationsDto.class, TableReservationDto.class,
                DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "/api/tables/reservations", commonClient);
    }
}
