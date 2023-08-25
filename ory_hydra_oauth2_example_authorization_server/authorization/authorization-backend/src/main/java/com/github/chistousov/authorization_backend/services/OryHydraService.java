package com.github.chistousov.authorization_backend.services;

import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.chistousov.authorization_backend.models.AcceptLoginRequestModel;
import com.github.chistousov.authorization_backend.models.GetLoginResponseModel;
import com.github.chistousov.authorization_backend.models.ResponseWithRedirectModel;

import reactor.core.publisher.Mono;


/**
 * <p>
 * Сервис для работы с Ory Hydra
 * </p>
 * 
 * @author Nikita Chistousov (chistousov.nik@yandex.ru)
 * @since 11
 */
@Service
public class OryHydraService {
    private static final String LOGIN_CHALLENGE = "login_challenge";
    private static final String CONSENT_CHALLENGE = "consent_challenge";
    private static final String LOGOUT_CHALLENGE = "logout_challenge";
	
	private WebClient oryHydraAdminEndPoint;

	public OryHydraService(Environment env){

        this.oryHydraAdminEndPoint = WebClient
                        .builder()
                        .baseUrl(Objects.requireNonNull(env.getProperty("application.ory-hydra.admin.baseURI")))
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        // Так как Ory Hydra настроена на TLS termination, то необходим заголовок ниже
                        // Делаем вид что, WebClient прокси с TLS termination
                        .defaultHeader("X-Forwarded-Proto", "https")
                        .build();
	}


    /**
     * <p>
     * проверяем аутентифицировался ли раньше пользователь и запомнил себя в
     * системе?
     * плюс так же получаем информацию о клиенте
     * </p>
     * 
     * @param loginChallenge - уникальный идентификатор обработки входа
     * 
     * @author Nikita Chistousov (chistousov.nik@yandex.ru)
     * @since 11
     */
    public Mono<GetLoginResponseModel> loginRequestInfo(String loginChallenge) {
        return this.oryHydraAdminEndPoint
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("oauth2/auth/requests/login")
                                .queryParam(LOGIN_CHALLENGE, loginChallenge)
                                .build())
                .retrieve()
                .bodyToMono(GetLoginResponseModel.class);
    }


    /**
     * <p>
     * принимаем аутентификацию определенного пользователя
     * </p>
     * 
     * @param loginChallenge - уникальный идентификатор обработки входа
     * @param acceptLoginRequestModel - данные для аутентификации
     * 
     * @author Nikita Chistousov (chistousov.nik@yandex.ru)
     * @since 11
     */
    public Mono<ResponseWithRedirectModel> acceptLoginRequest(String loginChallenge, Mono<AcceptLoginRequestModel> acceptLoginRequestModel) {
        return this.oryHydraAdminEndPoint
                .put()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("oauth2/auth/requests/login/accept")
                                .queryParam(LOGIN_CHALLENGE, loginChallenge)
                                .build())
                .body(acceptLoginRequestModel, AcceptLoginRequestModel.class)
                .retrieve()
                .bodyToMono(ResponseWithRedirectModel.class);
    }

    // /**
    //  * <p>
    //  * отклоняем аутентификацию определенного пользователя
    //  * </p>
    //  * 
    //  * @param loginChallenge - уникальный идентификатор обработки входа
    //  * @param rejectLoginRequestModel - данные об ошибке
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<ResponseWithRedirectModel> rejectLoginRequest(String loginChallenge,
    //         Mono<ErrorModel> rejectLoginRequestModel) {
    //     return this.oryHydraAdminEndPoint
    //             .put()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/login/reject")
    //                             .queryParam(LOGIN_CHALLENGE, loginChallenge)
    //                             .build())
    //             .body(rejectLoginRequestModel, ErrorModel.class)
    //             .retrieve()
    //             .bodyToMono(ResponseWithRedirectModel.class);
    // }

    // /**
    //  * <p>
    //  * проверяем начали ли проверку scope
    //  * в системе?
    //  * плюс так же получаем информацию о клиенте
    //  * </p>
    //  * 
    //  * @param consentChallenge - уникальный идентификатор обработки scope
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<GetConsentResponseModel> consentRequestInfo(String consentChallenge) {
    //     return this.oryHydraAdminEndPoint
    //             .get()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/consent")
    //                             .queryParam(CONSENT_CHALLENGE, consentChallenge)
    //                             .build())
    //             .retrieve()
    //             .bodyToMono(GetConsentResponseModel.class);
    // }

    // /**
    //  * <p>
    //  * принимаем scopes определенного пользователя
    //  * </p>
    //  * 
    //  * @param consentChallenge - уникальный идентификатор обработки scope
    //  * @param acceptConsentRequestModel - данные для scope
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<ResponseWithRedirectModel> acceptConsentRequest(String consentChallenge,
    //         Mono<AcceptConsentRequestModel> acceptConsentRequestModel) {
    //     return this.oryHydraAdminEndPoint
    //             .put()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/consent/accept")
    //                             .queryParam(CONSENT_CHALLENGE, consentChallenge)
    //                             .build())
    //             .body(acceptConsentRequestModel, AcceptConsentRequestModel.class)
    //             .retrieve()
    //             .bodyToMono(ResponseWithRedirectModel.class);
    // }


    // /**
    //  * <p>
    //  * Получаем информацию по logout
    //  * </p>
    //  * 
    //  * @param logoutChallenge - уникальный идентификатор обработки выхода
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<GetLogoutResponseModel> logoutRequestInfo(String logoutChallenge) {
    //     return this.oryHydraAdminEndPoint
    //             .get()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/logout")
    //                             .queryParam(LOGOUT_CHALLENGE, logoutChallenge)
    //                             .build())
    //             .retrieve()
    //             .bodyToMono(GetLogoutResponseModel.class);
    // }

    // /**
    //  * <p>
    //  * подтверждаем logout
    //  * </p>
    //  * 
    //  * @param logoutChallenge - уникальный идентификатор обработки logout
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<ResponseWithRedirectModel> acceptLogoutRequest(String logoutChallenge) {
    //     return this.oryHydraAdminEndPoint
    //             .put()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/logout/accept")
    //                             .queryParam(LOGOUT_CHALLENGE, logoutChallenge)
    //                             .build())
    //             .retrieve()
    //             .bodyToMono(ResponseWithRedirectModel.class);
    // }

    // /**
    //  * <p>
    //  * отклоняем запрос на logout
    //  * </p>
    //  * 
    //  * @param logoutChallenge - уникальный идентификатор обработки выхода
    //  * 
    //  * @author Nikita Chistousov (chistousov.nik@yandex.ru)
    //  * @since 11
    //  */
    // public Mono<ResponseWithRedirectModel> rejectLogoutRequest(String logoutChallenge) {
    //     return this.oryHydraAdminEndPoint
    //             .put()
    //             .uri(
    //                     uriBuilder -> uriBuilder
    //                             .path("oauth2/auth/requests/logout/reject")
    //                             .queryParam(LOGOUT_CHALLENGE, logoutChallenge)
    //                             .build())
    //             .retrieve()
    //             .bodyToMono(ResponseWithRedirectModel.class);
    // }

}