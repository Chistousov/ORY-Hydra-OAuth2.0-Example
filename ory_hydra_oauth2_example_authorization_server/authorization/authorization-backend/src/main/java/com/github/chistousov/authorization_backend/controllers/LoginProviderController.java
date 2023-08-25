package com.github.chistousov.authorization_backend.controllers;

import java.net.URI;
import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.WebSession;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


// @Controller
// @RequestMapping("/login")
// @Slf4j
public class LoginProviderController {

    // private static final String NUMBER_OF_LOGIN_ATTEMPTS_ZERO = "Количество попыток входа истекло";
    // private static final String NUMBER_OF_LOGIN_ATTEMPTS = "number_of_login_attempts";
    // private static final String LOGIN_CHALLENGE = "login_challenge";

    // private UserService userService;
    // private OryHydraService oryHydraService;

    // //количество попыток входа
    // private int numberOfLoginAttempts;

    // private String frontendURI;

    // public LoginProviderController(Environment env, UserService userService, OryHydraService oryHydraService) {

    //     this.userService = userService;
    //     this.oryHydraService = oryHydraService;

    //     this.numberOfLoginAttempts = Integer.parseUnsignedInt(env.getProperty("application.ory-hydra.number-of-login-attempts", "5"));

    //     this.frontendURI = env.getProperty("application.ory-hydra.frontend.login-redirectURI");

    // }

    // @GetMapping
    // public Mono<ResponseEntity<Object>> getLogin(@RequestParam(LOGIN_CHALLENGE) String loginChallenge, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSession webSession) {

    //     var hostHeaderRequest =  serverHttpRequest.getHeaders().getHost();
    //     if(Objects.isNull(hostHeaderRequest)){
    //         throw new IllegalArgumentException("Не найден заголовок Host");
    //     }

    //     return oryHydraService.loginRequestInfo(loginChallenge)
    //         .flatMap(loginRequestInfo -> {
                    
    //                 //если пользователь входил раньше
    //                 if(loginRequestInfo.getSkip().booleanValue()) {

    //                     // получаем информацию с БД
    //                     var userMono = userService.createUser(Long.valueOf(loginRequestInfo.getSubject()));

    //                     // модель для подтверждения входа
    //                     var acceptLoginRequestModel = userMono.map(
    //                         userModel ->
    //                         AcceptLoginRequestModelBuilder.builder()
    //                             .setSubject(loginRequestInfo.getSubject())
    //                             .setRemember(loginRequestInfo.getSkip())
    //                             //если getIsRemember == true, то запоминаем на сутки
    //                             .setRememberFor(60L * 60L * 24L)
    //                             // передаем все права в consent
    //                             // в consentЕ привяжим их к токену
    //                             .setContextModel(userModel)
    //                             .build()
    //                     );

    //                     return oryHydraService.acceptLoginRequest(
    //                         loginChallenge,
    //                         acceptLoginRequestModel
    //                     )
    //                     // переадресуем браузер
    //                     .map(responseWithRedirectModel -> 
    //                         ResponseEntity
    //                             .status(HttpStatus.FOUND)
    //                             .location(URI.create(responseWithRedirectModel.getRedirectTo()))
    //                             .build() 
    //                     )
    //                     .doOnEach(LoggerMDCWebFilter.logOnComplete(el -> log.info("Пользователь уже входил в систему")));
    //                 }

    //                 // запоминаем идентификатор попытки входа (login challenge)
    //                 webSession.getAttributes().put(LOGIN_CHALLENGE, loginChallenge);
    //                 // задаем количество попыток входа
    //                 webSession.getAttributes().put(NUMBER_OF_LOGIN_ATTEMPTS, String.valueOf(numberOfLoginAttempts));
                    
    //                 return Mono.just(
    //                     ResponseEntity
    //                         .status(HttpStatus.FOUND)
    //                         .location(URI.create(frontendURI))
    //                         .build() 
    //                 )
    //                 .doOnEach(LoggerMDCWebFilter.logOnComplete(el -> log.info("Пользователь получает страницу входа")));
    //         });


