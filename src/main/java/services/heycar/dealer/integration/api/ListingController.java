package services.heycar.dealer.integration.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import services.heycar.dealer.integration.entity.Listing;
import services.heycar.dealer.integration.service.ListingService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/listing")
@AllArgsConstructor
public class ListingController {

    ListingService listingService;

    @GetMapping(value = "/search/{dealerId}")
    Mono<ResponseEntity<ListingResponseDto>> search(
            @PathVariable String dealerId,
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String color
    ) {
        return listingService.search(dealerId, make, model, year, color)
                .map(ListingDto::from)
                .collect(Collectors.toList())
                .map(ListingResponseDto::new)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/upload_csv/{dealerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ListingResponseDto>> uploadCsv(
            @PathVariable String dealerId,
            @RequestPart Part listing
    ) {
        return DataBufferUtils
                .join(listing.content(), 5000)
                .map(data -> {
                    byte[] bytes = new byte[data.readableByteCount()];
                    data.read(bytes);
                    DataBufferUtils.release(data);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .map(it -> getListing(it.lines(), dealerId))
                .delayUntil(it -> listingService.saveAll(it))
                .map(it -> ListingResponseDto.builder()
                        .listings(it.stream().map(ListingDto::from).collect(Collectors.toList())).build())
                .map(ResponseEntity::ok);
    }

    @PostMapping("/vehicle_listings/{dealerId}")
    public Mono<ResponseEntity<ListingResponseDto>> uploadListing(
            @PathVariable String dealerId,
            @RequestBody ListingRequestDto request
    ) {
        return listingService.saveAll(
                request.getListings()
                        .stream()
                        .map(it -> ListingDto.toListing(it, dealerId))
                        .collect(Collectors.toList())
        )
                .map(ListingDto::from)
                .collectList()
                .map(ListingResponseDto::new)
                .map(ResponseEntity::ok);
    }

    private List<Listing> getListing(Stream<String> listings, String dealerId) {
        return listings
                .skip(1)
                .map(it -> {
                    //code, make/model, power-in-ps, year, color, price
                    var parts = it.split(",");
                    return Listing.builder()
                            .code(parts[0])
                            .make(parts[1].split("/")[0])
                            .model(parts[1].split("/")[1])
                            .power(Integer.parseInt(parts[2]))
                            .year(Integer.parseInt(parts[3]))
                            .color(parts[4])
                            .price(Integer.parseInt(parts[5]))
                            .dealerId(dealerId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Value
    @Builder
    public static class ListingRequestDto {
        List<ListingDto> listings;
    }

    @Value
    @Builder
    public static class ListingResponseDto {
        List<ListingDto> listings;
    }

    @Value
    @Builder
    public static class ListingDto {
        Long id;

        String dealerId;

        String code;

        String make;

        String model;

        Integer kw;

        Integer year;

        String color;

        Integer price;

        public static ListingDto from(Listing listing) {
            return ListingDto.builder()
                    .code(listing.getCode())
                    .color(listing.getColor())
                    .dealerId(listing.getDealerId())
                    .id(listing.getId())
                    .make(listing.getMake())
                    .model(listing.getModel())
                    .kw(listing.getPower())
                    .price(listing.getPrice())
                    .year(listing.getYear())
                    .build();
        }

        public static Listing toListing(ListingDto listing, String dealerId) {
            return Listing.builder()
                    .code(listing.getCode())
                    .color(listing.getColor())
                    .dealerId(dealerId)
                    .id(listing.getId())
                    .make(listing.getMake())
                    .model(listing.getModel())
                    .power(listing.getKw())
                    .price(listing.getPrice())
                    .year(listing.getYear())
                    .build();
        }
    }
}
