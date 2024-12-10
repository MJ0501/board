package com.springboot.board.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList = "title"),
        @Index(columnList = "hashtag"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy"),
})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends AuditingFields{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter @ManyToOne(optional = false) private UserAccount userAccount;

    @Setter @Column(nullable = false)
    private String title;

    @Setter @Column(nullable = false, length = 10000)
    private String content;

    @Setter
    private String hashtag;

    @ToString.Exclude
    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();


    private Article(UserAccount userAccount, String title, String content, String hashtag) {
        this.userAccount = userAccount;
        this.title = title;
        this.content = content;
        this.hashtag = hashtag;
    }
    public static Article of(UserAccount userAccount, String title, String content, String hashtag) {
        return new Article(userAccount,title,content, hashtag);
    }

//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        Article article = (Article) o;
//        return Objects.equals(id, article.id);
//    }
//
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Article article)) return false;
        return id != null && id.equals(article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
