package com.example.cashCard.jsonTest;

import com.example.cashCard.TestDataUtil;
import com.example.cashCard.domain.entity.CashCardEntity;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {

    @Autowired
    private JacksonTester<CashCardEntity> json;
    @Autowired
    private JacksonTester<CashCardEntity[]> jsonList;
    private CashCardEntity[] cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.array(
                TestDataUtil.cashCardEntityA(99L, 123.45, "hiss"),
                TestDataUtil.cashCardEntityA(100L, 1.00, "hiss"),
                TestDataUtil.cashCardEntityA(101L, 150.00, "hiss")
        );
    }

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCardEntity cashCardEntity = TestDataUtil.cashCardEntityA(99L, 123.45, "hiss");

        assertThat(json.write(cashCardEntity)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCardEntity)).extractingJsonPathNumberValue("@.id").isEqualTo(99);

        assertThat(json.write(cashCardEntity)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCardEntity)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected = """
                {
                "id": 99,
                "amount": 123.45,
                "owner": "hiss"
                }
                """;
        CashCardEntity cashCardEntity = TestDataUtil.cashCardEntityA(99L, 123.45, "hiss");
        assertThat(json.parse(expected)).isEqualTo(cashCardEntity);
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        String expected = """
                [
                  {"id": 99, "amount": 123.45, "owner": "hiss" },
                  {"id": 100, "amount": 1.00, "owner": "hiss" },
                  {"id": 101, "amount": 150.00, "owner": "hiss" }
                ]
                """;
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson(expected);
    }

}
