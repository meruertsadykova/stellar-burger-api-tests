import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.UserRequests;
import org.apache.http.HttpStatus;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class LoginTest {

    private final boolean isValidLogin;
    private final String wrongPassword;
    private String email;
    private String password;
    private String name;
    private User user;
    private Login login;
    private UserRequests userRequests;
    private String accessToken;

    public LoginTest(boolean isValidLogin, String wrongPassword) {
        this.isValidLogin = isValidLogin;
        this.wrongPassword = wrongPassword;
    }

    @Parameterized.Parameters(name = "Валидный логин: {0}")
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {true, null},
                {false, faker.internet().password()},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        Faker faker = new Faker();
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().firstName();

        user = new User(email, password, name);
        userRequests = new UserRequests();

        Response responseCreate = userRequests.createUser(user);
        accessToken = responseCreate.then().log().all().statusCode(HttpStatus.SC_OK).extract().path("accessToken");

        login = isValidLogin ? new Login(email, password) : new Login(email, wrongPassword);
    }

    @Test
    @DisplayName("Проверка логина с валидными и невалидными данными")
    @Description("Позитивный и негативный сценарии логина пользователя")
    public void loginUser() {
        Response responseLogin = userRequests.loginUser(login);

        if (isValidLogin) {
            responseLogin.then().log().all()
                    .statusCode(HttpStatus.SC_OK)
                    .body("success", equalTo(true));
        } else {
            responseLogin.then().log().all()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body("success", equalTo(false))
                    .body("message", equalTo("email or password are incorrect"));
        }
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            Response responseDelete = userRequests.deleteUser(accessToken);
            responseDelete.then().log().all().statusCode(HttpStatus.SC_ACCEPTED).body("success", equalTo(true));
        }
    }
}
