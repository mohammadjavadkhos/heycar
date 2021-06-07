package services.heycar.dealer.integration.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import services.heycar.dealer.integration.entity.Listing;

import static org.springframework.data.relational.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingCustomRepository {

    private final R2dbcEntityTemplate template;

    public Flux<Listing> search(
            String dealerId,
            String make,
            String model,
            Integer year,
            String color
    ) {
        Criteria criteria = where("dealerId").is(dealerId);

        if (make != null)
            criteria = criteria.and(where("make").like("%" + make + "%"));
        if (model != null)
            criteria = criteria.and(where("model").like("%" + model + "%"));
        if (year != null)
            criteria = criteria.and(where("year").is(year));
        if (color != null)
            criteria = criteria.and(where("color").is(color));

        return this.template.select(Listing.class)
                .matching(Query.query(criteria))
                .all();
    }
}