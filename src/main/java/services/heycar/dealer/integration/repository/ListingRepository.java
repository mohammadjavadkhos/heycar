package services.heycar.dealer.integration.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import services.heycar.dealer.integration.entity.Listing;


public interface ListingRepository extends ReactiveCrudRepository<Listing, String> {
    Mono<Listing> findByDealerIdAndCode(String dealerId, String code);
}
