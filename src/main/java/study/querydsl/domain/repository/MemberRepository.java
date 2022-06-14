package study.querydsl.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import study.querydsl.domain.entity.Member;

import java.util.List;

// 인터페이스는 여러개를 상속 가능하며
// 상속 받으면 Impl로 되어있는 기능을 호출해서 사용할 수 있다.
// QuerydslPredicateExecutor (where 조건절에 넣는 부분으로 제공하는거)
// 단점 : 조인 불가, 애플리케이션이 querydsl에 의존적이 됨
public interface MemberRepository
    extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    // 간단한 정적 조회 쿼리는 이렇게 구현하면 됨
    List<Member> findByUsername(String username);
}
