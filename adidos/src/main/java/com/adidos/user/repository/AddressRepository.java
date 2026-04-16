package com.adidos.user.repository;

import com.adidos.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // Sắp xếp địa chỉ mặc định lên đầu, sau đó đến địa chỉ mới nhất
    List<Address> findByUserIdOrderByIsDefaultDescIdDesc(Long userId);
}