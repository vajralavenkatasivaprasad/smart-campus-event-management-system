

package com.campus.ems.repository;

import com.campus.ems.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByTargetRoleInOrderByIsPinnedDescCreatedAtDesc(List<Announcement.TargetRole> roles);
}
