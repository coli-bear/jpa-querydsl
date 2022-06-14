package study.querydsl.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"username", "age"})
public class MemberDto {
    private String username;
    private Integer age;

    public MemberDto() {}

    public MemberDto(String username, Integer age) {
        this.username = username;
        this.age = age;
    }

}
