package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notebook {
    private Long id;
    private Long chatId;
    private String surname;
    private String name;
    private String patronymic;
    private String phone;
    private String description;
}