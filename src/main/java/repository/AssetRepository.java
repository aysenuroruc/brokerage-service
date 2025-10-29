package repository;

import jakarta.persistence.LockModeType;
import model.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);
    List<Asset> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.customer.id = :customerId AND a.assetName = :assetName")
    Optional<Asset> findByCustomerIdAndAssetNameWithLock(
            @Param("customerId") Long customerId,
            @Param("assetName") String assetName
    );
}
