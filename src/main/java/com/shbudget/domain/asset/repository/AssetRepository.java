package com.shbudget.domain.asset.repository;

import com.shbudget.domain.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findAllByBookId(Long bookId);

    Optional<Asset> findByIdAndBookId(Long id, Long bookId);

    @Query("SELECT SUM(a.balance) FROM Asset a WHERE a.bookId = :bookId")
    Long sumBalanceByBookId(@Param("bookId") Long bookId);

    boolean existsByIdAndBookId(Long id, Long bookId);
}
