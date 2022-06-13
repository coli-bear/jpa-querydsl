package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.entity.Member;
import study.querydsl.domain.entity.QMember;
import study.querydsl.domain.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.entity.QMember.*;
import static study.querydsl.domain.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {
    @PersistenceContext
    private EntityManager em;

    private JPAQueryFactory query;

    @BeforeEach
    public void before() {
        query = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        // find member1
        String qlString =
            "select m from Member m" +
                " where m.username = :username";
        Member findMember1 = em.createQuery(qlString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

        assertThat(findMember1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() {
        // compileQuerydsl 실행해서 Q파일을 만들어야 함
//        QMember m = new QMember("member1");
//        QMember m = QMember.member;

        String findMemberName = "member1";

//        Member findMember = query.selectFrom(m)
//            .where(m.username.eq(findMemberName))
//            .fetchOne();

//        Member findMember = query.selectFrom(QMember.member)
//            .where(QMember.member.username.eq(findMemberName))
//            .fetchOne();

        // 이거 권장
        Member findMember = query

//            아래 두 개는 selectFrom으로 병합 가능
//            .select(member)
//            .from(member)

            .selectFrom(member)
//            where 조건절은 BooleanBuilder 을 이용해서도 가능하다.
//            .where(member.username.eq(findMemberName))

//            검색 조건 사용 법
//            and 는 아래 두 방식으로 사용하면 된다.(개인적으로는 아래가 보기 편함
//            .where(eqMemberName(findMemberName).and(eqMemberAge(10)))
            .where(
                eqMemberName(findMemberName),
                eqMemberAge(10)
            )
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo(findMemberName);
    }

    // 결과 조회 법
    @Test
    public void resultFetch() {
        // given
        List<Member> fetch = query.selectFrom(member).fetch();
        Member fetchOne = query.selectFrom(member).fetchOne();
        Member fetchFirst = query.selectFrom(QMember.member).fetchFirst();

//        이거 사용안하는듯..?
//        어차피 쿼리 2 번 나감 아래 예시 사용 하는거 권장

//        QueryResults<Member> fetchResults = query.selectFrom(member).fetchResults();
//        long total = fetchResults.getTotal();
//        List<Member> results = fetchResults.getResults();


//        이거 사용안하는듯..?
//        long total = query.selectFrom(member).fetchCount();
//        이거 쓰면 될

        long count = query.selectFrom(member).stream().count();
        List<Member> results = query.selectFrom(member).fetch();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순 (desc)
     * 2. 회원 이름 오름차순 (asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    public void sort() {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        extracted();
        // when

        List<Member> results = query.selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        Member member5 = results.get(0);
        Member member6 = results.get(1);
        Member memberNull = results.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isEqualTo(null);
    }

    @Test
    public void paging1() {
        List<Member> result = query.selectFrom(member)
            .orderBy(member.username.desc())
            // paging 할때 이거 사용
            .offset(1)
            .limit(2)
            .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> result = query.selectFrom(member)
            .orderBy(member.username.desc())
            // paging 할때 이거 사용
            .offset(1)
            .limit(2)
            .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getResults().size()).isEqualTo(2);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = query.select(
            member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
            .from(member)
            .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     *
     * @throws Exception
     */
    @Test
    public void group() throws Exception {
        List<Tuple> results = query.select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
//            .having()
            .fetch();

        Tuple teamA = results.get(0);
        Tuple teamB = results.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * TeamA에 소속된 회원
     */
    @Test
    public void join() {
        List<Member> results = query.selectFrom(member)
            .join(member.team, team)
//            .rightJoin(member.team, team)
//            .leftJoin(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        assertThat(results)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(막조인)
     * 회원의 이름과 팀 이름이 같은 회원 조회
     * outer join이 불가능하다...(과거에는)
     * 최신 버전에서는 연관관계가 없는 데이블도 join on 절을 사용해서 조인 가능
     */
    @Test
    public void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> results = query.select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();

        assertThat(results)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }


    /**
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void joinOnFiltering() {
        List<Tuple> result = query.select(member, team)
            .from(member)
            // left join 인 경우에는 on 절을 이용해서 필터링(where 에서는 제대로 조회 안될 수 있다)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();

        result.stream().forEach(tuple ->
            System.out.println("tuple: " + tuple)
        );
    }

    /**
     * 연관관계가 없는 entity 외부 조인할 때
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> results = query.select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

        results.forEach(tuple ->
            System.out.println("tuple: " + tuple)
        );
    }

    @PersistenceUnit
    private EntityManagerFactory emf;

    /**
     * fetch join : sql 에서 제공하는 기능이 아닌 sql 조인을 활용해서 연관된 엔티티를 한번에 조회 하는 기능
     */
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = query.selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        // 로딩 된 엔티티인지 초기화 안된 엔티티인지 알려주는 애
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = query.selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

        // 로딩 된 엔티티인지 초기화 안된 엔티티인지 알려주는 애
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isTrue();
    }


    /**
     * 나이가 가장 많은 회원 조회
     * <p>
     * JPA Subquery 의 단점
     * - from 절(inline view) 는 제공되지 않는다.
     * - 서브쿼리는 join으로 변경 가능하기 떄문에 바꿀 수 있으면 바꿔라
     * - 애플리케이션에서 쿼리를 2번 실행
     * - nativeSQL을 사용
     */
    @Test
    public void subQuery() {
        // 서브 쿼리는 JPAExpressions 를 이용해서 사용해야 한다.
        // 서브 쿼리나 동일 테이블을 조회하는 경우 Q 크랠스가 중복되면 안되기 때문에 아래와 같이
        // 추가로 선언해서 사용해야만 한다.
        QMember memberSub = new QMember("memberSub");

        List<Member> results = query.selectFrom(member)
            .where(member.age.eq(
                JPAExpressions.select(memberSub.age.max())
                    .from(memberSub)
            ))
            .fetch();
        assertThat(results).extracting("age").containsExactly(40);
    }

    /**
     * 나이가 가장 평균 이상 회원 조회
     */
    @Test
    public void subQueryGoe() {
        // 서브 쿼리는 JPAExpressions 를 이용해서 사용해야 한다.
        // 서브 쿼리나 동일 테이블을 조회하는 경우 Q 크랠스가 중복되면 안되기 때문에 아래와 같이
        // 추가로 선언해서 사용해야만 한다.
        QMember memberSub = new QMember("memberSub");

        List<Member> results = query.selectFrom(member)
            .where(member.age.goe(
                JPAExpressions.select(memberSub.age.avg())
                    .from(memberSub)
            ))
            .fetch();
        assertThat(results).extracting("age").containsExactly(30, 40);
    }

    /**
     * 나이가 10살 이상 회원 조회
     */
    @Test
    public void subQueryIn() {
        // 서브 쿼리는 JPAExpressions 를 이용해서 사용해야 한다.
        // 서브 쿼리나 동일 테이블을 조회하는 경우 Q 크랠스가 중복되면 안되기 때문에 아래와 같이
        // 추가로 선언해서 사용해야만 한다.
        QMember memberSub = new QMember("memberSub");

        List<Member> results = query.selectFrom(member)
            .where(member.age.in(
                JPAExpressions.select(memberSub.age)
                    .from(memberSub)
                    .where(memberSub.age.gt(10))
            ))
            .fetch();
        assertThat(results).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        // 서브 쿼리는 JPAExpressions 를 이용해서 사용해야 한다.
        // 서브 쿼리나 동일 테이블을 조회하는 경우 Q 크랠스가 중복되면 안되기 때문에 아래와 같이
        // 추가로 선언해서 사용해야만 한다.
        QMember memberSub = new QMember("memberSub");

        List<Tuple> results = query.select(member.username,
            JPAExpressions.select(memberSub.age.avg())
                .from(memberSub)
        ).from(member)
            .fetch();

        results.forEach(tuple ->
            System.out.println("tuple: " + tuple)
        );
    }

    // db에서는 데이터 줄이는 역할만하고
    // 데이터를 변경하는 경우는 애플리케이션 또는 프레젠테이션 레이어에서 할 것
    @Test
    public void basicCase() {
        List<String> results = query.select(member.age
            .when(10).then("열살")
            .when(20).then("스무살")
            .otherwise("늙은 화석")
        )
            .from(member)
            .fetch();

//        assertThat(results).extracting("age").containsExactly("열살", "스무살", "늙은 화석", "늙은 화석");
        for (String result : results) {
            System.out.println("age : " + result);
        }
    }

    @Test
    public void complexCase() {
        List<String> results = query.select(new CaseBuilder()
            .when(member.age.between(0, 20)).then("0~20")
            .otherwise("21~")
        ).from(member)
            .fetch();

        results.forEach(result -> {
            System.out.println("나이는? " + result);
        });
    }

    @Test
    public void constant() {
        List<Tuple> results = query.select(member.username, Expressions.constant("A"))
            .from(member)
            .fetch();

        for (Tuple result : results) {
            System.out.println("result : " + result);
        }
    }

    @Test
    public void concat() {
        // username_age
        // stringValue는 enum 처리할 떄 많이 사용 함.
        String ages = query.select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(ages).isEqualTo("member1_10");
    }

    private void extracted() {
        em.flush();
        em.clear();
    }

    private BooleanExpression eqMemberAge(int age) {
        return member.age.eq(age);
    }

    private BooleanExpression eqMemberName(String findMemberName) {
        return member.username.eq(findMemberName);
    }
}
