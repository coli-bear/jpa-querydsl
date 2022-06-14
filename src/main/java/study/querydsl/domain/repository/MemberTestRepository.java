package study.querydsl.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.entity.Member;
import study.querydsl.domain.entity.QTeam;
import study.querydsl.domain.repository.common.CustomQueryDslRepositorySupport;
import study.querydsl.dto.MemberSearchCondition;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.entity.QMember.member;
import static study.querydsl.domain.entity.QTeam.team;

@Repository
public class MemberTestRepository extends CustomQueryDslRepositorySupport {
    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
            .from(member)
            .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
            .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> memberQuery = selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            );

        List<Member> content = getQuerydsl().applyPagination(pageable, memberQuery).fetch();

        return PageableExecutionUtils.getPage(content, pageable, memberQuery::fetchCount);

    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> result = applyPagination(pageable, query -> query
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
        );
        return result;
    }

    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> result = applyPagination(pageable, contentQuery -> contentQuery
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            ), countQuery -> countQuery
            .select(member.id)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
        );
        return result;
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

