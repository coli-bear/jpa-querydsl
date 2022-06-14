package study.querydsl.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.domain.entity.Member;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.entity.QMember.member;
import static study.querydsl.domain.entity.QTeam.team;

@RequiredArgsConstructor
//public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     */
//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
//        return from(member).leftJoin(member.team, team)
//            .where(usernameEq(condition.getUsername()),
//                teamNameEq(condition.getTeamName()),
//                ageGoe(condition.getAgeGoe()),
//                ageLoe(condition.getAgeLoe()))
//            .select(
//                new QMemberTeamDto(
//                    member.id.as("memberId"),
//                    member.username,
//                    member.age,
//                    team.id.as("teamId"),
//                    team.name.as("teamName")
//                )
//            )
//            .fetch();
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
//        JPQLQuery<MemberTeamDto> jpaQuery = from(member).leftJoin(member.team, team)
//            .where(usernameEq(condition.getUsername()),
//                teamNameEq(condition.getTeamName()),
//                ageGoe(condition.getAgeGoe()),
//                ageLoe(condition.getAgeLoe()))
//            .select(
//                new QMemberTeamDto(
//                    member.id.as("memberId"),
//                    member.username,
//                    member.age,
//                    team.id.as("teamId"),
//                    team.name.as("teamName")
//                )
//            );

        // 장점 : 페이징이 아주 편리, querydsl에서 기능들이 편리
        // 단점 : sort가 안되고, from부터 오니까..., JPAQueryFactory로 시작할 수 없음, 메서드 체인이 끊김...
//        List<MemberTeamDto> results = getQuerydsl()
//            .applyPagination(pageable, jpaQuery)
//            .fetch();
//        return query.fetch();
        final JPAQuery query = queryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());
//
        JPAQuery<Long> countQuery = total(condition);
        List<MemberTeamDto> results = query.fetch();
//
//        // 페이지 사이즈보다 컨텐츠 사이즈보다 크면 count query 실행을 하지 않음
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
//        return new PageImpl<MemberTeamDto>(results, pageable, total);
//        return null;
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        final JPAQuery query = queryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        JPAQuery<Long> countQuery = total(condition);
        List<MemberTeamDto> results = query.fetch();

        // 페이지 사이즈보다 컨텐츠 사이즈보다 크면 count query 실행을 하지 않음
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
//        return new PageImpl<MemberTeamDto>(results, pageable, total);
//        return null;
    }

    private JPAQuery<Long> total(MemberSearchCondition condition) {
        return queryFactory.select(member.count()).from(member).where(
            usernameEq(condition.getUsername()),
            teamNameEq(condition.getTeamName()),
            ageGoe(condition.getAgeGoe()),
            ageLoe(condition.getAgeLoe())
        );
//        return null;
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
