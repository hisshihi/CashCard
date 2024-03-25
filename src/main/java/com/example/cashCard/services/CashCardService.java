package com.example.cashCard.services;

import com.example.cashCard.domain.entity.CashCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface CashCardService {
    Optional<CashCardEntity> findCashCardById(Long requestId);

    CashCardEntity saveCashCard(CashCardEntity cashCardEntity);


    Page<CashCardEntity> findAllCashCards(PageRequest of);

    CashCardEntity findByIdAndOwner(Long requestId, String name);

    Page<CashCardEntity> findByOwner(String name, PageRequest amount);

    void deleteCashCardById(Long requestId);
}
