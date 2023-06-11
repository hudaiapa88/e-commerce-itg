package com.uc.ecommerce.model.dto.category;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    @NotEmpty(message = "{constraint.common.Empty.message}")
    @Size(max = 255,message = "{constraint.common.Size.message}")
    private String title;
}
