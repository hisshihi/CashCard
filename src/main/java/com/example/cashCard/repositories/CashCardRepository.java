package com.example.cashCard.repositories;


import com.example.cashCard.domain.entity.CashCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashCardRepository extends CrudRepository<CashCardEntity, Long>, PagingAndSortingRepository<CashCardEntity, Long> {
//    Метод поиска карточки по ID и владельцу
    CashCardEntity findByIdAndOwner(Long id, String owner);
    // Метод поиска карточек по владельцу
    Page<CashCardEntity> findByOwner(String owner, PageRequest request);
}
