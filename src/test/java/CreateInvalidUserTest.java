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
import methods.UserRequests;

import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class CreateInvalidUserTest {

    private final String email;
    private final String password;
    private final String name;
    private final int statusCode;
    private final boolean success;
    private final String message;

    private User user;
    private UserRequests userRequests;
    private String accessToken;

    public CreateInvalidUserTest(String email, String password, String name, int statusCode, boolean success, String message) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
    }

    @Parameterized.Parameters(name = "Email: {0}, Password: {1}, Name: {2}")
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {null, faker.internet().password(), faker.name().firstName(), 403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), null, faker.name().firstName(), 403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), faker.internet().password(), null, 403, false, "Email, password and name are required fields"},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName(), 200, true, null}
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        user = new User(email, password, name);
        userRequests = new UserRequests();
    }

    @Test
    @DisplayName("Создание пользователя с невалидными данными или повторное создание существующего")
    @Description("Тест проверяет, что API возвращает ошибку при попытке создать пользователя с отсутствующими обязательными полями " +
            "и запрещает создание уже существующего пользователя.")
    public void createInvalidUser() {
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then()
                .log().all()
                .statusCode(statusCode)
                .body("success", equalTo(success))
                .body("message", equalTo(message));

        if (responseCreate.statusCode() == 200) {
            accessToken = responseCreate.then().extract().path("accessToken");
            Response responseCreateDouble = userRequests.createUser(user);
            responseCreateDouble.then()
                    .log().all()
                    .statusCode(403)
                    .body("success", equalTo(false))
                    .body("message", equalTo("User already exists"));
        }
    }

    @After
    public void deleteUser() {
        if (statusCode == 200 && accessToken != null) {
            userRequests.deleteUser(accessToken);
        }
    }
}


