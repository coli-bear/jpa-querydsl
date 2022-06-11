package study.querydsl.dto.common;

import lombok.Data;
import study.querydsl.enums.ResponseCode;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Data
public class ResponseOneData<T> {
    private LocalDateTime time;
    private String url;
    private ResponseCode code;
    private T data;

    public ResponseOneData(T data, String url, ResponseCode code) {
        this.time = LocalDateTime.now();
        this.url = url;
        this.data = data;
        this.code = code;
    }

    public static ResponseOneData<String> createHello() {
        return new ResponseOneData<>(
            "hello",
            "http://localhost:8080/hello",
            ResponseCode.SUCCESS
        );
    }

    private static <T> ResponseOneData create(
        T data, HttpServletRequest request, ResponseCode code
    ) {
        return new ResponseOneData(data, request.getRequestURL().toString(), code);
    }

    public static <T> ResponseOneData createForSuccess(
        T data, HttpServletRequest request
    ) {
        return create(data, request, ResponseCode.SUCCESS);
    }
    public static <T> ResponseOneData createForFail(
        T data, HttpServletRequest request
    ) {
        return create(data, request, ResponseCode.FAIL);
    }
}
