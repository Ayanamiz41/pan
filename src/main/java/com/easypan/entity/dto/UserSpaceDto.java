package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpaceDto implements Serializable {
    private Long useSpace;
    private Long totalSpace;

}