    // }

    // @PostMapping
    // @ResponseBody
    // public Mono<ResponseEntity<ResponseWithRedirectModel>> postLogin(@RequestBody PostLoginModel postLoginModel,
    //                                             WebSession webSession,
    //                                             ServerHttpRequest serverHttpRequest,
    //                                             ServerHttpResponse serverHttpResponse) {
        
    //     var hostHeaderRequest =  serverHttpRequest.getHeaders().getHost();
    //     if(Objects.isNull(hostHeaderRequest)){
    //         throw new IllegalArgumentException("Не найден заголовок Host");
    //     }


    //     // получаем loginChallenge
    //     String loginChallenge = webSession.getRequiredAttribute(LOGIN_CHALLENGE).toString();
    //     // получаем количество попыток
    //     long numberAttempts = Long.parseUnsignedLong(webSession.getRequiredAttribute(NUMBER_OF_LOGIN_ATTEMPTS));

    //     if(numberAttempts == 0L){
    //         var errorModel = Mono.just(
    //             ErrorModelBuilder
    //                 .builder()
    //                 .setError("request_denied")
    //                 .setErrorDebug(NUMBER_OF_LOGIN_ATTEMPTS_ZERO)
    //                 .setErrorDescription("Количество попыток входа истекло! Закройте вкладку и повторите вход в приложение.")
    //                 .setErrorHint(NUMBER_OF_LOGIN_ATTEMPTS_ZERO)
    //                 .setStatusCode(401L)
    //                 .build()
    //         );
            
    //         return oryHydraService.rejectLoginRequest(
    //                     loginChallenge,
    //                     errorModel
    //                 )
    //                     // переадресуем браузер
    //                     .map(responseWithRedirectModel -> 
    //                         ResponseEntity
    //                             .status(HttpStatus.CREATED)
    //                             .body(responseWithRedirectModel)
    //                     )
    //             .doOnEach(LoggerMDCWebFilter.logOnComplete(el -> log.info(NUMBER_OF_LOGIN_ATTEMPTS_ZERO)));
    //     }

    //     numberAttempts--;

    //     webSession.getAttributes().put(NUMBER_OF_LOGIN_ATTEMPTS, String.valueOf(numberAttempts));

    //     // Mono для проверки пользователя в Oracle БД по паролю
    //     var userMono = userService.createUser(postLoginModel.getPassword());

    //     //одновременно проверяем пользователя в Oracle БД по паролю и
    //     //запрашиваем метаинформацию о попытке входа по loginChallenge
    //     return userMono.zipWith(
    //         oryHydraService.loginRequestInfo(loginChallenge),
    //         (userModel, loginChallengeInfo) -> {

    //             var acceptLoginRequestModel = Mono.just(
    //                 AcceptLoginRequestModelBuilder.builder()
    //                 .setSubject(userModel.getManid().toString())
    //                 .setRemember(postLoginModel.getIsRemember())
    //                 //если getIsRemember == true, то запоминаем на сутки
    //                 .setRememberFor(60L * 60L * 24L)
    //                 // передаем все права в consent
    //                 // в consentЕ привяжим их к токену
    //                 .setContextModel(userModel)
    //                 .build()  
    //             );

    //             return oryHydraService.acceptLoginRequest(loginChallenge, acceptLoginRequestModel)
    //                 // переадресуем браузер
    //                 .map(responseWithRedirectModel -> {

    //                     webSession.getAttributes().remove(LOGIN_CHALLENGE);
    //                     webSession.getAttributes().remove(NUMBER_OF_LOGIN_ATTEMPTS);

    //                     return 
    //                         ResponseEntity
    //                             .status(HttpStatus.CREATED)
    //                             .body(responseWithRedirectModel);
    //                 });
    //         }
    //     ).flatMap(el -> el)
    //     .doOnEach(LoggerMDCWebFilter.logOnComplete(el -> log.info("Login закончился успехом. Переходит на consent.")));
        
    // }

}