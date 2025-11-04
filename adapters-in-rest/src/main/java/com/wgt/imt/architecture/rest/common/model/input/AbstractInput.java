package com.wgt.imt.architecture.rest.common.model.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractInput implements Serializable {
    @Serial
    private static final long serialVersionUID = -3319284486621296531L;
}
