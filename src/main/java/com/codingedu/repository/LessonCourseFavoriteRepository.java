package com.codingedu.repository;

import com.codingedu.entity.LessonCourseFavorite;
import com.codingedu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonCourseFavoriteRepository extends JpaRepository<LessonCourseFavorite, Long> {
    List<LessonCourseFavorite> findByUser(User user);
    boolean existsByUserAndLang(User user, String lang);
    void deleteByUserAndLang(User user, String lang);
}
