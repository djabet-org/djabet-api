package hello.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import hello.Roll;

@Repository
public interface CrashRepository extends PagingAndSortingRepository<Crash, Long> {

   Page<Crash> findByPlatform(String platform, Pageable pageable);

}
