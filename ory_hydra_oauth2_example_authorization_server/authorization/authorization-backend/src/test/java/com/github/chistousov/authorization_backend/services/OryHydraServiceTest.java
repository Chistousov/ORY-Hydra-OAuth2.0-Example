package com.github.chistousov.authorization_backend.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.chistousov.authorization_backend.models.AcceptLoginRequestModelBuilder;
import com.github.chistousov.authorization_backend.models.CreateClientModel;
import com.github.chistousov.authorization_backend.models.PostRegistrationModel;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
public class OryHydraServiceTest {

	private static final File pathToResourceDockerComposeFile = Path.of("").toAbsolutePath().getParent().getParent()
			.resolve("docker-compose.yaml").toFile();

	private static final String postgresqlUserDataContainerDockerCompose = "ory-hydra-oauth2-ex-auth-server-userdata-postgresql";
	private static final int postgresqlUserDataPortDockerCompose = 5432;
	private static final String postgresqlUserDataUserDockerCompose = "user_data";
	private static final String postgresqlUserDataPassDockerCompose = "superpass";

	private static final String postgresqlHydraPassDockerCompose = "superpass2";

	private static final String hydraSecretsCookiesDockerCompose = "some_cookies111111111111111122222";
	private static final String hydraSecretsSystemDockerCompose = "some_secrets111111111111111122222";

	private static final String hydraContainerDockerCompose = "ory-hydra-oauth2-example-authorization-server-hydra";
	private static final int hydraAdminPortDockerCompose = 4445;
	private static final int hydraPublicPortDockerCompose = 4444;

	private static final String login = "some_login";
	private static final String password = "some_password";
	private static final String orgName = "some_org";

	private static WebTestClient publicWebTestClient;
	private static WebTestClient adminWebTestClient;

	private static final String AUTH_DOMAIN = "authorization-server.com";

	private static final String OAUTH2_AUTHENTICATION_CSRF = "oauth2_authentication_csrf";
	private static final String OAUTH2_CONSENT_CSRF = "oauth2_consent_csrf";
	private static final String OAUTH2_AUTHENTICATION_SESSION = "oauth2_authentication_session";

	private static final String LOGIN_CHALLENGE = "login_challenge";
	private static final String CONSENT_CHALLENGE = "consent_challenge";
	private static final String CODE_CHALLENGE = "code_challenge";

	private static final String LOGOUT_CHALLENGE = "logout_challenge";

	// PKCE
	private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
	private static final String CODE_VERIFIER = "code_verifier";

	// протокол PKCE code challenge method SHA256
	private static final String codeChallengeMethod = "S256";

	// название cookies для session Spring
	private static final String SESSION = "SESSION-LOGIN-WRAPPER";

	@Container
	public static DockerComposeContainer<?> containersDockerCompose = new DockerComposeContainer<>(
			pathToResourceDockerComposeFile)
			.withEnv("USER_DATA_POSTGRESQL_PASSWORD", postgresqlUserDataPassDockerCompose)
			.withEnv("HYDRA_POSTGRESQL_PASSWORD", postgresqlHydraPassDockerCompose)
			.withEnv("HYDRA_SECRETS_COOKIE", hydraSecretsCookiesDockerCompose)
			.withEnv("HYDRA_SECRETS_SYSTEM", hydraSecretsSystemDockerCompose)
			.withEnv("HYDRA_DEPENDS_ON_MIGRATE", "service_started")
			
