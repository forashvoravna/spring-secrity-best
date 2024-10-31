package com.example.project.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class TaskDTO {
    private String id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
}
