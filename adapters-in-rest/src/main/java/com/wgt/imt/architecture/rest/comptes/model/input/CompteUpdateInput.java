package com.wgt.imt.architecture.rest.comptes.model.input;

import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
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
public class CompteUpdateInput extends AbstractUpdateInput {

    @Serial
    private static final long serialVersionUID = -6069802379242271752L;
    private UpdatableProperty<String> name = UpdatableProperty.empty();
    private UpdatableProperty<String> type = UpdatableProperty.empty();
    private UpdatableProperty<Double> solde = UpdatableProperty.empty();

    public static Compte from(final CompteUpdateInput input, final Compte alreadySaved) {
        return Compte.builder()
                .identifier(alreadySaved.getIdentifier())
                .name(input.getName().defaultIfNotOverwrite(alreadySaved.getName()))
                .solde(input.getSolde().defaultIfNotOverwrite(alreadySaved.getSolde()))
                .type(input.getType().defaultIfNotOverwrite(TypeCompteEnum::fromOrDefault, alreadySaved.getType()))
                .build();
    }

    public void setName(final String name) {
        this.name = UpdatableProperty.makesChanges(name);
    }

    public void setType(final String type) {
        this.type = UpdatableProperty.makesChanges(type);
    }

    public void setSolde(final Double solde) {
        this.solde = UpdatableProperty.makesChanges(solde);
    }
}