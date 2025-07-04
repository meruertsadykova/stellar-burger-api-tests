import methods.UserRequests;
import models.Login;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class UpdateUserDataTest {

    private final String newEmail;
    private final String newPassword;
    private final String newName;

    private String oldEmail;
    private String oldPassword;
    private String oldName;

    private UserRequests userRequests;
    private String accessToken;

    public UpdateUserDataTest(String newEmail, String newPassword, String newName) {
        this.newEmail = newEmail;
        this.newPassword = newPassword;
        this.newName = newName;
    }

    @Parameterized.Parameters(name = "New data: {0}, {1}, {2}")
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
        RestAssured.baseURI = BURGERS_URL;
        Faker faker = new Faker();
        oldEmail = faker.internet().emailAddress();
        oldPassword = faker.internet().password();
        oldName = faker.name().firstName();

        User user = new User(oldEmail, oldPassword, oldName);
        userRequests = new UserRequests();
        Response responseCreate = userRequests.createUser(user);
        accessToken = responseCreate.then().log().all().statusCode(200).extract().path("accessToken");
    }

    @Test
    @DisplayName("Обновление данных авторизованного пользователя")
    @Description("Пользователь может изменить email, пароль и имя при наличии токена")
    public void modifyUserDataTest() {
        userRequests.getUser(accessToken)
                .then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(oldEmail))
                .body("user.name", equalTo(oldName));

        User newUserData = new User(newEmail, newPassword, newName);
        userRequests.updateUser(newUserData, accessToken)
                .then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));

        userRequests.getUser(accessToken)
                .then().log().all()
                .statusCode(200)
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));

        userRequests.loginUser(new Login(newEmail, newPassword))
                .then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Попытка обновления данных без авторизации")
    @Description("Если пользователь не авторизован — данные не изменяются, возвращается 401")
    public void modifyUserDataWithoutAuthorizationTest() {
        User updateAttempt = new User(newEmail, newPassword, newName);
        userRequests.updateUserWithoutAuthorization(updateAttempt)
                .then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            userRequests.deleteUser(accessToken)
                    .then().log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
        }
    }
}
