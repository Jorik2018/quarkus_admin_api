package org.isobit.admin;

import lombok.*;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Result<T>{

    private List<T> data;

    private int size=0; 

}