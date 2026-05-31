package com.libri.server.dto;

import com.libri.server.entity.Author;
import lombok.Data;

@Data
public class AuthorDto {
    private Long id;
    private String firstName;
    private String lastName;

    public static AuthorDto from(Author a) {
        AuthorDto dto = new AuthorDto();
        dto.setId(a.getId());
        dto.setFirstName(a.getFirstName());
        dto.setLastName(a.getLastName());
        return dto;
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
