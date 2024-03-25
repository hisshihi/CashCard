package com.example.cashCard.controllers;

import com.example.cashCard.domain.entity.CashCardEntity;
import com.example.cashCard.services.CashCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cashcards")
public class CashCardContoller {

    private final CashCardService cashCardService;

//    Контроллер для метода Get, получения карточки по id
    @GetMapping("/{requestId}")
    private ResponseEntity<CashCardEntity> findById(@PathVariable Long requestId, Principal principal) {
        // Если карта не пренадлежит пользователю, возвращаем 404
//        Optional<CashCardEntity> cashCardEntity = Optional.ofNullable(cashCardService.findByIdAndOwner(requestId,  principal.getName())); // principal.getName() - получаем имя пользвоателя из базовой аунтификации
        CashCardEntity cashCardEntity = cashCardService.findByIdAndOwner(requestId, principal.getName());
        if (cashCardEntity != null) {
            return ResponseEntity.ok(cashCardEntity);
        } else return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<String> createCashCard(@RequestBody CashCardEntity cashCardEntity, UriComponentsBuilder ucb, Principal principal) {
//        Создаём новую карточку для того, чтобы убедиться, что её создал нужный нам пользователь
        CashCardEntity cashCardEntityWithOwner = new CashCardEntity(null, cashCardEntity.getAmount(), principal.getName());
        CashCardEntity savedCashCard = cashCardService.saveCashCard(cashCardEntityWithOwner);
//        В URI указывается путь к ресурсу карты и его идентификатор.
        URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.getId()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping
    private ResponseEntity<List<CashCardEntity>> findAllCashCards(Pageable pageable, Principal principal) {
//      Создаём экземпляр страницы, куда передаём сущность и сохраняем в репозиторий
        // В качестве параметра передаём PageRequest для того, чтобы получить номера страниц(установлено по умолчанию),
        // размер(по умолчанию) и сортировку, которую мы сделали по полю amount и по увеличению
        Page<CashCardEntity> page = cashCardService.findByOwner(principal.getName(), PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
        ));
        return ResponseEntity.ok(page.getContent());
    }

//    Контроллер для метода Put, обновления существующей карточки
    @PutMapping("/{requestId}")
//    Принимаем в http запросе id карты, тело и принадлежащего её пользователю
    private ResponseEntity<CashCardEntity> putCashCard(@PathVariable Long requestId, @RequestBody CashCardEntity cashCardEntityUpdate, Principal principal) {
        // Ищем карту в базе данных
        CashCardEntity cashCardEntity = cashCardService.findByIdAndOwner(requestId, principal.getName());
        if (cashCardEntity != null) {
            CashCardEntity updatedCashCard = new CashCardEntity(cashCardEntity.getId(), cashCardEntityUpdate.getAmount(), principal.getName());
            cashCardService.saveCashCard(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

//    Контроллер для удаления карты
    @DeleteMapping("/{requestId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestId, Principal principal) {
        CashCardEntity cashCard = cashCardService.findByIdAndOwner(requestId, principal.getName());
        if (cashCard != null) {
            cashCardService.deleteCashCardById(requestId);
            return ResponseEntity.noContent().build();
        } else return ResponseEntity.notFound().build();
    }

}
