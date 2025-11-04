package com.wgt.imt.architecture.rest.comptes.model.input;

import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
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
public class CompteInput extends AbstractInput {

    @Serial
    private static final long serialVersionUID = -790440597537439877L;
    private String name;
    private String type;

    public static Compte convert(final CompteInput compteInput) {
        return Compte.builder()
                .identifier(UUID.randomUUID())
                .name(compteInput.getName())
                .type(TypeCompteEnum.fromOrDefault(compteInput.getType()))
                .solde(0.0)
                .build();
    }
}
