import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class TripWishlistId implements Serializable {

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "user_id")
    private UUID userId;
}