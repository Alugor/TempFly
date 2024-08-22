package xyz.alugor.tempfly.database.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class TempFly {
    private UUID uuid;
    private Long duration;
    private Boolean status;
}