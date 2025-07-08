import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import org.apache.http.HttpStatus;

import org.junit.runners.Parameterized;
import methods.OrderRequests;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final int fromIndex;
    private final int toIndex;
    private final int statusCode;
    private OrderRequests orderRequests;

    public CreateOrderTest(int fromIndex, int toIndex, int statusCode) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.statusCode = statusCode;
    }

    @Parameterized.Parameters(name = "Создание заказа с ингредиентами от {0} до {1}, ожидаемый код: {2}")
    public static Object[][] getData() {
        return new Object[][]{
                {0, 1, HttpStatus.SC_OK},
                {0, 6, HttpStatus.SC_OK},
                {10, 15, HttpStatus.SC_OK},
                {0, 0, HttpStatus.SC_BAD_REQUEST},
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @Test
    @DisplayName("Создание заказа с разным количеством ингредиентов")
    @Description("Проверяется успешное создание заказа с валидными ингредиентами и ошибка при отсутствии ингредиентов")
    public void createOrder() {
        Response responseGetIngredient = orderRequests.getIngredient();
        List<String> ingredients = new ArrayList<>(responseGetIngredient.then().log().all().statusCode(HttpStatus.SC_OK).extract().path("data._id"));

        Order order = new Order(ingredients.subList(fromIndex, toIndex));
        Response responseCreate = orderRequests.createOrder(order);

        if (statusCode == HttpStatus.SC_OK) {
            responseCreate.then().log().all()
                    .assertThat()
                    .body("order.number", notNullValue())
                    .body("success", equalTo(true));
        } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
            responseCreate.then().log().all()
                    .assertThat()
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        }
    }
}

