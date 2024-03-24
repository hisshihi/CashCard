package com.example.cashCard;

import com.example.cashCard.domain.entity.CashCardEntity;

public class TestDataUtil {

    public static CashCardEntity cashCardEntityA(Long id, Double amount, String owner) {
        return CashCardEntity.builder()
                .id(id)
                .amount(amount)
                .owner(owner)
                .build();
    }

}
