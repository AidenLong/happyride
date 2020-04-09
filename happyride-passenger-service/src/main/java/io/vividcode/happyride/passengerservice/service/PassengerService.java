package io.vividcode.happyride.passengerservice.service;

import com.google.common.collect.Streams;
import io.vividcode.happyride.passengerservice.api.web.CreatePassengerRequest;
import io.vividcode.happyride.passengerservice.api.web.CreateUserAddressRequest;
import io.vividcode.happyride.passengerservice.api.web.PassengerView;
import io.vividcode.happyride.passengerservice.api.web.UserAddressView;
import io.vividcode.happyride.passengerservice.dataaccess.PassengerRepository;
import io.vividcode.happyride.passengerservice.domain.Passenger;
import io.vividcode.happyride.passengerservice.domain.UserAddress;
import io.vividcode.happyride.passengerservice.support.PassengerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PassengerService {

  @Autowired
  PassengerRepository passengerRepository;

  public List<PassengerView> findAll() {
    return Streams.stream(passengerRepository.findAll())
        .map(this::createPassengerView)
        .collect(Collectors.toList());
  }

  public Optional<PassengerView> getPassenger(String passengerId) {
    return passengerRepository.findById(passengerId)
        .map(this::createPassengerView);
  }

  public PassengerView createPassenger(CreatePassengerRequest request) {
    Passenger passenger = new Passenger();
    passenger.setName(request.getName());
    passenger.setEmail(request.getEmail());
    passenger.setMobilePhoneNumber(request.getMobilePhoneNumber());
    List<UserAddress> userAddresses = Optional.ofNullable(request.getUserAddresses())
        .map(requests -> requests.stream()
            .map(this::createUserAddress)
            .collect(Collectors.toList())
        ).orElse(new ArrayList<>());
    passenger.setUserAddresses(userAddresses);
    return createPassengerView(passengerRepository.save(passenger));
  }

  public PassengerView addAddress(String passengerId, CreateUserAddressRequest request) {
    Passenger passenger = passengerRepository.findById(passengerId)
        .orElseThrow(() -> new PassengerNotFoundException(passengerId));
    UserAddress userAddress = createUserAddress(request);
    passenger.addUserAddress(userAddress);
    return createPassengerView(passenger);
  }

  public Optional<UserAddressView> getAddress(String passengerId, String addressId) {
    return passengerRepository.findById(passengerId)
        .flatMap(passenger -> passenger.getUserAddress(addressId))
        .map(this::createUserAddressView);
  }

  public PassengerView deleteAddress(String passengerId, String addressId) {
    Passenger passenger = passengerRepository.findById(passengerId)
        .orElseThrow(() -> new PassengerNotFoundException(passengerId));
    passenger.getUserAddress(addressId).ifPresent(passenger::removeUserAddress);
    return createPassengerView(passenger);
  }

  private UserAddress createUserAddress(CreateUserAddressRequest request) {
    UserAddress address = new UserAddress();
    address.generateId();
    address.setName(request.getName());
    address.setAddressId(request.getAddressId());
    return address;
  }

  private PassengerView createPassengerView(Passenger passenger) {
    return new PassengerView(passenger.getId(),
        passenger.getName(),
        passenger.getEmail(),
        passenger.getMobilePhoneNumber(),
        passenger.getUserAddresses().stream().map(PassengerUtils::createUserAddressView)
            .collect(Collectors.toList()));
  }

  private UserAddressView createUserAddressView(UserAddress userAddress) {
    return new UserAddressView(userAddress.getId(),
        userAddress.getName(),
        userAddress.getAddressId());
  }
}
