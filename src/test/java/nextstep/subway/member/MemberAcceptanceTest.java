package nextstep.subway.member;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.member.dto.MemberRequest;
import nextstep.subway.member.dto.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.ACCESS_TOKEN_요청;
import static org.assertj.core.api.Assertions.assertThat;

public class MemberAcceptanceTest extends AcceptanceTest {
    public static final String EMAIL = "email@email.com";
    public static final String PASSWORD = "password";
    public static final String NEW_EMAIL = "newemail@email.com";
    public static final String NEW_PASSWORD = "newpassword";
    public static final int AGE = 20;
    public static final int NEW_AGE = 21;

    @DisplayName("회원 정보를 관리한다.")
    @Test
    void manageMember() {
        // when
        ExtractableResponse<Response> createResponse = 회원_생성을_요청(EMAIL, PASSWORD, AGE);
        // then
        회원_생성됨(createResponse);

        // when
        ExtractableResponse<Response> findResponse = 회원_정보_조회_요청(createResponse);
        // then
        회원_정보_조회됨(findResponse, EMAIL, AGE);

        // when
        ExtractableResponse<Response> updateResponse = 회원_정보_수정_요청(createResponse, NEW_EMAIL, NEW_PASSWORD, NEW_AGE);
        // then
        회원_정보_수정됨(updateResponse);

        // when
        ExtractableResponse<Response> deleteResponse = 회원_삭제_요청(createResponse);
        // then
        회원_삭제됨(deleteResponse);
    }

    @DisplayName("나의 정보를 관리한다.")
    @Test
    void manageMyInfo() {
        // given
        회원_등록됨(EMAIL, PASSWORD, AGE);
        TokenResponse token = ACCESS_TOKEN_요청(EMAIL, PASSWORD).as(TokenResponse.class);

        // when
        ExtractableResponse<Response> 내_정보_응답 = 내_정보_조회_요청(token.getAccessToken());
        // then
        회원_정보_조회됨(내_정보_응답);

        // when
        MemberRequest updateMember = new MemberRequest(NEW_EMAIL, NEW_PASSWORD, NEW_AGE);
        ExtractableResponse<Response> 내_정보_수정_응답 = 내_정보_수정_요청(token.getAccessToken(), updateMember);

        // then
        TokenResponse updatedToken = ACCESS_TOKEN_요청(NEW_EMAIL, NEW_PASSWORD).as(TokenResponse.class);
        내_정보_수정_검증(updatedToken.getAccessToken(), 내_정보_수정_응답);

        // when
        ExtractableResponse<Response> 내_정보_삭제_응답 = 내_정보_삭제_요청(updatedToken.getAccessToken());

        // then
        회원_삭제됨(내_정보_삭제_응답);
    }

    private void 내_정보_수정_검증(String accessToken, ExtractableResponse<Response> 내_정보_수정_응답) {
        MemberResponse nowMember = 내_정보_조회_요청(accessToken).as(MemberResponse.class);
        MemberResponse updatedMember = 내_정보_수정_응답.as(MemberResponse.class);

        회원_정보_수정됨(내_정보_수정_응답);
        assertThat(nowMember).isEqualTo(updatedMember);
    }

    public static ExtractableResponse<Response> 내_정보_조회_요청(String accessToken) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/members/me")
                .then().log().all().extract();
    }

    public static ExtractableResponse<Response> 내_정보_수정_요청(String accessToken, MemberRequest request) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/members/me")
                .then().log().all().extract();
    }

    public static ExtractableResponse<Response> 내_정보_삭제_요청(String accessToken) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/members/me")
                .then().log().all().extract();
    }

    public static void 회원_등록됨(String email, String password, Integer age) {
        회원_생성을_요청(email, password, age);
    }

    public static ExtractableResponse<Response> 회원_생성을_요청(String email, String password, Integer age) {
        MemberRequest memberRequest = new MemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_정보_조회_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get(uri)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_정보_수정_요청(ExtractableResponse<Response> response, String email, String password, Integer age) {
        String uri = response.header("Location");
        MemberRequest memberRequest = new MemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().put(uri)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_삭제_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");
        return RestAssured
                .given().log().all()
                .when().delete(uri)
                .then().log().all()
                .extract();
    }

    public static void 회원_생성됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.CREATED);
    }

    public static void 회원_정보_조회됨(ExtractableResponse<Response> response, String email, int age) {
        MemberResponse memberResponse = response.as(MemberResponse.class);
        assertThat(memberResponse.getId()).isNotNull();
        assertThat(memberResponse.getEmail()).isEqualTo(email);
        assertThat(memberResponse.getAge()).isEqualTo(age);
    }

    public static void 회원_정보_조회됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.OK);
    }

    public static void 회원_정보_수정됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.OK);
    }

    public static void 회원_삭제됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.NO_CONTENT);
    }
}
