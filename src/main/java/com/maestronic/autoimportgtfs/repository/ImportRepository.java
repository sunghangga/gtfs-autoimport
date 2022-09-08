package com.maestronic.autoimportgtfs.repository;

import com.maestronic.autoimportgtfs.entity.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<Import, Integer> {

    @Query("SELECT i FROM Import i WHERE i.fileType = :fileType" +
            " ORDER BY i.id DESC")
    List<Import> findImportDataByFileType(@Param("fileType") String fileType, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE" +
            " ELSE FALSE END" +
            " FROM Import i WHERE i.fileName = :fileName")
    boolean existsByFileName(@Param("fileName") String fileName);
}
