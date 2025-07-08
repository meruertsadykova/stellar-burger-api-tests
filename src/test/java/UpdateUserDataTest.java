import methods.UserRequests;
import models.Login;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.github.javafaker.Faker;
import org.apache.http.HttpStatus;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserDataTest {

    private String oldEmail;
    private String oldPassword;
    private String oldName;

    private String newEmail;
    private String newPassword;
    private String newName;

    private UserRequests userRequests;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;

        Faker faker = new Faker();
        oldEmail = faker.internet().emailAddress();
        oldPassword = faker.internet().password();
        oldName = faker.name().firstName();

        newEmail = faker.internet().emailAddress();
        newPassword = faker.internet().password();
        newName = faker.name().firstName();

        userRequests = new UserRequests();
        User user = new User(oldEmail, oldPassword, oldName);
        Response response = userRequests.createUser(user);
        accessToken = response.then().statusCode(HttpStatus.SC_OK).extract().path("accessToken");
    }

    @Test
    @DisplayName("Обновление email авторизованного пользователя")
    @Description("Пользователь с валидным токеном может обновить свой email")
    public void updateUserEmailTest() {
        User updatedUser = new User(newEmail, oldPassword, oldName);
        userRequests.updateUser(updatedUser, accessToken)
                .then().statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));

        userRequests.getUser(accessToken)
                .then().statusCode(HttpStatus.SC_OK)
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Обновление имени авторизованного пользователя")
    @Description("Пользователь с валидным токеном может обновить своё имя")
    public void updateUserNameTest() {
        User updatedUser = new User(oldEmail, oldPassword, newName);
        userRequests.updateUser(updatedUser, accessToken)
                .then().statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true))
                .body("user.name", equalTo(newName));

        userRequests.getUser(accessToken)
                .then().statusCode(HttpStatus.SC_OK)
                .body("user.name", equalTo(newName));
    }

    @Test
    @DisplayName("Обновление пароля и вход с новым паролем")
    @Description("Пользователь может изменить пароль и авторизоваться с новым паролем")
    public void updateUserPasswordTest() {
        User updatedUser = new User(oldEmail, newPassword, oldName);
        userRequests.updateUser(updatedUser, accessToken)
                .then().statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true));

        userRequests.loginUser(new Login(oldEmail, newPassword))
                .then().statusCode(HttpStatus.SC_OK)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Попытка обновления данных без авторизации")
    @Description("Неавторизованный пользователь не может изменить данные и получает ошибку 401")
    public void updateUserWithoutAuthorizationTest() {
        User updatedUser = new User(newEmail, newPassword, newName);
        userRequests.updateUserWithoutAuthorization(updatedUser)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userRequests.deleteUser(accessToken)
                    .then().statusCode(HttpStatus.SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}
