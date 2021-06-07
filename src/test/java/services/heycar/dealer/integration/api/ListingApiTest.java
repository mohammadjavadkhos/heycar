package services.heycar.dealer.integration.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import services.heycar.dealer.integration.entity.Listing;
import services.heycar.dealer.integration.repository.ListingRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListingApiTest {

    @LocalServerPort
    int port;

    RequestSpecification requestSpecification;

    @Autowired
    ListingRepository listingRepository;

    @BeforeEach
    public void setUpAbstractIntegrationTest() {
        requestSpecification = new RequestSpecBuilder()
                .setPort(port)
                .build();
    }

    @Test
    void shouldReturnListings() {
        createListing();

        searchListing("123", null, null, null, null)
                .statusCode(HttpStatus.OK.value())
                .body("listings", not(emptyOrNullString()))
                .body("listings[0].kw", is(132))
                .body("listings[0].price", is(13990))
                .body("listings[0].year", is(2014))
                .body("listings[0].dealerId", is("123"))
                .body("listings[0].model", is("megane"))
                .body("listings[0].make", is("renault"))
                .body("listings[0].color", is("green"))
                .body("listings[0].code", is("a"));
    }

    @Test
    void shouldSaveListings() {
        given(requestSpecification)
                .body(uploadListingRequest())
                .contentType(ContentType.JSON)
                .post("/listing/vehicle_listings/321")
                .then()
                .statusCode(HttpStatus.OK.value());

        searchListing("321", null, null, null, null)
                .statusCode(HttpStatus.OK.value())
                .body("listings", not(emptyOrNullString()))
                .body("listings[0].kw", is(111))
                .body("listings[0].price", is(111))
                .body("listings[0].year", is(2019))
                .body("listings[0].dealerId", is("321"))
                .body("listings[0].model", is("megane"))
                .body("listings[0].make", is("renault"))
                .body("listings[0].color", is("red"))
                .body("listings[0].code", is("b"));
    }

    @Test
    void searchShouldReturnFoundedListings() {
        given(requestSpecification)
                .body(uploadListingRequest())
                .contentType(ContentType.JSON)
                .post("/listing/vehicle_listings/321")
                .then()
                .statusCode(HttpStatus.OK.value());

        searchListing("321", null, null, 2019, null)
                .statusCode(HttpStatus.OK.value())
                .body("listings", not(emptyOrNullString()))
                .body("listings[0].kw", is(111))
                .body("listings[0].price", is(111))
                .body("listings[0].year", is(2019))
                .body("listings[0].dealerId", is("321"))
                .body("listings[0].model", is("megane"))
                .body("listings[0].make", is("renault"))
                .body("listings[0].color", is("red"))
                .body("listings[0].code", is("b"));
    }

    @Test
    void shouldReturnSavedCsv() {
        var content = "code, make/model, power-in-ps, year, color, price\n" +
                "1,mercedes/a 180,123,2014,black,15950\n" +
                "2,audi/a3,111,2016,white,17210\n" +
                "3,vw/golf,86,2018,green,14980\n" +
                "4,skoda/octavia,86,2018,green,169902";
        given()
                .spec(new RequestSpecBuilder().addMultiPart("listing", content,
                        MediaType.MULTIPART_FORM_DATA_VALUE).build())
                .spec(requestSpecification)
                .post("/listing/upload_csv/{dealerId}", "444")
                .then()
                .statusCode(HttpStatus.OK.value());

        searchListing("444", null, null, null, null)
                .statusCode(HttpStatus.OK.value())
                .body("listings", hasSize(4))
                .body("listings[0].kw", is(123))
                .body("listings[0].price", is(15950))
                .body("listings[0].year", is(2014))
                .body("listings[0].dealerId", is("444"))
                .body("listings[0].model", is("a 180"))
                .body("listings[0].make", is("mercedes"))
                .body("listings[0].color", is("black"))
                .body("listings[0].code", is("1"));
    }

    private ValidatableResponse searchListing(String dealerId, String model, String make, Integer year, String color) {
        return given(requestSpecification)
                .queryParam("model", model)
                .queryParam("make", make)
                .queryParam("year", year)
                .queryParam("color", color)
                .when()
                .get("/listing/search/{dealerId}", dealerId)
                .then();
    }


    private void createListing() {
        var listing = Listing.builder()
                .power(132)
                .price(13990)
                .year(2014)
                .dealerId("123")
                .model("megane")
                .make("renault")
                .color("green")
                .code("a")
                .build();
        listingRepository.save(listing).block(Duration.ofMillis(10000));
    }

    private Map uploadListingRequest() {
        return Map.of(
                "listings", List.of(
                        Map.of(
                                "kw", 111,
                                "price", 111,
                                "year", 2019,
                                "dealerId", "321",
                                "model", "megane",
                                "make", "renault",
                                "color", "red",
                                "code", "b"
                        )
                )
        );
    }
}
