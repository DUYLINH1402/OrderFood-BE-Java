package com.foodorder.backend.zone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing ward information")
public class WardResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Ward ID", example = "1")
    private Long id;

    @Schema(description = "Ward name", example = "Ben Nghe Ward")
    private String name;

    @Schema(description = "ID of the district this ward belongs to", example = "1")
    private Long districtId;
}
