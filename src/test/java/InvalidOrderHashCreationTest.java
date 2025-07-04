import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.apache.http.HttpStatus.*;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;

import org.junit.runners.Parameterized;
import methods.OrderRequests;

import java.util.List;

@RunWith(Parameterized.class)
public class InvalidOrderHashCreationTest {

    private final List<String> ingredients;
    private OrderRequests orderRequests;
    private Order order;

    public InvalidOrderHashCreationTest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @Parameterized.Parameters(name = "Проверка с невалидным хэшем: {0}")
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {List.of(faker.number().digits(7))},
                {List.of(faker.number().digits(23))},
                {List.of(faker.number().digits(25))}
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Создание заказа с невалидными хэшами ингредиентов")
    @Description("Проверка, что при попытке создать заказ с несуществующими или некорректными хэшами ингредиентов возвращается ошибка 500")
    public void createOrderInvalidHash() {
        Response responseCreate = orderRequests.createOrder(order);
        responseCreate.then().log().all().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

}
