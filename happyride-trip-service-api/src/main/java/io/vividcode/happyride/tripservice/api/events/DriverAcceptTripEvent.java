package io.vividcode.happyride.tripservice.api.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class DriverAcceptTripEvent implements TripDomainEvent {

  @NonNull
  private DriverAcceptTripDetails acceptTripDetails;
}
