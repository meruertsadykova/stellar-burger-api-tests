package methods;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Login;
import models.User;

import static constants.ApiConstants.*;
import static io.restassured.RestAssured.given;

public class UserRequests {

    @Step("Создание пользователя")
    public Response createUser(User user){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post(USER_REGISTER);
    }

    @Step("Логин пользователя")
    public Response loginUser(Login login){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(login)
                .when()
                .post(USER_LOGIN);
    }

    @Step("Получить информацию о пользователе")
    public Response getUser(String token){
        return given()
                .header("Authorization", token)
                .get(USER);
    }

    @Step("Обновление данных авторизованного пользователя")
    public Response updateUser(User user, String token){
        return given()
                .header("Authorization", token)
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER);
    }

    @Step("Обновление данных пользователя без авторизации")
    public Response updateUserWithoutAuthorization(User user){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(USER);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token){
        return given()
                .header("Authorization", token)
                .delete(USER);
    }
}
