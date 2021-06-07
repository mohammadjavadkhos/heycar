package services.heycar.dealer.integration.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import services.heycar.dealer.integration.entity.Listing;
import services.heycar.dealer.integration.repository.ListingCustomRepository;
import services.heycar.dealer.integration.repository.ListingRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ListingService {

    ListingRepository listingRepository;

    ListingCustomRepository listingCustomRepository;

    public Flux<Listing> saveAll(List<Listing> listings) {
        return Flux.fromIterable(listings)
                .flatMap(listing -> listingRepository.findByDealerIdAndCode(listing.getDealerId(), listing.getCode())
                        .flatMap(item -> listingRepository.save(listing.toBuilder().id(item.getId()).build()))
                        .switchIfEmpty(listingRepository.save(listing)));
    }

    public Flux<Listing> search(
            String dealerId,
            String make,
            String model,
            Integer year,
            String color
    ) {
        return listingCustomRepository.search(dealerId, make, model, year, color);
    }
}
