import methods.RequestSpec;
import models.Login;
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
public class CreateUserTest {

    private final String email;
    private final String password;
    private final String name;
    private User user;
    private UserRequests userRequests;

    public CreateUserTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters(name = "Email: {0}, Password: {1}, Name: {2}")
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
        user = new User(email, password, name);
        userRequests = new UserRequests();
    }

    @Test
    @DisplayName("Проверка успешного создания пользователя")
    @Description("Тест проверяет возможность создания нового пользователя по API с валидными данными. " +
            "После успешного создания проверяется, что возвращается статус 200 и success=true.")
    public void createUser() {
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().statusCode(200).body("success", equalTo(true));
    }

    @After
    public void deleteUser() {
        try {
            Login login = new Login(user.getEmail(), user.getPassword());

            Response loginResponse = userRequests.loginUser(login);
            String accessToken = loginResponse.then().extract().path("accessToken");

            if (accessToken != null) {
                Response responseDelete = userRequests.deleteUser(accessToken);
                responseDelete.then().statusCode(202).body("success", equalTo(true));
            }
        } catch (Exception e) {
            System.out.println("Не удалось удалить пользователя: " + e.getMessage());
        }
    }

}