			.withExposedService(postgresqlUserDataContainerDockerCompose, postgresqlUserDataPortDockerCompose,
					Wait.forSuccessfulCommand(
							String.format("pg_isready -U %s", postgresqlUserDataUserDockerCompose))
							.withStartupTimeout(Duration.ofMinutes(1)))
			.withExposedService(hydraContainerDockerCompose, hydraPublicPortDockerCompose,
					Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(30)))
			.withExposedService(hydraContainerDockerCompose, hydraAdminPortDockerCompose,
					Wait.forHttp("/version").withHeader("X-Forwarded-Proto", "https")
							.withStartupTimeout(Duration.ofMinutes(30)))
			// .yaml v2 <-> v3
			.withOptions("--compatibility")
			// docker-compose local
			.withLocalCompose(true);


	// созданный тестовый клиент приложения
	private static String clientId;
	private static String clientSecret;

	@DynamicPropertySource
	public static void postgresqlUserDataSettings(DynamicPropertyRegistry registry)
			throws UnsupportedEncodingException {

		/* postgresql user */
		registry.add("spring.datasource.driverClassName", () -> "org.postgresql.Driver");
		registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

		String postgresqlStringConn = String.format(
				"jdbc:postgresql://%s:%d/%s?user=%s&password=%s&escapeSyntaxCallMode=callIfNoReturn",
				containersDockerCompose.getServiceHost(postgresqlUserDataContainerDockerCompose,
						postgresqlUserDataPortDockerCompose),
				containersDockerCompose.getServicePort(postgresqlUserDataContainerDockerCompose,
						postgresqlUserDataPortDockerCompose),
				postgresqlUserDataUserDockerCompose,
				postgresqlUserDataUserDockerCompose,
				postgresqlUserDataPassDockerCompose);

		registry.add("spring.datasource.url", () -> postgresqlStringConn);

		/* Ory Hydra admin */

		var hydraAdminBaseURI = String.format("http://%s:%d/admin/",
				containersDockerCompose.getServiceHost(hydraContainerDockerCompose, hydraAdminPortDockerCompose),
				containersDockerCompose.getServicePort(hydraContainerDockerCompose, hydraAdminPortDockerCompose));

		registry.add("application.ory-hydra.admin.baseURI", () -> hydraAdminBaseURI);

		adminWebTestClient = WebTestClient.bindToServer()
				.baseUrl(hydraAdminBaseURI)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				// Так как Ory Hydra настроена на TLS termination, то необходим заголовок ниже
				// Делаем вид что, WebClient прокси с TLS termination
				.defaultHeader("X-Forwarded-Proto", "https")
				.build();

		/* Ory Hydra Public */

		var hydraPublicBaseURI = String.format("http://%s:%d/",
				containersDockerCompose.getServiceHost(hydraContainerDockerCompose, hydraPublicPortDockerCompose),
				containersDockerCompose.getServicePort(hydraContainerDockerCompose, hydraPublicPortDockerCompose));

		registry.add("application.ory-hydra.public.baseURI", () -> hydraPublicBaseURI);

		publicWebTestClient = WebTestClient.bindToServer()
				.baseUrl(hydraPublicBaseURI)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				// Так как Ory Hydra настроена на TLS termination, то необходим заголовок ниже
				// Делаем вид что, WebClient прокси с TLS termination
				.defaultHeader("X-Forwarded-Proto", "https")
				.build();


		// создаем тестового клиента приложения
		CreateClientModel createClientModel = new CreateClientModel();
		createClientModel.setRedirect_uris(List.of("http://127.0.0.1:9631/callback"));
		createClientModel.setGrant_types(List.of("authorization_code"));
		createClientModel.setResponse_types(List.of("code"));
		createClientModel.setScope("offine");

		adminWebTestClient
				.post()
				.uri("/clients")
				.body(Mono.just(createClientModel), CreateClientModel.class)
				.exchange()
				.expectStatus()
				.isCreated()
				.expectBody(CreateClientModel.class)
				.value(val -> {
					clientId = val.getClient_id();
					clientSecret = val.getClient_secret();
				});

	}

	@Autowired
	private UserService userService;

	@Autowired
	private OryHydraService oryHydraService;

	// state для 4.1.1 Authorization Request для grant type Authorization Code Grant
	private static String state;

	private String oauth2AuthenticationCSRF = null;
	private String oauth2ConsentCSRF = null;
	private String oauth2AuthenticationSession = null;

	private String loginLocation = null;
	private String loginChallenge = null;
	private String consentChallenge = null;

	private String session = null;
	private String redirectConsentLocation = null;
	private String consentLocation = null;
	private String redirectForContentVerifier = null;
	private String redirectCallback = null;

	private String logoutLocation = null;
	private String logoutChallenge = null;
	private String redirectLogoutLocation = null;

	private String codeVerifier;
	private String codeChallenge;


	@BeforeEach
	public void initEach() throws UnsupportedEncodingException, NoSuchAlgorithmException {

		userService.createUser(
				PostRegistrationModel
						.builder()
						.login(login)
						.password(password)
						.orgName(orgName)
						.build());

		// PKCE
		codeVerifier = generateCodeVerifier();
		codeChallenge = generateCodeChallange(codeVerifier);

		state = generateStr();

	}

	private List<String> headerSetCookieLoginFlow;

	// начало аутентификации с редиректом на login
	private void oauth2AuthRedirectToLogin() throws URISyntaxException {

		publicWebTestClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.path("oauth2/auth")
						.queryParam("client_id", clientId)
						.queryParam("response_type", "code")
						.queryParam("state", state)
						// PKCE
						.queryParam(CODE_CHALLENGE, codeChallenge)
						.queryParam(CODE_CHALLENGE_METHOD, codeChallengeMethod)
						.build())
				.cookie(OAUTH2_AUTHENTICATION_SESSION, oauth2AuthenticationSession == null
						|| oauth2AuthenticationSession.isBlank()
						|| oauth2AuthenticationSession.isEmpty() ? ""
								: oauth2AuthenticationSession)
				.exchange()
				.expectStatus()
				.isFound()
				.expectHeader()
				.exists("Location")
				// проверка, что редирект действительно отправляем на данный сервер
				.expectHeader()
				.valueMatches("Location",
						String.format("^https:\\/\\/%s\\/api\\/login\\?login_challenge=.+$",
								AUTH_DOMAIN))
				.expectHeader()
				.value("Location", location -> loginLocation = location)	
				.expectHeader()
				.exists("Set-Cookie")
				.expectHeader()
				.values("Set-Cookie", val -> headerSetCookieLoginFlow = val);
				// .expectCookie()
				// .exists(OAUTH2_AUTHENTICATION_CSRF)
				// .expectCookie()
				// // защита от CSRF
				// .value(OAUTH2_AUTHENTICATION_CSRF, val -> oauth2AuthenticationCSRF = val);

		URI loginLocationURI = new URI(loginLocation);

		// берем параметры запроса
		String queryParamsStr = loginLocationURI.getQuery();

		// проверяем, что параметры запроса есть
		assertThat(queryParamsStr).isNotEmpty().isNotBlank().contains("=");

		// парсим параметры запроса в удобную структуру
		var queryParams = Arrays
				.stream(queryParamsStr.split("&"))
				.map(this::splitQueryParameter)
				.collect(
						Collectors.groupingBy(
								SimpleImmutableEntry::getKey,
								LinkedHashMap::new,
								Collectors.mapping(Map.Entry::getValue,
										Collectors.toList())));

		// проверяем, что в параметрах запроса есть login_challenge
		assertThat(queryParams).isNotEmpty().containsOnlyKeys(LOGIN_CHALLENGE);

		// запоминаем login_challenge
		loginChallenge = queryParams.get(LOGIN_CHALLENGE).get(0);
	}

	private void fullCycleAuth() throws UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {

		// given (instead of when)

		// when

		// ---------------------------------------------------------------------------------------
		// 4.1.1 Authorization Request

		// запрос на аутентификацию
		oauth2AuthRedirectToLogin();

		// check info loginChallenge
		StepVerifier
				.create(this.oryHydraService.loginRequestInfo(loginChallenge))
				.expectNextMatches(body -> body.getClient().getClientId().equals(clientId))
				.verifyComplete();

		// authorization with redirect
		var acceptLoginRequestModel = Mono.just(
				AcceptLoginRequestModelBuilder.builder()
					.setSubject(login)
					.setRemember(true)
					.setRememberFor(60L * 60L * 24L)
					.setContextModel("")
					.build());
	
		StepVerifier
				.create(this.oryHydraService.acceptLoginRequest(loginChallenge, acceptLoginRequestModel))
				.expectNextMatches(
					responseWithRedirectModel -> 
					responseWithRedirectModel
					.getRedirectTo()
					.equals(
						String.format(
								"[?($.redirect_to =~ /^https:\\/\\/%s\\/oauth2\\/auth\\?client_id=%s&code_challenge=%s&code_challenge_method=%s&login_verifier=.+&response_type=code&state=%s$/)]",
								"authorization-server.com", clientId, codeChallenge, codeChallengeMethod,
								state)
					)
				)
				.verifyComplete();
		
		
				
		// then (instead of verify)
	}

	@Test
	@DisplayName("Полный цикл аутентификации и авторизации пользователя. Обработка без ошибок")
	void fullCycleAuthentication() throws UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {

		fullCycleAuth();

	}

	// для парсинга QueryParameter URI
	private SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
		final int idx = it.indexOf("=");
		final String key = idx > 0 ? it.substring(0, idx) : it;
		final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
		return new SimpleImmutableEntry<>(
				URLDecoder.decode(key, StandardCharsets.UTF_8),
				URLDecoder.decode(value, StandardCharsets.UTF_8));
	}

	private static String generateStr() {
		// генерим случайную строки для state
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 30;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}

	private static String generateCodeVerifier() throws UnsupportedEncodingException {
		SecureRandom secureRandom = new SecureRandom();
		byte[] codeVerifier = new byte[32];
		secureRandom.nextBytes(codeVerifier);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
	}

	private static String generateCodeChallange(String codeVerifier)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] bytes = codeVerifier.getBytes("US-ASCII");
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(bytes, 0, bytes.length);
		byte[] digest = messageDigest.digest();

		return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
	}

}
