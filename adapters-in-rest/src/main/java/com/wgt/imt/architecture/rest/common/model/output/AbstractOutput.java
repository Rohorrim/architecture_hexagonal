package com.wgt.imt.architecture.rest.common.model.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AbstractOutput implements Serializable {
    @Serial
    private static final long serialVersionUID = 2457194761432688330L;
}
