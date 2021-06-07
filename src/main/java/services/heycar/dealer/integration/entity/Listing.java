package services.heycar.dealer.integration.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table(value = "Listing")
public class Listing {

    @Id
    Long id;

    String dealerId;

    String code;

    String make;

    String model;

    Integer power;

    Integer year;

    String color;

    Integer price;
}
