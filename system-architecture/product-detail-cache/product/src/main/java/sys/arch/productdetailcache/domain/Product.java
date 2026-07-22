package sys.arch.productdetailcache.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean popular;

    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Product() {
    }

    public Product(String name, int price, String description, boolean popular) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.popular = popular;
        this.version = 1L;
        this.updatedAt = LocalDateTime.now();
    }

    public Product(Long id, String name, int price, String description, boolean popular) {
        this(name, price, description, popular);
        this.id = id;
    }

    public void update(String name, int price, String description, boolean popular) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.popular = popular;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        if (version == 0L) {
            version = 1L;
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPopular() {
        return popular;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
