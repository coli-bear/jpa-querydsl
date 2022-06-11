package study.querydsl.dto.common;

import lombok.Data;
import study.querydsl.enums.ResponseCode;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class ResponseCollectionData<T> {
    private LocalDateTime time;
    private String url;
    private ResponseCode code;
    private Collection<T> data;

    public ResponseCollectionData(Collection<T> data, String url, ResponseCode code) {
        this.time = LocalDateTime.now();
        this.url = url;
        this.data = data;
        this.code = code;
    }

    private static <T> ResponseCollectionData create(
        Collection<T>  data, HttpServletRequest request, ResponseCode code
    ) {
        return new ResponseCollectionData(data, request.getRequestURL().toString(), code);
    }

    public static <T> ResponseCollectionData createForSuccess(
        Collection<T> data, HttpServletRequest request
    ) {
        return create(data, request, ResponseCode.SUCCESS);
    }
    public static <T> ResponseCollectionData createForFail(
        Collection<T> data, HttpServletRequest request
    ) {
        return create(data, request, ResponseCode.FAIL);
    }
}
