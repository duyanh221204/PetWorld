package com.duyanhnguyen.petworld.backend.elasticsearch.document;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ESGroupDocument {

    @Id
    @Field(type = FieldType.Long)
    Long id;

    @Field(type = FieldType.Text)
    String name;

    @Field(type = FieldType.Date)
    Instant createdAt;

}
