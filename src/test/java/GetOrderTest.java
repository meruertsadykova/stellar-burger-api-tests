import methods.RequestSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.OrderRequests;
import methods.UserRequests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class GetOrderTest {

    private final String email;
    private final String password;
    private final String name;
    private UserRequests userRequests;
    private OrderRequests orderRequests;
    private String accessToken;

    public GetOrderTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters(name = "User: {0}, Pass: {1}, Name: {2}")
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()}
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
        userRequests = new UserRequests();
        User user = new User(email, password, name);
        Response responseCreate = userRequests.createUser(user);
        accessToken = responseCreate.then().log().all().extract().path("accessToken");
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Проверяет, что авторизованный пользователь может получить список своих заказов и в ответе поле 'orders.total' не null.")
    public void getOrderTest() {
        Response responseGetOrder = orderRequests.getOrderUser(accessToken);
        responseGetOrder.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders.total", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    @Description("Проверяет, что неавторизованный пользователь не может получить список заказов и получает ошибку 401.")
    public void getOrderWithoutAuthorization() {
        Response response = orderRequests.getOrderUserWithoutAuthorization();
        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            Response responseDelete = userRequests.deleteUser(accessToken);
            responseDelete.then().log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
        }
    }
}

