package com.springboot.board.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

@DisplayName("테스트 도구 - Form 데이터 인코더")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {FormDataEncoder.class, ObjectMapper.class})
class FormDataEncoderTest {

    private final FormDataEncoder formDataEncoder;

    FormDataEncoderTest(@Autowired FormDataEncoder formDataEncoder) {
        this.formDataEncoder = formDataEncoder;
    }

    @DisplayName("Object -> url encoded String(Form body data)")
    @Test
    void givenObject_whenEncoding_thenReturnsFormEncodedString() {
        TestObject obj = new TestObject(
            "This 'is' \"test\" string.",
            List.of("hello", "my", "friend").toString().replace(" ", ""),
            String.join(",", "hello", "my", "friend"),
            null,
            1234,
            3.14,
            false,
            BigDecimal.TEN,
            TestEnum.THREE);

        String result = formDataEncoder.encode(obj);
        System.out.println(result);
        assertThat(result).isEqualTo(
    "str=This%20'is'%20%22test%22%20string." +
            "&listStr1=%5Bhello,my,friend%5D" +
            "&listStr2=hello,my,friend" +
            "&nullStr" +
            "&number=1234" +
            "&floatingNumber=3.14" +
            "&bool=false" +
            "&bigDecimal=10" +
            "&testEnum=THREE");
    }

    record TestObject(String str, String listStr1, String listStr2, String nullStr,
                        Integer number, Double floatingNumber, Boolean bool, BigDecimal bigDecimal, TestEnum testEnum) {}

    enum TestEnum {
        ONE, TWO, THREE
    }

}
