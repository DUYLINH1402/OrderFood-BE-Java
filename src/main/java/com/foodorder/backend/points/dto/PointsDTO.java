package com.foodorder.backend.points.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for reward points information")
public class PointsDTO {

    @Schema(description = "Number of points", example = "100")
    private int amount;
}

