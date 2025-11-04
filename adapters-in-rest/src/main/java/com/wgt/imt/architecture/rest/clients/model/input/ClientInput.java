package com.wgt.imt.architecture.rest.clients.model.input;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.rest.common.model.input.AbstractInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ClientInput extends AbstractInput {
    @Serial
    private static final long serialVersionUID = -6256916847598118362L;
    private String lastname;
    private String firstname;
    private String genre;

    public static Client convert(final ClientInput input) {
        return Client.builder()
                .identifier(UUID.randomUUID())
                .lastname(input.getLastname())
                .firstname(input.getFirstname())
                .genre(GenreEnum.fromOrDefault(input.getGenre()))
                .build();
    }
}
