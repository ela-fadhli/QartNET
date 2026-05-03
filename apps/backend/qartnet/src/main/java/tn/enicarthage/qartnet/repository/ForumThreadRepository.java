package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.enicarthage.qartnet.model.ForumThread;
import java.util.Optional;
import java.util.UUID;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    Optional<ForumThread> findByPublicId(UUID publicId);

    @Query(
            value = """                                                                                                                                                                         
              SELECT DISTINCT t FROM ForumThread t
              LEFT JOIN t.tags tag
              WHERE (:categoryPublicId IS NULL OR t.forumCategory.publicId = :categoryPublicId)
              AND   (:tagPublicId      IS NULL OR tag.publicId        = :tagPublicId)
              """,
            countQuery = """                                                                                                                                                                    
              SELECT COUNT(DISTINCT t) FROM ForumThread t
              LEFT JOIN t.tags tag
              WHERE (:categoryPublicId IS NULL OR t.forumCategory.publicId = :categoryPublicId)
              AND   (:tagPublicId      IS NULL OR tag.publicId        = :tagPublicId)
              """
    )
    Page<ForumThread> findFiltered(
            @Param("categoryPublicId") UUID categoryPublicId,
            @Param("tagPublicId") UUID tagPublicId,
            Pageable pageable
    );
}
