package com.example.cashCard.services.impl;

import com.example.cashCard.domain.entity.CashCardEntity;
import com.example.cashCard.repositories.CashCardRepository;
import com.example.cashCard.services.CashCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashCardServiceImpl implements CashCardService {

    private final CashCardRepository cashCardRepository;

    @Override
    public Optional<CashCardEntity> findCashCardById(Long requestId) {
        return cashCardRepository.findById(requestId);
    }

    @Override
    public CashCardEntity saveCashCard(CashCardEntity cashCardEntity) {
        return cashCardRepository.save(cashCardEntity);
    }

    @Override
    public Page<CashCardEntity> findAllCashCards(PageRequest of) {
        return cashCardRepository.findAll(of);
    }

    @Override
    public CashCardEntity findByIdAndOwner(Long requestId, String name) {
        CashCardEntity cashCardEntityWithOwner = cashCardRepository.findByIdAndOwner(requestId, name);
        return cashCardEntityWithOwner;
    }

    @Override
    public Page<CashCardEntity> findByOwner(String name, PageRequest amount) {
        return cashCardRepository.findByOwner(name, amount);
    }


}
