package com.softlabs.aicontents.domain.sample.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SampleRequestDTO {

  @NotBlank(message = "이름은 필수입니다")
  private String name;

  @Email(message = "올바른 이메일 형식이 아닙니다")
  @NotBlank(message = "이메일은 필수입니다")
  private String email;

  @Min(value = 18, message = "나이는 18세 이상이어야 합니다")
  @Max(value = 100, message = "나이는 100세 이하여야 합니다")
  @NotNull(message = "나이는 필수입니다")
  private Integer age;

  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-0000-0000)")
  private String phone;

  @Size(min = 2, max = 50, message = "주소는 2자 이상 50자 이하여야 합니다")
  private String address;
}
