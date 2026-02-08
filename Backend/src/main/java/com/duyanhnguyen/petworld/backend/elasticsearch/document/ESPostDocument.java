package com.duyanhnguyen.petworld.backend.elasticsearch.document;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ESPostDocument {

    @Id
    Long id;

    @Field(type = FieldType.Text)
    String content;

    @Field(type = FieldType.Date)
    Instant createdAt;

    @Field(type = FieldType.Long)
    Long groupId;

}
