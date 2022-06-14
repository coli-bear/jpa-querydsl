package study.querydsl.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"name", "age"})
public class UserDto {
    private String name;
    private Integer age;

    public UserDto(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
