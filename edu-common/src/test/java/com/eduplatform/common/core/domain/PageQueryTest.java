package com.eduplatform.common.core.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageQueryTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidPageNumberAndSize() {
        PageQuery query = new PageQuery();
        query.setPageNum(0);
        query.setPageSize(101);

        assertThat(validator.validate(query)).hasSize(2);
    }

    @Test
    void acceptsBoundaryValues() {
        PageQuery query = new PageQuery();
        query.setPageNum(1);
        query.setPageSize(100);

        assertThat(validator.validate(query)).isEmpty();
    }
}
