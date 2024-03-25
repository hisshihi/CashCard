package com.example.cashCard;

import com.example.cashCard.domain.entity.CashCardEntity;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

	@Autowired
//	Обращение к локальным HTTP запросам
	private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
//		HTTP запрос к конченой точке cashcards/99
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isNotNull();

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

//	Тест для првоерки, создаётся ли новая карта
	@Test
//	DirtiesContext используется для очистки контекста, для того, чтобы удалить из кэша данные, которые могут навредить другим тестам
	// https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-dirtiescontext.html
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCardEntity cashCardEntity = TestDataUtil.cashCardEntityA(null, 250.00, null);
		// Вызывается метод restTemplate.postForEntity("/cashcards", cashCardEntity, Void.class),
		// который отправляет POST-запрос на сервер с указанными данными.
		ResponseEntity<Void> createResponse =
				restTemplate
						.withBasicAuth("hiss", "abc123")
						.postForEntity("/cashcards", cashCardEntity, Void.class);
//		Проверяется, что статус ответа сервера равен HttpStatus.CREATED, что означает успешное создание новой карты.
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

//		Из ответа сервера извлекается URI новой карты.
		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
//		Вызывается метод restTemplate.getForEntity(locationOfNewCashCard, String.class),
//		который отправляет GET-запрос на сервер для получения информации о созданной карте.
		ResponseEntity<String> getResponse =
				restTemplate
						.withBasicAuth("hiss", "abc123")
						.getForEntity(locationOfNewCashCard, String.class);
//		Проверяется, что статус ответа сервера равен HttpStatus.OK, что означает успешное получение информации о карте.
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}


//	Проверяю приходит ли по page данные
	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void shouldReturnASortedPageOfCashCards() {
//		Создаём заправшиваемый url и сортируем его по убыванию(desc)
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,asc", String.class);
//		Проверяем статус, должен быть OK
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

//		Создаём контекст документа и с помощью него можем извлечь данные из json
		DocumentContext documentContext = JsonPath.parse(response.getBody());
//		Выбираем все данные и проверяем их размер
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);
// 		Проверяем сортировку
		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(1.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$.[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("hiss", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

//	Тест для проверки роли пользователя
//	Пользователь с ролью "NON-OWNER" не может оперировать данными карт
	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-owns-no-cards", "qwe123")
				.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

//	Тест который проверяет, может ли получить доступ к карте только её владелец
	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards/102", String.class); // Это карта Арины

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	// Тест для проверки метода PUT
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		CashCardEntity cashCardEntity = TestDataUtil.cashCardEntityA(null, 19.99, null);
		// Инкапсулируем объект CashCardEntity для отправки в запросе HTTP
		/*
		* Версия exchange является более общей версией методов
		* exchange требует, чтобы в качестве параметров были указаны глагол и тело запроса
		*
		* В общем, строка HttpEntity<CashCardEntity> request = new HttpEntity<>(cashCardEntity); используется для создания объекта HttpEntity,
		*  который инкапсулирует объект CashCardEntity в качестве тела запроса,
		*  который затем отправляется как часть HTTP PUT-запроса для обновления объекта CashCardEntity на сервере.
		* */
		HttpEntity<CashCardEntity> request = new HttpEntity<>(cashCardEntity);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				// Отправляем запрос PUT по указонному адресу
				.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

//		Првоеряем приходят ли правильные данные при обновлении карточки
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

//	Попытка обновить несуществующую карту
	@Test
	void shouldNotUpdateACashCardThatDoesNotExist() {
		CashCardEntity unknownCashCardEntity = TestDataUtil.cashCardEntityA(null, 19.99, null);
		HttpEntity<CashCardEntity> request = new HttpEntity<>(unknownCashCardEntity);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.exchange("/cashcards/999999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

//	Метод PUT не может обновить карточку, не принадлежащую пользователю
	@Test
	void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
		CashCardEntity arinaCashCardEntity = TestDataUtil.cashCardEntityA(null, 333.33, null);
		HttpEntity<CashCardEntity> request = new HttpEntity<>(arinaCashCardEntity);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

//	Тестирования метода удаления карточки
	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("hiss", "abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		//	Проверяем удалена ли карта
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("hiss", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

//	Тест для проверки удаления несуществующей карты
	@Test
	void shouldNotDeleteACashCardThatDoesNotExists() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("hiss", "abc123")
				.exchange("/cashcards/999999", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

//	Тест на првоерку может ли пользователь удалить не свою карту
	@Test
	void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("hiss", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}


}
