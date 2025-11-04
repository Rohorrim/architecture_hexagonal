package com.wgt.imt.architecture.rest.clients.model.input;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.rest.common.model.input.AbstractUpdateInput;
import com.wgt.imt.architecture.rest.common.model.input.UpdatableProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ClientUpdateInput extends AbstractUpdateInput {

    @Serial
    private static final long serialVersionUID = -6190479828349200043L;
    private UpdatableProperty<String> lastname = UpdatableProperty.empty();
    private UpdatableProperty<String> firstname = UpdatableProperty.empty();
    private UpdatableProperty<String> genre = UpdatableProperty.empty();

    public static Client from(final ClientUpdateInput input, final Client alreadySaved) {
        return alreadySaved.toBuilder()
                .lastname(input.getLastname().defaultIfNotOverwrite(alreadySaved.getLastname()))
                .firstname(input.getFirstname().defaultIfNotOverwrite(alreadySaved.getFirstname()))
                .genre(input.getGenre().defaultIfNotOverwrite(GenreEnum::fromOrDefault, alreadySaved.getGenre()))
                .build();
    }

    public void setLastname(final String lastname) {
        this.lastname = UpdatableProperty.makesChanges(lastname);
    }

    public void setFirstname(final String firstname) {
        this.firstname = UpdatableProperty.makesChanges(firstname);
    }

    public void setGenre(final String genre) {
        this.genre = UpdatableProperty.makesChanges(genre);
    }
}
