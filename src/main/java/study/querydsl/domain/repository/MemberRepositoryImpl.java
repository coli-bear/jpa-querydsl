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

        // ?????? : ???????????? ?????? ??????, querydsl?????? ???????????? ??????
        // ?????? : sort??? ?????????, from?????? ?????????..., JPAQueryFactory??? ????????? ??? ??????, ????????? ????????? ??????...
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
//        // ????????? ??????????????? ????????? ??????????????? ?????? count query ????????? ?????? ??????
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

        // ????????? ??????????????? ????????? ??????????????? ?????? count query ????????? ?????? ??????
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
