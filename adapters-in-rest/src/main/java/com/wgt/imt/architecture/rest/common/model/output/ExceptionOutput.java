package com.wgt.imt.architecture.rest.common.model.output;

import lombok.*;

import java.io.Serial;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ExceptionOutput extends AbstractOutput {

    @Serial
    private static final long serialVersionUID = 3305123755780216165L;
    private final String type;
    private final String message;
    private final String timestamp;
    private final String path;
}
