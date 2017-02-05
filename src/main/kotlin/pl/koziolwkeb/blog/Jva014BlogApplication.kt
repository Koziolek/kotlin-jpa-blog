package pl.koziolwkeb.blog

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.stereotype.Component
import java.util.*
import javax.persistence.*
import javax.persistence.criteria.Path

@SpringBootApplication
open class Jva014BlogApplication {

    @Bean
    open fun init(repo: BlogRepository) = org.springframework.boot.CommandLineRunner {
        val blog = repo.save(
                Blog("My blog", Author("Ja"), ArrayList<BlogPost>())
        )

        repo.findAll().forEach{b -> print(b)}
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Jva014BlogApplication::class.java, *args)
}

@MappedSuperclass
class DomainObject(
        @Id var id: UUID = UUID.randomUUID(),
        @Version var version: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as DomainObject

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Entity
@AttributeOverrides(
        AttributeOverride(name = "id", column = Column(name = "AUTHOR_ID")),
        AttributeOverride(name = "version", column = Column(name = "AUTHOR_VERSION"))
)
@Table(name = "AUTHOR")
class Author(var name: String = "") : DomainObject()

@Entity
@NamedQuery(name = "findAll", query = "select B from Blog B order by B.name")
@AttributeOverrides(
        AttributeOverride(name = "id", column = Column(name = "BLOG_ID")),
        AttributeOverride(name = "version", column = Column(name = "BLOG_VERSION"))
)
@Table(name = "BLOG")
class Blog(

        @Column(name = "BLOG_NAME")
        val name: String = "",

        @JoinColumn(name = "BLOG_AUTHOR")
        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        var author: Author = Author(),

        @OneToMany(mappedBy = "blog", fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.ALL))
        var posts: MutableList<BlogPost> = ArrayList<BlogPost>()
) : DomainObject()

@Entity
@NamedQuery(name = "findByBlog", query = "select P from BlogPost P where P.blog.id = :id")
@AttributeOverrides(
        AttributeOverride(name = "id", column = Column(name = "POST_ID")),
        AttributeOverride(name = "version", column = Column(name = "POST_VERSION"))
)
@Table(name = "BLOG_POST")
class BlogPost(
        @Column(name = "POST_DATE")
        var date: Date = Date(),

        @Column(name = "POST_TITLE")
        var title: String = "",

        @Lob
        @Basic(fetch = FetchType.EAGER)
        @Column(name = "POST_TEXT")
        var text: String = "",

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "BLOG_ID")
        var blog: Blog? = null
) : DomainObject()

@RepositoryRestResource(
        path = "blog"
)
interface BlogRepository : JpaRepository<Blog, UUID>

@RepositoryRestResource(
        path = "blogPost"
)
interface BlogPostRepository : JpaRepository<BlogPost, UUID>, JpaSpecificationExecutor<BlogPost>

@Component
class BlogPostService(val blogPostRepo: BlogPostRepository) {

    fun findBlogPostsByBlogId(id: UUID) {
        blogPostRepo.findAll(findBlogPostByBlogId(id))
    }
}

fun findBlogPostByBlogId(id: UUID): Specification<BlogPost> {
    return Specification<BlogPost> { root, cq, cb ->
        val path: Path<String> = root.get("id")
        cb.equal(path, id)
    }
}

















