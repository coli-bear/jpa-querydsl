package study.querydsl.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.entity.Member;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.domain.entity.QMember.member;
import static study.querydsl.domain.entity.QTeam.team;


// 순수 JPA Repository
@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    // em에서 에러나는거는 인텔리제이가 인식 못하는거라 실행해서 문제 없으면 그냥 진행하면 됨
//    public MemberJpaRepository(EntityManager em) {
//        this.em = em;
    // 취향타는데 이거 spring bean에 등록해도 됨
    // 단점은 test code 짜는데 귀찮아짐...
    // 자세히 알고 싶으면 JPA 책 13.1 gogo
//        this.queryFactory = new JPAQueryFactory(em);
//    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findAll_QueryDsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUserName(String username) {
        return em.createQuery("select m from Member m where m.username = :username")
            .setParameter("username", username)
            .getResultList();
    }

    public List<Member> findByUserName_QueryDsl(String username) {
        return queryFactory.selectFrom(member)
            .where(usernameEq(username))
            .fetch();
    }


    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }

        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeGoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
            .select(
                new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(
                new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }
}
