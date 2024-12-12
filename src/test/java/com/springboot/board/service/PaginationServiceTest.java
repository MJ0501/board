package com.springboot.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaginationService.class)
@DisplayName("비즈니스로직 - 페이징처리")
class PaginationServiceTest {
    private final PaginationService sut;

    PaginationServiceTest(@Autowired PaginationService paginationService) {
        this.sut = paginationService;
    }

    @DisplayName("현재페이지, total pages 로 PagingBarList 생성")
    @MethodSource
    @ParameterizedTest(name = "[{index}] {0}, {1} => {2}")
     void givenCurrentPageNAndTotalPages_thenReturnPagingBarNumbers(int currentPage, int totalPages, List<Integer> expected) {
        List<Integer> actual = sut.getPagingBarNumbers(currentPage, totalPages);
        assertThat(actual).isEqualTo(expected);
    }
    static Stream<Arguments> givenCurrentPageNAndTotalPages_thenReturnPagingBarNumbers(){
        return Stream.of(
          arguments(0, 13, List.of(0,1,2,3,4)),
          arguments(2, 13, List.of(0,1,2,3,4)),
          arguments(4, 13, List.of(2,3,4,5,6)),
          arguments(6, 13, List.of(4,5,6,7,8)),
          arguments(10, 13, List.of(8,9,10,11,12)),
          arguments(11, 13, List.of(9,10,11,12)),
          arguments(12, 13, List.of(10,11,12))
        );
    }
    @DisplayName("현재 설정되어 있는 페이지네이션 바의 길이를 알려준다.")
    @Test
    void givenNothing_whenCalling_thenReturnsCurrentBarLength() {
        int barLength = sut.currentBarLength();
        assertThat(barLength).isEqualTo(5);
    }
}