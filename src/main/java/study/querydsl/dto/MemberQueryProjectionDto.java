package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"username", "age"})
public class MemberQueryProjectionDto {
    private String username;
    private Integer age;

    public MemberQueryProjectionDto() {}

    // 이거 있으면 DTO가 QFile로 생성 됨... ㅋㅋ
    @QueryProjection
    public MemberQueryProjectionDto(String username, Integer age) {
        this.username = username;
        this.age = age;
    }

}
