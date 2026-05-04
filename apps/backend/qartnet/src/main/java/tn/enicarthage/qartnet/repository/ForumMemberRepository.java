package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Forum;
import tn.enicarthage.qartnet.model.ForumMember;
import tn.enicarthage.qartnet.model.User;

import java.util.List;
import java.util.Optional;

public interface ForumMemberRepository extends JpaRepository<ForumMember, Long> {
    Optional<ForumMember> findByForumAndUser(Forum forum, User user);
    List<ForumMember> findByForum(Forum forum);
    boolean existsByForumAndUser(Forum forum, User user);
}

