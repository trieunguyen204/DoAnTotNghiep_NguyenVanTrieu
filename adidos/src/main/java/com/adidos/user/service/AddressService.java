package com.adidos.user.service;

import com.adidos.user.dto.AddressRequest;
import com.adidos.user.dto.AddressResponse;
import com.adidos.user.entity.Address;
import com.adidos.user.entity.User;
import com.adidos.user.repository.AddressRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void addAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // Nếu đây là địa chỉ đầu tiên hoặc user chọn làm mặc định, reset các địa chỉ cũ
        List<Address> currentAddresses = addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
        boolean isDefault = currentAddresses.isEmpty() || (request.getIsDefault() != null && request.getIsDefault());

        if (isDefault) {
            currentAddresses.forEach(a -> a.setIsDefault(false));
            addressRepository.saveAll(currentAddresses);
        }

        Address address = Address.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressDetail(request.getAddressDetail())
                .isDefault(isDefault)
                .build();
        addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId, String email) {
        Address address = addressRepository.findById(addressId).orElseThrow();
        if (!address.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Không có quyền xóa!");
        }
        addressRepository.delete(address);
    }

    private AddressResponse toResponse(Address address) {
        String fullAddress = String.format("%s, %s, %s, %s",
                address.getAddressDetail(), address.getWard(), address.getDistrict(), address.getProvince());
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .fullAddress(fullAddress)
                .isDefault(address.getIsDefault())
                .build();
    }
}