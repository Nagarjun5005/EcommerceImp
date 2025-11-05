package com.ecommerce.project.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Schema(name = "category id",description = "Automatically generated" )
    private Long categoryId;
    @Schema(name = "category Name",description = "All the products are grouped under category" ,example = "Electronics")
    private String categoryName;
}
